package com.curso.restaurante.api.fornecedor;

import com.curso.restaurante.domain.fornecedor.Fornecedor;
import com.curso.restaurante.domain.usuario.PerfilUsuario;
import com.curso.restaurante.service.fornecedor.FornecedorService;
import com.curso.restaurante.service.usuario.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class FornecedorControllerTest {

    private static final String SENHA_ADMIN_BOOTSTRAP = "admin123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FornecedorService fornecedorService;

    @Autowired
    private UsuarioService usuarioService;

    @Test
    void adminDeveCriarFornecedor() throws Exception {
        mockMvc.perform(post("/api/fornecedores")
                        .with(httpBasic("admin", SENHA_ADMIN_BOOTSTRAP))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"razaoSocial":"Distribuidora Ctrl Ltda","cnpj":"11122233344455"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.razaoSocial").value("Distribuidora Ctrl Ltda"))
                .andExpect(jsonPath("$.status").value("ATIVO"));
    }

    @Test
    void garcomNaoDeveCriarFornecedor() throws Exception {
        usuarioService.criar("Garçom Fornecedor Ctrl", "garcom.fornecedor.ctrl", "senha", PerfilUsuario.GARCOM);

        mockMvc.perform(post("/api/fornecedores")
                        .with(httpBasic("garcom.fornecedor.ctrl", "senha"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"razaoSocial":"Outra Distribuidora Ctrl","cnpj":"22233344455566"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveRetornar400ParaCnpjInvalido() throws Exception {
        mockMvc.perform(post("/api/fornecedores")
                        .with(httpBasic("admin", SENHA_ADMIN_BOOTSTRAP))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"razaoSocial":"Fornecedor Invalido Ctrl","cnpj":"123"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornar409ParaCnpjDuplicado() throws Exception {
        fornecedorService.cadastrar("Fornecedor Duplicado Ctrl", "33344455566677");

        mockMvc.perform(post("/api/fornecedores")
                        .with(httpBasic("admin", SENHA_ADMIN_BOOTSTRAP))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"razaoSocial":"Outro Nome Ctrl","cnpj":"33344455566677"}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void semCredencialDeveRetornar401() throws Exception {
        mockMvc.perform(get("/api/fornecedores"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void qualquerAutenticadoDeveListarFornecedores() throws Exception {
        usuarioService.criar("Cozinha Lista Fornecedor Ctrl", "cozinha.lista.fornecedor.ctrl", "senha", PerfilUsuario.COZINHA);

        mockMvc.perform(get("/api/fornecedores").with(httpBasic("cozinha.lista.fornecedor.ctrl", "senha")))
                .andExpect(status().isOk());
    }

    @Test
    void adminDeveBuscarFornecedorPorId() throws Exception {
        Fornecedor criado = fornecedorService.cadastrar("Buscar Id Fornecedor Ctrl", "44455566677788");

        mockMvc.perform(get("/api/fornecedores/" + criado.getId())
                        .with(httpBasic("admin", SENHA_ADMIN_BOOTSTRAP)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.razaoSocial").value("Buscar Id Fornecedor Ctrl"));
    }

    @Test
    void adminDeveReceber404ParaFornecedorInexistente() throws Exception {
        mockMvc.perform(get("/api/fornecedores/999999")
                        .with(httpBasic("admin", SENHA_ADMIN_BOOTSTRAP)))
                .andExpect(status().isNotFound());
    }

    @Test
    void adminDeveAtivarEInativarFornecedor() throws Exception {
        Fornecedor criado = fornecedorService.cadastrar("Toggle Fornecedor Ctrl", "55566677788899");

        mockMvc.perform(post("/api/fornecedores/" + criado.getId() + "/inativar")
                        .with(httpBasic("admin", SENHA_ADMIN_BOOTSTRAP)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INATIVO"));

        mockMvc.perform(post("/api/fornecedores/" + criado.getId() + "/ativar")
                        .with(httpBasic("admin", SENHA_ADMIN_BOOTSTRAP)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ATIVO"));
    }
}
