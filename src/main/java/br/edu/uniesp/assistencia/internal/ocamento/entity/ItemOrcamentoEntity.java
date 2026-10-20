package br.edu.uniesp.assistencia.internal.ocamento.entity;

import br.edu.uniesp.assistencia.internal.peca.entity.PecaEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@Table(name = "itens_orcamento")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemOrcamentoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "orcamento_id", nullable = false)
    private OrcamentoEntity orcamento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "peca_id", nullable = false)
    private PecaEntity peca;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(name = "preco_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoUnitario;

    @Column(name = "valor_total_item", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorTotalItem;

    public ItemOrcamentoEntity(OrcamentoEntity orcamento, PecaEntity peca, Integer quantidade, BigDecimal precoUnitario) {
        this.orcamento = orcamento;
        this.peca = peca;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
        this.valorTotalItem = precoUnitario.multiply(BigDecimal.valueOf(quantidade));
    }
}
