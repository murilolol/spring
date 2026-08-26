package com.curso.restaurante.domain.comum;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValidacoesTest {

    @Test
    void exigirTextoDeveRejeitarNulo() {
        assertThrows(
                IllegalArgumentException.class,
                () -> Validacoes.exigirTexto(null, "Nome é obrigatório"));
    }

    @Test
    void exigirTextoDeveRejeitarBranco() {
        assertThrows(
                IllegalArgumentException.class,
                () -> Validacoes.exigirTexto("   ", "Nome é obrigatório"));
    }

    @Test
    void exigirTextoDeveApararEspacos() {
        String resultado = Validacoes.exigirTexto("  Bruschetta  ", "Nome é obrigatório");

        assertEquals("Bruschetta", resultado);
    }

    @Test
    void exigirNaoNuloDeveRejeitarNulo() {
        assertThrows(
                NullPointerException.class,
                () -> Validacoes.exigirNaoNulo(null, "Categoria é obrigatória"));
    }

    @Test
    void exigirNaoNuloDeveRetornarOMesmoValor() {
        Object valor = new Object();

        assertEquals(valor, Validacoes.exigirNaoNulo(valor, "Categoria é obrigatória"));
    }

    @Test
    void exigirNaoNegativoDecimalDeveRejeitarNegativo() {
        assertThrows(
                IllegalArgumentException.class,
                () -> Validacoes.exigirNaoNegativo(new BigDecimal("-0.01"), "Saldo não pode ser negativo"));
    }

    @Test
    void exigirNaoNegativoDecimalDeveAceitarZero() {
        BigDecimal resultado = Validacoes.exigirNaoNegativo(BigDecimal.ZERO, "Saldo não pode ser negativo");

        assertEquals(BigDecimal.ZERO, resultado);
    }

    @Test
    void exigirPositivoDecimalDeveRejeitarZero() {
        assertThrows(
                IllegalArgumentException.class,
                () -> Validacoes.exigirPositivo(BigDecimal.ZERO, "Valor deve ser maior que zero"));
    }

    @Test
    void exigirPositivoDecimalDeveAceitarPositivo() {
        BigDecimal resultado = Validacoes.exigirPositivo(new BigDecimal("0.01"), "Valor deve ser maior que zero");

        assertEquals(new BigDecimal("0.01"), resultado);
    }

    @Test
    void exigirPositivoInteiroDeveRejeitarZeroENegativo() {
        assertThrows(
                IllegalArgumentException.class,
                () -> Validacoes.exigirPositivo(0, "Capacidade deve ser maior que zero"));
        assertThrows(
                IllegalArgumentException.class,
                () -> Validacoes.exigirPositivo(-1, "Capacidade deve ser maior que zero"));
    }

    @Test
    void exigirPositivoInteiroDeveAceitarPositivo() {
        assertEquals(4, Validacoes.exigirPositivo(4, "Capacidade deve ser maior que zero"));
    }

    @Test
    void exigirNaoNegativoInteiroDeveRejeitarNegativo() {
        assertThrows(
                IllegalArgumentException.class,
                () -> Validacoes.exigirNaoNegativo(-1, "Ordem de exibição não pode ser negativa"));
    }

    @Test
    void exigirNaoNegativoInteiroDeveAceitarZero() {
        assertEquals(0, Validacoes.exigirNaoNegativo(0, "Ordem de exibição não pode ser negativa"));
    }

    @Test
    void exigirEntreDeveRejeitarForaDoIntervalo() {
        assertThrows(
                IllegalArgumentException.class,
                () -> Validacoes.exigirEntre(
                        new BigDecimal("101"), BigDecimal.ZERO, new BigDecimal("100"), "Percentual inválido"));
        assertThrows(
                IllegalArgumentException.class,
                () -> Validacoes.exigirEntre(
                        new BigDecimal("-1"), BigDecimal.ZERO, new BigDecimal("100"), "Percentual inválido"));
    }

    @Test
    void exigirEntreDeveAceitarLimites() {
        assertEquals(
                BigDecimal.ZERO,
                Validacoes.exigirEntre(BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("100"), "Percentual inválido"));
        assertEquals(
                new BigDecimal("100"),
                Validacoes.exigirEntre(new BigDecimal("100"), BigDecimal.ZERO, new BigDecimal("100"), "Percentual inválido"));
    }
}
