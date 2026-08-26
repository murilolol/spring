package com.curso.restaurante.api.cardapio.dto;

import com.curso.restaurante.domain.Status;
import com.curso.restaurante.domain.cardapio.ItemCardapio;
import com.curso.restaurante.domain.cardapio.SecaoPreparo;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ItemCardapioResponse(
        Long id,
        Long categoriaId,
        String categoriaNome,
        String codigo,
        String nome,
        String descricao,
        BigDecimal precoVenda,
        int tempoPreparoMinutos,
        SecaoPreparo secaoPreparo,
        boolean exigePreparo,
        boolean controlaEstoque,
        BigDecimal saldoEstoque,
        LocalDate dataCadastro,
        Status status) {

    public static ItemCardapioResponse de(ItemCardapio item) {
        return new ItemCardapioResponse(
                item.getId(),
                item.getCategoria().getId(),
                item.getCategoria().getNome(),
                item.getCodigo(),
                item.getNome(),
                item.getDescricao(),
                item.getPrecoVenda(),
                item.getTempoPreparoMinutos(),
                item.getSecaoPreparo(),
                item.isExigePreparo(),
                item.isControlaEstoque(),
                item.getSaldoEstoque(),
                item.getDataCadastro(),
                item.getStatus());
    }
}
