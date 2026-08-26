package com.curso.restaurante.config;

import com.curso.restaurante.domain.usuario.PerfilUsuario;
import com.curso.restaurante.domain.usuario.Usuario;
import com.curso.restaurante.repository.usuario.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class BootstrapUsuarioAdminTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    void deveCriarUmUsuarioAdminNaInicializacaoDoContexto() {
        Usuario admin = usuarioRepository.findByUsername("admin").orElseThrow();

        assertEquals(PerfilUsuario.ADMIN, admin.getPerfil());
        assertTrue(admin.estaAtivo());
    }
}
