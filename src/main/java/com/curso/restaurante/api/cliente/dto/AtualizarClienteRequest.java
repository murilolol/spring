package com.curso.restaurante.api.cliente.dto;

import jakarta.validation.constraints.NotBlank;

public record AtualizarClienteRequest(
        @NotBlank(message = "não pode ficar em branco") String nome,
        @NotBlank(message = "não pode ficar em branco") String telefone,
        String email,
        String endereco) {
}
