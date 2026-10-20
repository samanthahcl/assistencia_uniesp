package br.edu.uniesp.assistencia.internal.ocamento.entity;

import br.edu.uniesp.assistencia.internal.ordemservico.entity.OrdemServicoEntity;
import br.edu.uniesp.assistencia.internal.peca.entity.PecaEntity;
import br.edu.uniesp.assistencia.shared.exception.RegraNegocioException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "orcamentos")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrcamentoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ordem_servico_id", nullable = false, unique = true)
    private OrdemServicoEntity ordemServico;

    @Column(name = "valor_mao_de_obra", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorMaoDeObra = BigDecimal.ZERO;

    @Column(name = "valor_pecas", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorPecas = BigDecimal.ZERO;

    @Column(name = "valor_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorTotal = BigDecimal.ZERO;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "data_expiracao", nullable = false)
    private LocalDateTime dataExpiracao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusOrcamento status = StatusOrcamento.PENDENTE;

    @Column(name = "data_resposta")
    private LocalDateTime dataResposta;

    @Column(name = "justificativa_cliente", columnDefinition = "TEXT")
    private String justificativaCliente;

    @OneToMany(mappedBy = "orcamento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemOrcamentoEntity> itens = new ArrayList<>();

    public OrcamentoEntity(OrdemServicoEntity ordemServico, BigDecimal valorMaoDeObra, int diasValidade) {
        if (valorMaoDeObra == null || valorMaoDeObra.compareTo(BigDecimal.ZERO) < 0) {
            throw new RegraNegocioException("Valor da mão de obra não pode ser negativo.");
        }
        this.ordemServico = ordemServico;
        this.valorMaoDeObra = valorMaoDeObra;
        this.valorPecas = BigDecimal.ZERO;
        this.valorTotal = valorMaoDeObra;
        this.dataCriacao = LocalDateTime.now();
        this.dataExpiracao = LocalDateTime.now().plusDays(diasValidade > 0 ? diasValidade : 10);
        this.status = StatusOrcamento.PENDENTE;
    }

    public void adicionarItem(PecaEntity peca, int quantidade) {
        if (this.status != StatusOrcamento.PENDENTE) {
            throw new RegraNegocioException("Não é permitido alterar itens de um orçamento já finalizado.");
        }
        if (quantidade <= 0) {
            throw new RegraNegocioException("Quantidade do item deve ser positiva.");
        }
        if (!peca.isAtivo()) {
            throw new RegraNegocioException("Não é permitido adicionar uma peça inativa ao orçamento.");
        }
        ItemOrcamentoEntity item = new ItemOrcamentoEntity(this, peca, quantidade, peca.getPreco());
        this.itens.add(item);
        recalcularTotais();
    }

    public void atualizarMaoDeObra(BigDecimal novoValor) {
        if (this.status != StatusOrcamento.PENDENTE) {
            throw new RegraNegocioException("Não é permitido alterar valores de um orçamento já finalizado.");
        }
        if (novoValor == null || novoValor.compareTo(BigDecimal.ZERO) < 0) {
            throw new RegraNegocioException("Valor da mão de obra não pode ser negativo.");
        }
        this.valorMaoDeObra = novoValor;
        recalcularTotais();
    }

    public void recalcularTotais() {
        this.valorPecas = this.itens.stream()
                .map(ItemOrcamentoEntity::getValorTotalItem)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.valorTotal = this.valorMaoDeObra.add(this.valorPecas);

        if (this.valorTotal.compareTo(BigDecimal.ZERO) <= 0 && this.itens.isEmpty()) {
            throw new RegraNegocioException("O orçamento deve possuir pelo menos mão de obra ou uma peça com valor positivo.");
        }
    }

    public void aprovar() {
        if (this.status != StatusOrcamento.PENDENTE) {
            throw new RegraNegocioException("Somente orçamento pendente pode ser aprovado.");
        }
        if (LocalDateTime.now().isAfter(this.dataExpiracao)) {
            this.status = StatusOrcamento.EXPIRADO;
            throw new RegraNegocioException("Orçamento expirado não pode ser aprovado.");
        }
        this.status = StatusOrcamento.APROVADO;
        this.dataResposta = LocalDateTime.now();
    }

    public void rejeitar(String justificativa) {
        if (this.status != StatusOrcamento.PENDENTE) {
            throw new RegraNegocioException("Somente orçamento pendente pode ser rejeitado.");
        }
        this.status = StatusOrcamento.REJEITADO;
        this.dataResposta = LocalDateTime.now();
        this.justificativaCliente = justificativa;
    }
}
