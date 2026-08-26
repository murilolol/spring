package com.curso.restaurante.api.comanda.dto;

import jakarta.validation.constraints.NotNull;

public record TransferirMesaRequest(@NotNull(message = "é obrigatório") Long mesaDestinoId) {
}
