package br.edu.uniesp.assistencia.internal.ordemservico.entity;

import br.edu.uniesp.assistencia.internal.peca.entity.PecaEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "os_pecas")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrdemServicoPecaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ordem_servico_id", nullable = false)
    private OrdemServicoEntity ordemServico;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "peca_id", nullable = false)
    private PecaEntity peca;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(name = "preco_unitario_historico", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoUnitarioHistorico;

    @Column(name = "data_registro", nullable = false, updatable = false)
    private LocalDateTime dataRegistro;

    public OrdemServicoPecaEntity(OrdemServicoEntity ordemServico, PecaEntity peca, Integer quantidade, BigDecimal precoUnitarioHistorico) {
        this.ordemServico = ordemServico;
        this.peca = peca;
        this.quantidade = quantidade;
        this.precoUnitarioHistorico = precoUnitarioHistorico;
        this.dataRegistro = LocalDateTime.now();
    }
}
