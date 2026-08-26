package com.curso.restaurante.api;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SegurancaTest {

    private static final String SENHA_ADMIN_BOOTSTRAP = "admin123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioService usuarioService;

    @Test
    void healthDeveSerPublico() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk());
    }

    @Test
    void endpointProtegidoSemCredencialDeveRetornar401() throws Exception {
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void endpointRestritoAAdminComPerfilInsuficienteDeveRetornar403() throws Exception {
        usuarioService.criar("Garçom Segurança", "garcom.seguranca.matriz", "senha", PerfilUsuario.GARCOM);

        mockMvc.perform(get("/api/usuarios").with(httpBasic("garcom.seguranca.matriz", "senha")))
                .andExpect(status().isForbidden());
    }

    @Test
    void endpointRestritoAAdminComPerfilCorretoDeveRetornar200() throws Exception {
        mockMvc.perform(get("/api/usuarios").with(httpBasic("admin", SENHA_ADMIN_BOOTSTRAP)))
                .andExpect(status().isOk());
    }

    @Test
    void endpointAutenticadoComCredencialInvalidaDeveRetornar401() throws Exception {
        mockMvc.perform(get("/api/auth/me").with(httpBasic("admin", "senha-errada")))
                .andExpect(status().isUnauthorized());
    }
}
