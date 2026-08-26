package com.curso.restaurante.api.comum;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaginaResponseTest {

    @Test
    void deveConverterUmaPageEmPaginaResponse() {
        PageImpl<String> pagina = new PageImpl<>(
                List.of("Entradas", "Bebidas"),
                PageRequest.of(1, 2),
                5);

        PaginaResponse<String> resposta = PaginaResponse.de(pagina);

        assertEquals(List.of("Entradas", "Bebidas"), resposta.conteudo());
        assertEquals(1, resposta.pagina());
        assertEquals(2, resposta.tamanho());
        assertEquals(5, resposta.totalElementos());
        assertEquals(3, resposta.totalPaginas());
        assertFalse(resposta.primeira());
        assertFalse(resposta.ultima());
    }

    @Test
    void deveIndicarPrimeiraEUltimaPaginaQuandoHaApenasUma() {
        PageImpl<String> pagina = new PageImpl<>(
                List.of("Entradas"),
                PageRequest.of(0, 10),
                1);

        PaginaResponse<String> resposta = PaginaResponse.de(pagina);

        assertTrue(resposta.primeira());
        assertTrue(resposta.ultima());
    }
}
