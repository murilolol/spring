package com.curso.restaurante.api.usuario;

import com.curso.restaurante.domain.usuario.PerfilUsuario;
import com.curso.restaurante.domain.usuario.Usuario;
import com.curso.restaurante.service.usuario.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UsuarioControllerTest {

    private static final String SENHA_ADMIN_BOOTSTRAP = "admin123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioService usuarioService;

    @Test
    void adminDeveCriarUsuario() throws Exception {
        mockMvc.perform(post("/api/usuarios")
                        .with(httpBasic("admin", SENHA_ADMIN_BOOTSTRAP))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Novo Garçom","username":"novo.garcom.ctrl","senha":"senha123","perfil":"GARCOM"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.username").value("novo.garcom.ctrl"))
                .andExpect(jsonPath("$.perfil").value("GARCOM"));
    }

    @Test
    void naoAdminNaoDeveCriarUsuario() throws Exception {
        usuarioService.criar("Garçom Comum", "garcom.comum.ctrl", "senha", PerfilUsuario.GARCOM);

        mockMvc.perform(post("/api/usuarios")
                        .with(httpBasic("garcom.comum.ctrl", "senha"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Outro","username":"outro.ctrl","senha":"senha123","perfil":"GARCOM"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void semCredencialDeveRetornar401() throws Exception {
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void requisicaoInvalidaDeveRetornar400ComListaDeCampos() throws Exception {
        mockMvc.perform(post("/api/usuarios")
                        .with(httpBasic("admin", SENHA_ADMIN_BOOTSTRAP))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"","username":"","senha":"","perfil":null}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos").isArray());
    }

    @Test
    void adminDeveBuscarUsuarioPorId() throws Exception {
        Usuario criado = usuarioService.criar("Buscar Id Ctrl", "buscar.id.ctrl", "senha", PerfilUsuario.COZINHA);

        mockMvc.perform(get("/api/usuarios/" + criado.getId())
                        .with(httpBasic("admin", SENHA_ADMIN_BOOTSTRAP)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("buscar.id.ctrl"));
    }

    @Test
    void adminDeveReceber404ParaUsuarioInexistente() throws Exception {
        mockMvc.perform(get("/api/usuarios/999999")
                        .with(httpBasic("admin", SENHA_ADMIN_BOOTSTRAP)))
                .andExpect(status().isNotFound());
    }

    @Test
    void adminDeveAtualizarUsuario() throws Exception {
        Usuario criado = usuarioService.criar("Nome Original Ctrl", "atualizar.ctrl", "senha", PerfilUsuario.GARCOM);

        mockMvc.perform(put("/api/usuarios/" + criado.getId())
                        .with(httpBasic("admin", SENHA_ADMIN_BOOTSTRAP))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Nome Atualizado Ctrl","perfil":"ADMIN"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Nome Atualizado Ctrl"))
                .andExpect(jsonPath("$.perfil").value("ADMIN"));
    }

    @Test
    void donoDeveAlterarAPropriaSenha() throws Exception {
        Usuario criado = usuarioService.criar("Dono Senha Ctrl", "dono.senha.ctrl", "senha-atual-ctrl", PerfilUsuario.GARCOM);

        mockMvc.perform(patch("/api/usuarios/" + criado.getId() + "/senha")
                        .with(httpBasic("dono.senha.ctrl", "senha-atual-ctrl"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"senhaAtual":"senha-atual-ctrl","novaSenha":"senha-nova-ctrl"}
                                """))
                .andExpect(status().isNoContent());
    }

    @Test
    void naoDonoNemAdminNaoDeveAlterarSenhaDeOutroUsuario() throws Exception {
        Usuario vitima = usuarioService.criar("Vitima Ctrl", "vitima.ctrl", "senha-vitima-ctrl", PerfilUsuario.GARCOM);
        usuarioService.criar("Atacante Ctrl", "atacante.ctrl", "senha-atacante-ctrl", PerfilUsuario.GARCOM);

        mockMvc.perform(patch("/api/usuarios/" + vitima.getId() + "/senha")
                        .with(httpBasic("atacante.ctrl", "senha-atacante-ctrl"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"senhaAtual":"senha-vitima-ctrl","novaSenha":"senha-nova-ctrl"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminDeveAtivarEInativarUsuario() throws Exception {
        Usuario criado = usuarioService.criar("Toggle Ctrl", "toggle.ctrl", "senha", PerfilUsuario.GARCOM);

        mockMvc.perform(post("/api/usuarios/" + criado.getId() + "/inativar")
                        .with(httpBasic("admin", SENHA_ADMIN_BOOTSTRAP)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INATIVO"));

        mockMvc.perform(post("/api/usuarios/" + criado.getId() + "/ativar")
                        .with(httpBasic("admin", SENHA_ADMIN_BOOTSTRAP)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ATIVO"));
    }
}
