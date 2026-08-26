package com.curso.restaurante.domain.comum;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class ConflitoDeEstadoExceptionTest {

    @Test
    void deveConterAMensagemFornecida() {
        ConflitoDeEstadoException excecao = new ConflitoDeEstadoException("Já existe uma sessão de caixa aberta");

        assertEquals("Já existe uma sessão de caixa aberta", excecao.getMessage());
    }

    @Test
    void deveSerUmaDominioException() {
        ConflitoDeEstadoException excecao = new ConflitoDeEstadoException("Já existe uma sessão de caixa aberta");

        assertInstanceOf(DominioException.class, excecao);
    }
}
