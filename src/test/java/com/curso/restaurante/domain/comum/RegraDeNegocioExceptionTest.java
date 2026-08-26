package com.curso.restaurante.domain.comum;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class RegraDeNegocioExceptionTest {

    @Test
    void deveConterAMensagemFornecida() {
        RegraDeNegocioException excecao = new RegraDeNegocioException("Saldo em estoque insuficiente");

        assertEquals("Saldo em estoque insuficiente", excecao.getMessage());
    }

    @Test
    void deveSerUmaDominioException() {
        RegraDeNegocioException excecao = new RegraDeNegocioException("Saldo em estoque insuficiente");

        assertInstanceOf(DominioException.class, excecao);
    }
}
