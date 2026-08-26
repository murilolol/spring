package com.curso.restaurante.api.cozinha.dto;

import com.curso.restaurante.domain.cardapio.SecaoPreparo;
import com.curso.restaurante.domain.cozinha.PreparoItem;
import com.curso.restaurante.domain.cozinha.PrioridadePreparo;
import com.curso.restaurante.domain.cozinha.StatusPreparo;

import java.time.LocalDateTime;

public record PreparoItemResponse(
        Long id,
        Long itemPedidoId,
        String itemCardapioNome,
        int quantidade,
        Long pedidoId,
        String pedidoCodigo,
        SecaoPreparo secao,
        StatusPreparo status,
        PrioridadePreparo prioridade,
        int tempoEstimadoMinutos,
        String responsavelNome,
        LocalDateTime enfileiradoEm,
        LocalDateTime iniciadoEm,
        LocalDateTime concluidoEm,
        LocalDateTime canceladoEm) {

    public static PreparoItemResponse de(PreparoItem preparoItem) {
        return new PreparoItemResponse(
                preparoItem.getId(),
                preparoItem.getItemPedido().getId(),
                preparoItem.getItemPedido().getItemCardapio().getNome(),
                preparoItem.getItemPedido().getQuantidade(),
                preparoItem.getItemPedido().getPedido().getId(),
                preparoItem.getItemPedido().getPedido().getCodigo(),
                preparoItem.getSecao(),
                preparoItem.getStatus(),
                preparoItem.getPrioridade(),
                preparoItem.getTempoEstimadoMinutos(),
                preparoItem.getResponsavel() == null ? null : preparoItem.getResponsavel().getNome(),
                preparoItem.getEnfileiradoEm(),
                preparoItem.getIniciadoEm(),
                preparoItem.getConcluidoEm(),
                preparoItem.getCanceladoEm());
    }
}
