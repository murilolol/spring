package com.curso.restaurante.api.pedido.dto;

import com.curso.restaurante.domain.pedido.Pedido;
import com.curso.restaurante.domain.pedido.StatusPedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoResponse(
        Long id,
        String codigo,
        Long comandaId,
        String solicitanteNome,
        StatusPedido status,
        String observacao,
        String motivoCancelamento,
        List<ItemPedidoResponse> itens,
        BigDecimal total,
        LocalDateTime abertoEm,
        LocalDateTime enviadoPreparoEm,
        LocalDateTime prontoEm,
        LocalDateTime entregueEm,
        LocalDateTime pagoEm,
        LocalDateTime canceladoEm) {

    public static PedidoResponse de(Pedido pedido) {
        return new PedidoResponse(
                pedido.getId(),
                pedido.getCodigo(),
                pedido.getComanda().getId(),
                pedido.getSolicitante().getNome(),
                pedido.getStatus(),
                pedido.getObservacao(),
                pedido.getMotivoCancelamento(),
                pedido.getItens().stream().map(ItemPedidoResponse::de).toList(),
                pedido.calcularTotal(),
                pedido.getAbertoEm(),
                pedido.getEnviadoPreparoEm(),
                pedido.getProntoEm(),
                pedido.getEntregueEm(),
                pedido.getPagoEm(),
                pedido.getCanceladoEm());
    }
}
