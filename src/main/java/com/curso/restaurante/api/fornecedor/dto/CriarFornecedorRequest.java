package com.curso.restaurante.api.fornecedor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CriarFornecedorRequest(
        @NotBlank(message = "não pode ficar em branco") @Size(max = 150, message = "deve ter no máximo 150 caracteres")
        String razaoSocial,
        @NotBlank(message = "não pode ficar em branco")
        @Pattern(regexp = "\\d{14}", message = "deve conter exatamente 14 dígitos numéricos")
        String cnpj) {
}
