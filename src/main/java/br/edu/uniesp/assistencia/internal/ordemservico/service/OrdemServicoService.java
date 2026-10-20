package br.edu.uniesp.assistencia.internal.ordemservico.service;

import br.edu.uniesp.assistencia.internal.cliente.entity.ClienteEntity;
import br.edu.uniesp.assistencia.internal.cliente.repository.ClienteRepository;
import br.edu.uniesp.assistencia.internal.equipamento.entity.EquipamentoEntity;
import br.edu.uniesp.assistencia.internal.equipamento.repository.EquipamentoRepository;
import br.edu.uniesp.assistencia.internal.ordemservico.dto.CriarOrdemServicoRequest;
import br.edu.uniesp.assistencia.internal.ordemservico.dto.CriarOrdemServicoResponse;
import br.edu.uniesp.assistencia.internal.ordemservico.entity.OrdemServicoEntity;
import br.edu.uniesp.assistencia.internal.ordemservico.mapper.OrdemServicoMapper;
import br.edu.uniesp.assistencia.internal.ordemservico.repository.OrdemServicoRepository;
import br.edu.uniesp.assistencia.shared.exception.RecursoNaoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
public class OrdemServicoService {

    static final int MAX_TENTATIVAS_PROTOCOLO = 5;
    static final String CONSTRAINT_PROTOCOLO_UNICO = "uk_ordens_servico_numero_protocolo";

    private final OrdemServicoRepository ordemServicoRepository;
    private final ClienteRepository clienteRepository;
    private final EquipamentoRepository equipamentoRepository;
    private final GeradorNumeroProtocolo geradorNumeroProtocolo;
    private final TransactionTemplate transactionTemplate;

    /**
     * Caso de uso "abrir OS".
     * <p>
     * Cada tentativa roda em UMA transação: a OS e o histórico inicial (cascade) são gravados juntos e,
     * se qualquer parte falhar, tudo sofre rollback (RN14). Se o banco rejeitar o protocolo por colisão
     * (RN04), a transação daquela tentativa é descartada e uma nova é aberta com outro protocolo.
     * Não usamos {@code @Transactional} no método porque, após uma violação de constraint, a transação
     * e a sessão do Hibernate ficam inutilizáveis e não podem ser reaproveitadas na nova tentativa.
     */
    public CriarOrdemServicoResponse abrirOrdemServico(CriarOrdemServicoRequest request) {
        for (int tentativa = 1; ; tentativa++) {
            try {
                return transactionTemplate.execute(status -> abrirEmTransacao(request));
            } catch (DataIntegrityViolationException e) {
                if (!violouUnicidadeDoProtocolo(e) || tentativa >= MAX_TENTATIVAS_PROTOCOLO) {
                    throw e;
                }
            }
        }
    }

    private CriarOrdemServicoResponse abrirEmTransacao(CriarOrdemServicoRequest request) {
        ClienteEntity cliente = clienteRepository.findById(request.clienteId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente", request.clienteId()));

        EquipamentoEntity equipamento = equipamentoRepository.findById(request.equipamentoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Equipamento", request.equipamentoId()));

        // RN01, RN02, RN03 e RN14 são garantidas pelo domínio (construtor de OrdemServicoEntity)
        OrdemServicoEntity ordemServico = OrdemServicoMapper.paraEntidade(
                request, geradorNumeroProtocolo.gerar(), cliente, equipamento);

        // saveAndFlush: a violação de unicidade aparece aqui, ainda dentro da tentativa
        ordemServicoRepository.saveAndFlush(ordemServico);

        return OrdemServicoMapper.paraResposta(ordemServico);
    }

    private boolean violouUnicidadeDoProtocolo(DataIntegrityViolationException e) {
        Throwable causa = e.getMostSpecificCause();
        String mensagem = causa.getMessage();
        return mensagem != null && mensagem.contains(CONSTRAINT_PROTOCOLO_UNICO);
    }
}
