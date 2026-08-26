package com.curso.restaurante.api.caixa.dto;

import com.curso.restaurante.domain.caixa.FormaPagamento;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record RegistrarPagamentoRequest(
        @NotNull(message = "é obrigatório") FormaPagamento formaPagamento,
        @NotNull(message = "é obrigatório") @Positive(message = "deve ser maior que zero") BigDecimal valor,
        BigDecimal valorRecebido) {
}
