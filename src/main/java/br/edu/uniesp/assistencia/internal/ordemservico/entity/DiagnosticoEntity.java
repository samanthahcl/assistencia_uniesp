package br.edu.uniesp.assistencia.internal.ordemservico.entity;

import br.edu.uniesp.assistencia.internal.tecnico.entity.TecnicoEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "diagnosticos")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DiagnosticoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ordem_servico_id", nullable = false, unique = true)
    private OrdemServicoEntity ordemServico;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tecnico_id", nullable = false)
    private TecnicoEntity tecnico;

    @Column(name = "descricao_tecnica", nullable = false, columnDefinition = "TEXT")
    private String descricaoTecnica;

    @Column(name = "solucao_proposta", nullable = false, columnDefinition = "TEXT")
    private String solucaoProposta;

    @Column(name = "data_registro", nullable = false, updatable = false)
    private LocalDateTime dataRegistro;

    public DiagnosticoEntity(OrdemServicoEntity ordemServico, TecnicoEntity tecnico,
                             String descricaoTecnica, String solucaoProposta) {
        this.ordemServico = ordemServico;
        this.tecnico = tecnico;
        this.descricaoTecnica = descricaoTecnica;
        this.solucaoProposta = solucaoProposta;
        this.dataRegistro = LocalDateTime.now();
    }
}
