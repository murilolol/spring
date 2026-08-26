package com.curso.restaurante.api.cliente.dto;

import com.curso.restaurante.domain.Status;
import com.curso.restaurante.domain.cliente.Cliente;

import java.time.LocalDate;

public record ClienteResponse(
        Long id,
        String nome,
        String documento,
        String telefone,
        String email,
        String endereco,
        LocalDate dataNascimento,
        LocalDate dataCadastro,
        Status status) {

    public static ClienteResponse de(Cliente cliente) {
        return new ClienteResponse(
                cliente.getId(),
                cliente.getNome(),
                cliente.getDocumento(),
                cliente.getTelefone(),
                cliente.getEmail(),
                cliente.getEndereco(),
                cliente.getDataNascimento(),
                cliente.getDataCadastro(),
                cliente.getStatus());
    }
}
