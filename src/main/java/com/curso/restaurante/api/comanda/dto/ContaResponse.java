package com.curso.restaurante.api.comanda.dto;

import com.curso.restaurante.domain.comanda.Comanda;

import java.math.BigDecimal;

public record ContaResponse(
        BigDecimal subtotal, BigDecimal taxaServico, BigDecimal total, BigDecimal totalPago, BigDecimal saldoDevedor) {

    public static ContaResponse de(Comanda comanda, BigDecimal totalPago) {
        return new ContaResponse(
                comanda.calcularSubtotal(),
                comanda.calcularTaxaServico(),
                comanda.calcularTotal(),
                totalPago,
                comanda.calcularSaldoDevedor(totalPago));
    }
}
