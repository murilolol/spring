package com.curso.restaurante.api.cliente.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record CriarClienteRequest(
        @NotBlank(message = "não pode ficar em branco") String nome,
        String documento,
        @NotBlank(message = "não pode ficar em branco") String telefone,
        String email,
        String endereco,
        LocalDate dataNascimento) {
}
