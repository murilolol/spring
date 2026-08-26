package com.curso.restaurante.api.cozinha.dto;

import com.curso.restaurante.domain.cozinha.PrioridadePreparo;
import jakarta.validation.constraints.NotNull;

public record AlterarPrioridadeRequest(@NotNull(message = "é obrigatório") PrioridadePreparo prioridade) {
}
