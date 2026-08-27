package com.curso.restaurante.api.cardapio.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record AlterarEstoqueMinimoRequest(
        @NotNull(message = "é obrigatório") @PositiveOrZero(message = "não pode ser negativo") BigDecimal estoqueMinimo) {
}
