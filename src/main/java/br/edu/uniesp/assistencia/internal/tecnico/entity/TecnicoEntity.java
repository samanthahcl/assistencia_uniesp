package br.edu.uniesp.assistencia.internal.tecnico.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "tecnicos")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TecnicoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, length = 150, unique = true)
    private String email;

    @Column(length = 20)
    private String telefone;

    @Column(length = 100)
    private String especialidade;

    @Column(nullable = false)
    private boolean ativo = true;

    public TecnicoEntity(String nome, String email, String telefone, String especialidade) {
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
        this.especialidade = especialidade;
    }

    public void alterarNome(String nome) {
        this.nome = nome;
    }

    public void alterarTelefone(String telefone) {
        this.telefone = telefone;
    }

    public void alterarEspecialidade(String especialidade) {
        this.especialidade = especialidade;
    }

    public void ativar() {
        this.ativo = true;
    }

    public void inativar() {
        this.ativo = false;
    }
}
