package com.curso.restaurante.api.usuario.dto;

import com.curso.restaurante.domain.usuario.PerfilUsuario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AtualizarUsuarioRequest(
        @NotBlank(message = "não pode ficar em branco") String nome,
        @NotNull(message = "é obrigatório") PerfilUsuario perfil) {
}
