package br.edu.uniesp.assistencia.shared.exception;

import lombok.Getter;

@Getter
public class RecursoNaoEncontradoException extends RuntimeException {

    private final String recurso;
    private final Object identificador;

    public RecursoNaoEncontradoException(String recurso, Object identificador) {
        super(recurso + " não encontrado para o identificador: " + identificador);

        this.recurso = recurso;
        this.identificador = identificador;
    }
}