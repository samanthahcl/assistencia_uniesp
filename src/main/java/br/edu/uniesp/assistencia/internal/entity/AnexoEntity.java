package br.edu.uniesp.assistencia.internal.entity;

import br.edu.uniesp.assistencia.internal.ordemservico.entity.OrdemServicoEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "anexos")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnexoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ordem_servico_id", nullable = false)
    private OrdemServicoEntity ordemServico;

    @Column(name = "nome_original", nullable = false, length = 255)
    private String nomeOriginal;

    @Column(name = "nome_arquivo_storage", nullable = false, unique = true, length = 255)
    private String nomeArquivoStorage;

    @Column(name = "tipo_conteudo", nullable = false, length = 100)
    private String tipoConteudo;

    @Column(name = "tamanho_bytes", nullable = false)
    private Long tamanhoBytes;

    @Column(name = "caminho_storage", nullable = false, length = 500)
    private String caminhoStorage;

    @Column(name = "data_upload", nullable = false, updatable = false)
    private LocalDateTime dataUpload;

    public AnexoEntity(OrdemServicoEntity ordemServico, String nomeOriginal, String nomeArquivoStorage,
                       String tipoConteudo, Long tamanhoBytes, String caminhoStorage) {
        this.ordemServico = ordemServico;
        this.nomeOriginal = nomeOriginal;
        this.nomeArquivoStorage = nomeArquivoStorage;
        this.tipoConteudo = tipoConteudo;
        this.tamanhoBytes = tamanhoBytes;
        this.caminhoStorage = caminhoStorage;
        this.dataUpload = LocalDateTime.now();
    }
}
