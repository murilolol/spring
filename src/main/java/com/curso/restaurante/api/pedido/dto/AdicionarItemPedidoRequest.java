package com.curso.restaurante.api.pedido.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AdicionarItemPedidoRequest(
        @NotNull(message = "é obrigatório") Long itemCardapioId,
        @NotNull(message = "é obrigatório") @Positive(message = "deve ser maior que zero") Integer quantidade,
        String observacao) {
}
