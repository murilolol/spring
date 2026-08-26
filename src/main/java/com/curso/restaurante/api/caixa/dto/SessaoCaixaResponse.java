package com.curso.restaurante.api.caixa.dto;

import com.curso.restaurante.domain.caixa.SessaoCaixa;
import com.curso.restaurante.domain.caixa.StatusSessaoCaixa;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SessaoCaixaResponse(
        Long id,
        String usuarioAberturaNome,
        String usuarioFechamentoNome,
        BigDecimal valorAbertura,
        BigDecimal valorInformadoFechamento,
        BigDecimal valorApuradoFechamento,
        BigDecimal diferencaFechamento,
        StatusSessaoCaixa status,
        String observacaoFechamento,
        LocalDateTime abertaEm,
        LocalDateTime fechadaEm) {

    public static SessaoCaixaResponse de(SessaoCaixa sessao) {
        return new SessaoCaixaResponse(
                sessao.getId(),
                sessao.getUsuarioAbertura().getNome(),
                sessao.getUsuarioFechamento() == null ? null : sessao.getUsuarioFechamento().getNome(),
                sessao.getValorAbertura(),
                sessao.getValorInformadoFechamento(),
                sessao.getValorApuradoFechamento(),
                sessao.getDiferencaFechamento(),
                sessao.getStatus(),
                sessao.getObservacaoFechamento(),
                sessao.getAbertaEm(),
                sessao.getFechadaEm());
    }
}
