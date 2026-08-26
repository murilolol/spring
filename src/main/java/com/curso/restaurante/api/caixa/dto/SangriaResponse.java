package com.curso.restaurante.api.caixa.dto;

import com.curso.restaurante.domain.caixa.Sangria;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SangriaResponse(
        Long id, Long sessaoCaixaId, BigDecimal valor, String motivo, String usuarioNome, LocalDateTime registradaEm) {

    public static SangriaResponse de(Sangria sangria) {
        return new SangriaResponse(
                sangria.getId(),
                sangria.getSessaoCaixa().getId(),
                sangria.getValor(),
                sangria.getMotivo(),
                sangria.getUsuario().getNome(),
                sangria.getRegistradaEm());
    }
}
