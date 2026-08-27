package com.curso.restaurante.api.cardapio.dto;

import com.curso.restaurante.domain.cardapio.SecaoPreparo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CriarItemCardapioRequest(
        @NotNull(message = "é obrigatório") Long categoriaId,
        @NotBlank(message = "não pode ficar em branco") String codigo,
        @NotBlank(message = "não pode ficar em branco") String nome,
        String descricao,
        @NotNull(message = "é obrigatório") @PositiveOrZero(message = "não pode ser negativo") BigDecimal precoVenda,
        @NotNull(message = "é obrigatório") @PositiveOrZero(message = "não pode ser negativo") Integer tempoPreparoMinutos,
        @NotNull(message = "é obrigatório") SecaoPreparo secaoPreparo,
        @NotNull(message = "é obrigatório") Boolean exigePreparo,
        @NotNull(message = "é obrigatório") Boolean controlaEstoque,
        @NotNull(message = "é obrigatório") @PositiveOrZero(message = "não pode ser negativo") BigDecimal saldoEstoque,
        @NotNull(message = "é obrigatório") LocalDate dataCadastro,
        @NotNull(message = "é obrigatório") @PositiveOrZero(message = "não pode ser negativo") BigDecimal estoqueMinimo,
        @Positive(message = "deve ser maior que zero") Long fornecedorId) {
}
