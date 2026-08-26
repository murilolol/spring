package com.curso.restaurante.api.cardapio.dto;

import com.curso.restaurante.domain.Status;
import com.curso.restaurante.domain.cardapio.CategoriaCardapio;

public record CategoriaCardapioResponse(
        Long id, String nome, String descricao, int ordemExibicao, Status status) {

    public static CategoriaCardapioResponse de(CategoriaCardapio categoria) {
        return new CategoriaCardapioResponse(
                categoria.getId(),
                categoria.getNome(),
                categoria.getDescricao(),
                categoria.getOrdemExibicao(),
                categoria.getStatus());
    }
}
