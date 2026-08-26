package com.curso.restaurante.api.usuario.dto;

import jakarta.validation.constraints.NotBlank;

public record AlterarSenhaRequest(
        String senhaAtual,
        @NotBlank(message = "não pode ficar em branco") String novaSenha) {
}
