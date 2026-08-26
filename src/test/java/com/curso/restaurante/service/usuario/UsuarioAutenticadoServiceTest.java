package com.curso.restaurante.service.usuario;

import com.curso.restaurante.domain.usuario.PerfilUsuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UsuarioAutenticadoServiceTest {

    @Autowired
    private UsuarioAutenticadoService usuarioAutenticadoService;

    @Autowired
    private UsuarioService usuarioService;

    @Test
    void deveCarregarUsuarioComAutoridadeDoPerfilPrefixadaComRole() {
        usuarioService.criar("Ana Caixa", "ana.autenticacao", "senha", PerfilUsuario.CAIXA);

        UserDetails userDetails = usuarioAutenticadoService.loadUserByUsername("ana.autenticacao");

        assertEquals("ana.autenticacao", userDetails.getUsername());
        assertTrue(userDetails.isEnabled());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(autoridade -> autoridade.getAuthority().equals("ROLE_CAIXA")));
    }

    @Test
    void deveLancarQuandoUsuarioNaoExiste() {
        assertThrows(
                UsernameNotFoundException.class,
                () -> usuarioAutenticadoService.loadUserByUsername("nao.existe.autenticacao"));
    }

    @Test
    void deveRetornarUserDetailsDesabilitadoQuandoUsuarioInativo() {
        var usuario = usuarioService.criar("Inativo Auth", "inativo.autenticacao", "senha", PerfilUsuario.GARCOM);
        usuarioService.inativar(usuario.getId());

        UserDetails userDetails = usuarioAutenticadoService.loadUserByUsername("inativo.autenticacao");

        assertFalse(userDetails.isEnabled());
    }
}
