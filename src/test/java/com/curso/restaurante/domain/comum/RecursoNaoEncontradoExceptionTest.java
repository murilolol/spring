package com.curso.restaurante.domain.comum;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class RecursoNaoEncontradoExceptionTest {

    @Test
    void deveConterAMensagemFornecida() {
        RecursoNaoEncontradoException excecao = new RecursoNaoEncontradoException("Cliente não encontrado");

        assertEquals("Cliente não encontrado", excecao.getMessage());
    }

    @Test
    void deveSerUmaDominioException() {
        RecursoNaoEncontradoException excecao = new RecursoNaoEncontradoException("Cliente não encontrado");

        assertInstanceOf(DominioException.class, excecao);
    }
}
