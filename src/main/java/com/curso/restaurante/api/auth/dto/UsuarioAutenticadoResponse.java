package com.curso.restaurante.api.auth.dto;

import com.curso.restaurante.domain.usuario.PerfilUsuario;
import com.curso.restaurante.domain.usuario.Usuario;

public record UsuarioAutenticadoResponse(String username, String nome, PerfilUsuario perfil) {

    public static UsuarioAutenticadoResponse de(Usuario usuario) {
        return new UsuarioAutenticadoResponse(usuario.getUsername(), usuario.getNome(), usuario.getPerfil());
    }
}
