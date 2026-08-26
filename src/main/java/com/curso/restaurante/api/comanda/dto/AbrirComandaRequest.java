package com.curso.restaurante.api.comanda.dto;

import com.curso.restaurante.domain.comanda.TipoAtendimento;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record AbrirComandaRequest(
        @NotNull(message = "é obrigatório") TipoAtendimento tipoAtendimento,
        Long mesaId,
        Long clienteId,
        @NotNull(message = "é obrigatório") @Positive(message = "deve ser maior que zero") Integer numeroPessoas,
        BigDecimal percentualTaxaServico,
        String observacao) {
}
