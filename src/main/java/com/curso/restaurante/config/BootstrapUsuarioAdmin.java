package com.curso.restaurante.config;

import com.curso.restaurante.domain.usuario.PerfilUsuario;
import com.curso.restaurante.repository.usuario.UsuarioRepository;
import com.curso.restaurante.service.usuario.UsuarioService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({"dev", "test"})
public class BootstrapUsuarioAdmin implements ApplicationRunner {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;
    private final String senhaInicial;

    public BootstrapUsuarioAdmin(
            UsuarioRepository usuarioRepository,
            UsuarioService usuarioService,
            @Value("${app.admin-bootstrap-password}") String senhaInicial) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioService = usuarioService;
        this.senhaInicial = senhaInicial;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (usuarioRepository.count() == 0) {
            usuarioService.criar("Administrador", "admin", senhaInicial, PerfilUsuario.ADMIN);
        }
    }
}
