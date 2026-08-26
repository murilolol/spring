package com.curso.restaurante.api.auth;

import com.curso.restaurante.domain.usuario.PerfilUsuario;
import com.curso.restaurante.service.usuario.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AutenticacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioService usuarioService;

    @Test
    void deveRetornarDadosDoUsuarioAutenticado() throws Exception {
        usuarioService.criar("Cliente Autenticado", "auth.me.ctrl", "senha-auth", PerfilUsuario.CAIXA);

        mockMvc.perform(get("/api/auth/me").with(httpBasic("auth.me.ctrl", "senha-auth")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("auth.me.ctrl"))
                .andExpect(jsonPath("$.nome").value("Cliente Autenticado"))
                .andExpect(jsonPath("$.perfil").value("CAIXA"));
    }

    @Test
    void semCredencialDeveRetornar401() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }
}
