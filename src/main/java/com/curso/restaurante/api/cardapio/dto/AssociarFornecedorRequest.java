package com.curso.restaurante.api.cardapio.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AssociarFornecedorRequest(
        @NotNull(message = "é obrigatório") @Positive(message = "deve ser maior que zero") Long fornecedorId) {
}
