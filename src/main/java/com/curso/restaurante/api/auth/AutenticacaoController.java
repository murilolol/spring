package com.curso.restaurante.api.auth;

import com.curso.restaurante.api.auth.dto.UsuarioAutenticadoResponse;
import com.curso.restaurante.service.usuario.UsuarioService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AutenticacaoController {

    private final UsuarioService usuarioService;

    public AutenticacaoController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/me")
    public UsuarioAutenticadoResponse me(Authentication authentication) {
        return UsuarioAutenticadoResponse.de(usuarioService.buscarPorUsername(authentication.getName()));
    }
}
