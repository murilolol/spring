package com.curso.restaurante.api.caixa.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record RegistrarSangriaRequest(
        @NotNull(message = "é obrigatório") @Positive(message = "deve ser maior que zero") BigDecimal valor,
        @NotBlank(message = "não pode ficar em branco") String motivo) {
}
