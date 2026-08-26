package com.curso.restaurante.api.cardapio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record AtualizarCategoriaCardapioRequest(
        @NotBlank(message = "não pode ficar em branco") String nome,
        String descricao,
        @NotNull(message = "é obrigatório") @PositiveOrZero(message = "não pode ser negativo") Integer ordemExibicao) {
}
