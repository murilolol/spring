package com.curso.restaurante.domain.comum;

public class ConflitoDeEstadoException extends DominioException {

    public ConflitoDeEstadoException(String mensagem) {
        super(mensagem);
    }
}
