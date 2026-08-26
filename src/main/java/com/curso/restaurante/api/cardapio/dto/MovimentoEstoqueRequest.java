package com.curso.restaurante.api.cardapio.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record MovimentoEstoqueRequest(
        @NotNull(message = "é obrigatório") @Positive(message = "deve ser maior que zero") BigDecimal quantidade) {
}
