package com.curso.restaurante.api.comum;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CancelarRequestTest {

    @Test
    void deveExporOMotivo() {
        CancelarRequest request = new CancelarRequest("Cliente desistiu");

        assertEquals("Cliente desistiu", request.motivo());
    }
}
