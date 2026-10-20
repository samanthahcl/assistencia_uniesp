package br.edu.uniesp.assistencia.internal.ordemservico.service;

import br.edu.uniesp.assistencia.internal.cliente.entity.ClienteEntity;
import br.edu.uniesp.assistencia.internal.cliente.repository.ClienteRepository;
import br.edu.uniesp.assistencia.internal.equipamento.entity.EquipamentoEntity;
import br.edu.uniesp.assistencia.internal.equipamento.repository.EquipamentoRepository;
import br.edu.uniesp.assistencia.internal.ordemservico.dto.CriarOrdemServicoRequest;
import br.edu.uniesp.assistencia.internal.ordemservico.dto.CriarOrdemServicoResponse;
import br.edu.uniesp.assistencia.internal.ordemservico.entity.HistoricoStatusEntity;
import br.edu.uniesp.assistencia.internal.ordemservico.entity.OrdemServicoEntity;
import br.edu.uniesp.assistencia.internal.ordemservico.entity.StatusOrdemServico;
import br.edu.uniesp.assistencia.internal.ordemservico.repository.OrdemServicoRepository;
import br.edu.uniesp.assistencia.shared.exception.RecursoNaoEncontradoException;
import br.edu.uniesp.assistencia.shared.exception.RegraNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrdemServicoService - abertura de OS")
class OrdemServicoServiceTest {

    @Mock OrdemServicoRepository ordemServicoRepository;
    @Mock ClienteRepository clienteRepository;
    @Mock EquipamentoRepository equipamentoRepository;
    @Mock GeradorNumeroProtocolo geradorNumeroProtocolo;
    @Mock PlatformTransactionManager transactionManager;

    OrdemServicoService service;

    private final CriarOrdemServicoRequest request =
            new CriarOrdemServicoRequest(1L, 2L, "Notebook não liga após queda de energia.");

    @BeforeEach
    void setUp() {
        service = new OrdemServicoService(ordemServicoRepository, clienteRepository, equipamentoRepository,
                geradorNumeroProtocolo, new TransactionTemplate(transactionManager));
    }

    private static ClienteEntity cliente(long id, boolean ativo) {
        ClienteEntity c = new ClienteEntity("Maria", "12345678901", "maria" + id + "@email.com");
        ReflectionTestUtils.setField(c, "id", id);
        if (!ativo) {
            c.inativar();
        }
        return c;
    }

    private static EquipamentoEntity equipamento(long id, ClienteEntity dono) {
        EquipamentoEntity e = new EquipamentoEntity("Notebook", "Dell", "Inspiron", "SN-" + id);
        ReflectionTestUtils.setField(e, "id", id);
        e.associarCliente(dono);
        return e;
    }

    /** Simula o banco: devolve a entidade com id gerado. */
    private void saveAndFlushAtribuiId() {
        when(ordemServicoRepository.saveAndFlush(any(OrdemServicoEntity.class))).thenAnswer(inv -> {
            OrdemServicoEntity os = inv.getArgument(0);
            ReflectionTestUtils.setField(os, "id", 10L);
            return os;
        });
    }

    private static DataIntegrityViolationException colisaoDeProtocolo() {
        return new DataIntegrityViolationException("erro",
                new RuntimeException("duplicate key value violates unique constraint \""
                        + OrdemServicoService.CONSTRAINT_PROTOCOLO_UNICO + "\""));
    }

    // ---------- Cenário 1 ----------
    @Test
    @DisplayName("Cenário 1: abre OS com sucesso e grava histórico inicial na mesma transação")
    void abreComSucesso() {
        ClienteEntity cliente = cliente(1L, true);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(equipamentoRepository.findById(2L)).thenReturn(Optional.of(equipamento(2L, cliente)));
        when(geradorNumeroProtocolo.gerar()).thenReturn("OS-20261015-A1B2");
        saveAndFlushAtribuiId();

        CriarOrdemServicoResponse response = service.abrirOrdemServico(request);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.numeroProtocolo()).matches("^OS-\\d{8}-[A-Z0-9]{4}$");
        assertThat(response.status()).isEqualTo(StatusOrdemServico.RECEBIDA);
        assertThat(response.dataAbertura()).isNotNull();

        ArgumentCaptor<OrdemServicoEntity> captor = ArgumentCaptor.forClass(OrdemServicoEntity.class);
        verify(ordemServicoRepository).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getHistorico()).hasSize(1);
        HistoricoStatusEntity h = captor.getValue().getHistorico().get(0);
        assertThat(h.getNovoStatus()).isEqualTo(StatusOrdemServico.RECEBIDA);
        assertThat(h.getResponsavel()).isEqualTo("SISTEMA");

        verify(transactionManager).commit(any());
        verify(transactionManager, never()).rollback(any());
    }

    // ---------- Cenário 3 ----------
    @Test
    @DisplayName("Cenário 3a: cliente inexistente -> RecursoNaoEncontradoException e nada é salvo")
    void clienteInexistente() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.abrirOrdemServico(request))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .satisfies(e -> assertThat(((RecursoNaoEncontradoException) e).getRecurso()).isEqualTo("Cliente"));

        verifyNoInteractions(ordemServicoRepository);
        verify(transactionManager).rollback(any());
    }

    @Test
    @DisplayName("Cenário 3b: equipamento inexistente -> RecursoNaoEncontradoException e nada é salvo")
    void equipamentoInexistente() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente(1L, true)));
        when(equipamentoRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.abrirOrdemServico(request))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .satisfies(e -> assertThat(((RecursoNaoEncontradoException) e).getRecurso()).isEqualTo("Equipamento"));

        verifyNoInteractions(ordemServicoRepository);
    }

    // ---------- Cenário 4 ----------
    @Test
    @DisplayName("Cenário 4: cliente inativo -> RN01 e nada é salvo")
    void clienteInativo() {
        ClienteEntity inativo = cliente(1L, false);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(inativo));
        when(equipamentoRepository.findById(2L)).thenReturn(Optional.of(equipamento(2L, inativo)));
        when(geradorNumeroProtocolo.gerar()).thenReturn("OS-20261015-A1B2");

        assertThatThrownBy(() -> service.abrirOrdemServico(request))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Não é permitido abrir Ordem de Serviço para um cliente inativo.");

        verifyNoInteractions(ordemServicoRepository);
        verify(transactionManager).rollback(any());
    }

    // ---------- Cenário 5 ----------
    @Test
    @DisplayName("Cenário 5: equipamento de outro cliente -> RN02 e nada é salvo")
    void equipamentoDeOutroCliente() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente(1L, true)));
        when(equipamentoRepository.findById(2L)).thenReturn(Optional.of(equipamento(2L, cliente(99L, true))));
        when(geradorNumeroProtocolo.gerar()).thenReturn("OS-20261015-A1B2");

        assertThatThrownBy(() -> service.abrirOrdemServico(request))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("O equipamento informado não pertence ao cliente indicado.");

        verifyNoInteractions(ordemServicoRepository);
    }

    // ---------- RN04 ----------
    @Test
    @DisplayName("RN04: colisão de protocolo no banco -> nova transação com outro protocolo (sem duplicar)")
    void colisaoDeProtocoloGeraNovoProtocolo() {
        ClienteEntity cliente = cliente(1L, true);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(equipamentoRepository.findById(2L)).thenReturn(Optional.of(equipamento(2L, cliente)));
        when(geradorNumeroProtocolo.gerar()).thenReturn("OS-20261015-AAAA", "OS-20261015-BBBB");
        when(ordemServicoRepository.saveAndFlush(any(OrdemServicoEntity.class)))
                .thenThrow(colisaoDeProtocolo())
                .thenAnswer(inv -> inv.getArgument(0));

        CriarOrdemServicoResponse response = service.abrirOrdemServico(request);

        assertThat(response.numeroProtocolo()).isEqualTo("OS-20261015-BBBB");
        verify(ordemServicoRepository, times(2)).saveAndFlush(any(OrdemServicoEntity.class));
        verify(transactionManager).commit(any());
        verify(transactionManager).rollback(any()); // a tentativa colidida foi descartada
    }

    @Test
    @DisplayName("RN04: colisões persistentes esgotam as tentativas e a exceção é propagada")
    void colisoesEsgotamTentativas() {
        ClienteEntity cliente = cliente(1L, true);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(equipamentoRepository.findById(2L)).thenReturn(Optional.of(equipamento(2L, cliente)));
        when(geradorNumeroProtocolo.gerar()).thenReturn("OS-20261015-AAAA");
        when(ordemServicoRepository.saveAndFlush(any(OrdemServicoEntity.class))).thenThrow(colisaoDeProtocolo());

        assertThatThrownBy(() -> service.abrirOrdemServico(request))
                .isInstanceOf(DataIntegrityViolationException.class);

        verify(ordemServicoRepository, times(OrdemServicoService.MAX_TENTATIVAS_PROTOCOLO))
                .saveAndFlush(any(OrdemServicoEntity.class));
    }

    @Test
    @DisplayName("Outra violação de integridade (não é o protocolo) não é repetida")
    void outraViolacaoNaoRepete() {
        ClienteEntity cliente = cliente(1L, true);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(equipamentoRepository.findById(2L)).thenReturn(Optional.of(equipamento(2L, cliente)));
        when(geradorNumeroProtocolo.gerar()).thenReturn("OS-20261015-AAAA");
        when(ordemServicoRepository.saveAndFlush(any(OrdemServicoEntity.class)))
                .thenThrow(new DataIntegrityViolationException("erro", new RuntimeException("violates foreign key")));

        assertThatThrownBy(() -> service.abrirOrdemServico(request))
                .isInstanceOf(DataIntegrityViolationException.class);

        verify(ordemServicoRepository, times(1)).saveAndFlush(any(OrdemServicoEntity.class));
    }

    // ---------- RN14 ----------
    @Test
    @DisplayName("RN14: se a gravação (OS + histórico) falha, a transação sofre rollback e não há commit")
    void falhaNaPersistenciaFazRollback() {
        ClienteEntity cliente = cliente(1L, true);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(equipamentoRepository.findById(2L)).thenReturn(Optional.of(equipamento(2L, cliente)));
        when(geradorNumeroProtocolo.gerar()).thenReturn("OS-20261015-A1B2");
        when(ordemServicoRepository.saveAndFlush(any(OrdemServicoEntity.class)))
                .thenThrow(new IllegalStateException("falha ao gravar histórico"));

        assertThatThrownBy(() -> service.abrirOrdemServico(request))
                .isInstanceOf(IllegalStateException.class);

        verify(transactionManager).rollback(any());
        verify(transactionManager, never()).commit(any());
    }
}
