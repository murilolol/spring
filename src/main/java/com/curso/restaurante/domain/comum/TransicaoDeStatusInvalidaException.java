package com.curso.restaurante.domain.comum;

public class TransicaoDeStatusInvalidaException extends ConflitoDeEstadoException {

    public TransicaoDeStatusInvalidaException(String mensagem) {
        super(mensagem);
    }
}
