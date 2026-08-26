package com.curso.restaurante.domain.comum;

public abstract class DominioException extends RuntimeException {

    protected DominioException(String mensagem) {
        super(mensagem);
    }
}
