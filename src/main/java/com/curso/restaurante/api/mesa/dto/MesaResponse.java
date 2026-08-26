package com.curso.restaurante.api.mesa.dto;

import com.curso.restaurante.domain.mesa.Mesa;
import com.curso.restaurante.domain.mesa.StatusMesa;

public record MesaResponse(Long id, int numero, int capacidade, String setor, StatusMesa status) {

    public static MesaResponse de(Mesa mesa) {
        return new MesaResponse(mesa.getId(), mesa.getNumero(), mesa.getCapacidade(), mesa.getSetor(), mesa.getStatus());
    }
}
