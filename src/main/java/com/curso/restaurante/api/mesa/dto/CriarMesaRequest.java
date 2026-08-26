package com.curso.restaurante.api.mesa.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CriarMesaRequest(
        @NotNull(message = "é obrigatório") Integer numero,
        @NotNull(message = "é obrigatório") @Positive(message = "deve ser maior que zero") Integer capacidade,
        @NotNull(message = "é obrigatório") String setor) {
}
