package com.curso.restaurante.api.erro;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CampoInvalidoTest {

    @Test
    void deveExporCampoEMensagem() {
        CampoInvalido campoInvalido = new CampoInvalido("nome", "não pode ficar em branco");

        assertEquals("nome", campoInvalido.campo());
        assertEquals("não pode ficar em branco", campoInvalido.mensagem());
    }
}
