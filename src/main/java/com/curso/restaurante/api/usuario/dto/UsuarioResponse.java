package com.curso.restaurante.api.usuario.dto;

import com.curso.restaurante.domain.Status;
import com.curso.restaurante.domain.usuario.PerfilUsuario;
import com.curso.restaurante.domain.usuario.Usuario;

import java.time.LocalDateTime;

public record UsuarioResponse(
        Long id,
        String nome,
        String username,
        PerfilUsuario perfil,
        Status status,
        LocalDateTime criadoEm) {

    public static UsuarioResponse de(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getUsername(),
                usuario.getPerfil(),
                usuario.getStatus(),
                usuario.getCriadoEm());
    }
}
