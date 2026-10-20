package br.edu.uniesp.assistencia.internal.ordemservico.entity;

import br.edu.uniesp.assistencia.internal.cliente.entity.ClienteEntity;
import br.edu.uniesp.assistencia.internal.equipamento.entity.EquipamentoEntity;
import br.edu.uniesp.assistencia.shared.exception.RegraNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Domínio - abertura de OrdemServicoEntity (RN01, RN02, RN03, RN14)")
class OrdemServicoEntityTest {

    private static final String PROTOCOLO = "OS-20261015-A1B2";

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

    @Test
    @DisplayName("RN03/RN14: abre com status RECEBIDA, data de abertura e histórico inicial do SISTEMA")
    void abreComEstadoInicialEHistorico() {
        ClienteEntity cliente = cliente(1L, true);
        LocalDateTime antes = LocalDateTime.now().minusSeconds(1);

        OrdemServicoEntity os = new OrdemServicoEntity(PROTOCOLO, cliente, equipamento(2L, cliente),
                "  Notebook não liga.  ");

        assertThat(os.getStatus()).isEqualTo(StatusOrdemServico.RECEBIDA);
        assertThat(os.getNumeroProtocolo()).isEqualTo(PROTOCOLO);
        assertThat(os.getDescricaoProblema()).isEqualTo("Notebook não liga.");
        assertThat(os.getDataAbertura()).isNotNull().isAfter(antes).isBeforeOrEqualTo(LocalDateTime.now());

        assertThat(os.getHistorico()).hasSize(1);
        HistoricoStatusEntity h = os.getHistorico().get(0);
        assertThat(h.getOrdemServico()).isSameAs(os);
        assertThat(h.getStatusAnterior()).isNull();
        assertThat(h.getNovoStatus()).isEqualTo(StatusOrdemServico.RECEBIDA);
        assertThat(h.getResponsavel()).isEqualTo("SISTEMA");
        assertThat(h.getObservacao()).isNotBlank();
        assertThat(h.getDataAlteracao()).isNotNull();
    }

    @Test
    @DisplayName("RN01: cliente inativo não pode abrir OS (mensagem exata)")
    void clienteInativo() {
        ClienteEntity inativo = cliente(1L, false);
        EquipamentoEntity equipamento = equipamento(2L, inativo);

        assertThatThrownBy(() -> new OrdemServicoEntity(PROTOCOLO, inativo, equipamento, "Problema"))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Não é permitido abrir Ordem de Serviço para um cliente inativo.");
    }

    @Test
    @DisplayName("RN02: equipamento de outro cliente é rejeitado (mensagem exata)")
    void equipamentoDeOutroCliente() {
        ClienteEntity solicitante = cliente(1L, true);
        ClienteEntity outro = cliente(99L, true);
        EquipamentoEntity doOutro = equipamento(2L, outro);

        assertThatThrownBy(() -> new OrdemServicoEntity(PROTOCOLO, solicitante, doOutro, "Problema"))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("O equipamento informado não pertence ao cliente indicado.");
    }

    @Test
    @DisplayName("RN02: compara identificadores, não instâncias (mesmo id em objetos distintos é aceito)")
    void comparaPorIdentificador() {
        ClienteEntity solicitante = cliente(1L, true);
        ClienteEntity mesmoClienteOutraInstancia = cliente(1L, true);
        EquipamentoEntity equipamento = equipamento(2L, mesmoClienteOutraInstancia);

        OrdemServicoEntity os = new OrdemServicoEntity(PROTOCOLO, solicitante, equipamento, "Problema");

        assertThat(os.getCliente()).isSameAs(solicitante);
    }

    @Test
    @DisplayName("RN02: equipamento sem cliente associado é rejeitado")
    void equipamentoSemCliente() {
        ClienteEntity cliente = cliente(1L, true);
        EquipamentoEntity semDono = new EquipamentoEntity("Notebook", "Dell", "Inspiron", "SN");

        assertThatThrownBy(() -> new OrdemServicoEntity(PROTOCOLO, cliente, semDono, "Problema"))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("O equipamento informado não pertence ao cliente indicado.");
    }

    @Test
    @DisplayName("Descrição vazia ou em branco é rejeitada pelo domínio")
    void descricaoInvalida() {
        ClienteEntity cliente = cliente(1L, true);
        EquipamentoEntity equipamento = equipamento(2L, cliente);

        assertThatThrownBy(() -> new OrdemServicoEntity(PROTOCOLO, cliente, equipamento, "   "))
                .isInstanceOf(RegraNegocioException.class);
        assertThatThrownBy(() -> new OrdemServicoEntity(PROTOCOLO, cliente, equipamento, null))
                .isInstanceOf(RegraNegocioException.class);
    }
}
