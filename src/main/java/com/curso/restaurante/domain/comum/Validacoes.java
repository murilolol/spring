package com.curso.restaurante.domain.comum;

import java.math.BigDecimal;
import java.util.Objects;

public final class Validacoes {

    private Validacoes() {
    }

    public static String exigirTexto(String valor, String mensagem) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(mensagem);
        }
        return valor.trim();
    }

    public static <T> T exigirNaoNulo(T valor, String mensagem) {
        return Objects.requireNonNull(valor, mensagem);
    }

    public static BigDecimal exigirNaoNegativo(BigDecimal valor, String mensagem) {
        Objects.requireNonNull(valor, mensagem);
        if (valor.signum() < 0) {
            throw new IllegalArgumentException(mensagem);
        }
        return valor;
    }

    public static BigDecimal exigirPositivo(BigDecimal valor, String mensagem) {
        Objects.requireNonNull(valor, mensagem);
        if (valor.signum() <= 0) {
            throw new IllegalArgumentException(mensagem);
        }
        return valor;
    }

    public static int exigirPositivo(int valor, String mensagem) {
        if (valor <= 0) {
            throw new IllegalArgumentException(mensagem);
        }
        return valor;
    }

    public static int exigirNaoNegativo(int valor, String mensagem) {
        if (valor < 0) {
            throw new IllegalArgumentException(mensagem);
        }
        return valor;
    }

    public static BigDecimal exigirEntre(BigDecimal valor, BigDecimal minimo, BigDecimal maximo, String mensagem) {
        Objects.requireNonNull(valor, mensagem);
        if (valor.compareTo(minimo) < 0 || valor.compareTo(maximo) > 0) {
            throw new IllegalArgumentException(mensagem);
        }
        return valor;
    }
}
