package com.curso.restaurante.api.caixa.dto;

import com.curso.restaurante.domain.caixa.FormaPagamento;
import com.curso.restaurante.domain.caixa.Pagamento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PagamentoResponse(
        Long id,
        Long comandaId,
        Long sessaoCaixaId,
        FormaPagamento formaPagamento,
        BigDecimal valor,
        BigDecimal valorRecebido,
        BigDecimal troco,
        String usuarioNome,
        LocalDateTime registradoEm) {

    public static PagamentoResponse de(Pagamento pagamento) {
        return new PagamentoResponse(
                pagamento.getId(),
                pagamento.getComanda().getId(),
                pagamento.getSessaoCaixa().getId(),
                pagamento.getFormaPagamento(),
                pagamento.getValor(),
                pagamento.getValorRecebido(),
                pagamento.getTroco(),
                pagamento.getUsuario().getNome(),
                pagamento.getRegistradoEm());
    }
}
