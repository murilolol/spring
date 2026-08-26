package com.curso.restaurante.domain.comum;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class TransicaoDeStatusInvalidaExceptionTest {

    @Test
    void deveConterAMensagemFornecida() {
        TransicaoDeStatusInvalidaException excecao =
                new TransicaoDeStatusInvalidaException("Não é possível marcar como pronto um pedido cancelado");

        assertEquals("Não é possível marcar como pronto um pedido cancelado", excecao.getMessage());
    }

    @Test
    void deveSerUmConflitoDeEstado() {
        TransicaoDeStatusInvalidaException excecao =
                new TransicaoDeStatusInvalidaException("Não é possível marcar como pronto um pedido cancelado");

        assertInstanceOf(ConflitoDeEstadoException.class, excecao);
    }
}
