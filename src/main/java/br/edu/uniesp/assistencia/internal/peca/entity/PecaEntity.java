package br.edu.uniesp.assistencia.internal.peca.entity;

import br.edu.uniesp.assistencia.shared.exception.RegraNegocioException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@Table(name = "pecas")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PecaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, length = 50, unique = true)
    private String codigo;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;

    @Column(name = "quantidade_estoque", nullable = false)
    private Integer quantidadeEstoque = 0;

    @Column(nullable = false)
    private boolean ativo = true;

    public PecaEntity(String nome, String codigo, BigDecimal preco, Integer quantidadeEstoque) {
        if (preco == null || preco.compareTo(BigDecimal.ZERO) < 0) {
            throw new RegraNegocioException("Preço da peça não pode ser negativo.");
        }
        if (quantidadeEstoque == null || quantidadeEstoque < 0) {
            throw new RegraNegocioException("Quantidade em estoque não pode ser negativa.");
        }
        this.nome = nome;
        this.codigo = codigo;
        this.preco = preco;
        this.quantidadeEstoque = quantidadeEstoque;
    }

    public void debitarEstoque(int quantidade) {
        if (quantidade <= 0) {
            throw new RegraNegocioException("Quantidade para débito deve ser maior que zero.");
        }
        if (this.quantidadeEstoque < quantidade) {
            throw new RegraNegocioException("Estoque insuficiente para a peça '" + this.nome + "'. Disponível: " + this.quantidadeEstoque);
        }
        this.quantidadeEstoque -= quantidade;
    }

    public void reporEstoque(int quantidade) {
        if (quantidade <= 0) {
            throw new RegraNegocioException("Quantidade para reposição deve ser maior que zero.");
        }
        this.quantidadeEstoque += quantidade;
    }

    public void atualizarPreco(BigDecimal novoPreco) {
        if (novoPreco == null || novoPreco.compareTo(BigDecimal.ZERO) < 0) {
            throw new RegraNegocioException("Preço da peça não pode ser negativo.");
        }
        this.preco = novoPreco;
    }

    public void alterarNome(String nome) {
        this.nome = nome;
    }

    public void ativar() {
        this.ativo = true;
    }

    public void inativar() {
        this.ativo = false;
    }
}
