package com.curso.restaurante.api.pedido.dto;

import com.curso.restaurante.domain.pedido.ItemPedido;

import java.math.BigDecimal;

public record ItemPedidoResponse(
        Long id,
        Long itemCardapioId,
        String itemCardapioNome,
        int quantidade,
        BigDecimal precoUnitario,
        BigDecimal subtotal,
        String observacao) {

    public static ItemPedidoResponse de(ItemPedido item) {
        return new ItemPedidoResponse(
                item.getId(),
                item.getItemCardapio().getId(),
                item.getItemCardapio().getNome(),
                item.getQuantidade(),
                item.getPrecoUnitario(),
                item.calcularSubtotal(),
                item.getObservacao());
    }
}
