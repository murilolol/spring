package com.curso.restaurante.api.comanda.dto;

import com.curso.restaurante.domain.comanda.Comanda;
import com.curso.restaurante.domain.comanda.StatusComanda;
import com.curso.restaurante.domain.comanda.TipoAtendimento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ComandaResponse(
        Long id,
        String codigo,
        TipoAtendimento tipoAtendimento,
        Long mesaId,
        Integer mesaNumero,
        Long clienteId,
        String clienteNome,
        String responsavelNome,
        int numeroPessoas,
        BigDecimal percentualTaxaServico,
        StatusComanda status,
        LocalDateTime abertaEm,
        LocalDateTime fechadaEm,
        LocalDateTime canceladaEm,
        String observacao) {

    public static ComandaResponse de(Comanda comanda) {
        return new ComandaResponse(
                comanda.getId(),
                comanda.getCodigo(),
                comanda.getTipoAtendimento(),
                comanda.getMesa() == null ? null : comanda.getMesa().getId(),
                comanda.getMesa() == null ? null : comanda.getMesa().getNumero(),
                comanda.getCliente() == null ? null : comanda.getCliente().getId(),
                comanda.getCliente() == null ? null : comanda.getCliente().getNome(),
                comanda.getResponsavel().getNome(),
                comanda.getNumeroPessoas(),
                comanda.getPercentualTaxaServico(),
                comanda.getStatus(),
                comanda.getAbertaEm(),
                comanda.getFechadaEm(),
                comanda.getCanceladaEm(),
                comanda.getObservacao());
    }
}
