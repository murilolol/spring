package com.curso.restaurante.api.cardapio;

import com.curso.restaurante.domain.cardapio.CategoriaCardapio;
import com.curso.restaurante.domain.usuario.PerfilUsuario;
import com.curso.restaurante.service.cardapio.CategoriaCardapioService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CategoriaCardapioControllerTest {

    private static final String SENHA_ADMIN_BOOTSTRAP = "admin123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoriaCardapioService categoriaCardapioService;

    @Autowired
    private UsuarioService usuarioService;

    @Test
    void adminDeveCriarCategoria() throws Exception {
        mockMvc.perform(post("/api/categorias-cardapio")
                        .with(httpBasic("admin", SENHA_ADMIN_BOOTSTRAP))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Entradas Ctrl","descricao":"descrição","ordemExibicao":1}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Entradas Ctrl"));
    }

    @Test
    void garcomNaoDeveCriarCategoria() throws Exception {
        usuarioService.criar("Garçom Categoria Ctrl", "garcom.categoria.ctrl", "senha", PerfilUsuario.GARCOM);

        mockMvc.perform(post("/api/categorias-cardapio")
                        .with(httpBasic("garcom.categoria.ctrl", "senha"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Outra Ctrl","descricao":null,"ordemExibicao":1}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void qualquerAutenticadoDeveListarEBuscarCategoria() throws Exception {
        usuarioService.criar("Cozinha Categoria Ctrl", "cozinha.categoria.ctrl", "senha", PerfilUsuario.COZINHA);
        CategoriaCardapio criada = categoriaCardapioService.criar("Bebidas Ctrl", null, 1);

        mockMvc.perform(get("/api/categorias-cardapio").with(httpBasic("cozinha.categoria.ctrl", "senha")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/categorias-cardapio/" + criada.getId())
                        .with(httpBasic("cozinha.categoria.ctrl", "senha")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Bebidas Ctrl"));
    }

    @Test
    void adminDeveAtualizarCategoria() throws Exception {
        CategoriaCardapio criada = categoriaCardapioService.criar("Nome Antigo Ctrl", null, 1);

        mockMvc.perform(put("/api/categorias-cardapio/" + criada.getId())
                        .with(httpBasic("admin", SENHA_ADMIN_BOOTSTRAP))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Nome Novo Ctrl","descricao":"nova descrição","ordemExibicao":2}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Nome Novo Ctrl"));
    }

    @Test
    void adminDeveAtivarEInativarCategoria() throws Exception {
        CategoriaCardapio criada = categoriaCardapioService.criar("Toggle Ctrl", null, 1);

        mockMvc.perform(post("/api/categorias-cardapio/" + criada.getId() + "/inativar")
                        .with(httpBasic("admin", SENHA_ADMIN_BOOTSTRAP)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INATIVO"));

        mockMvc.perform(post("/api/categorias-cardapio/" + criada.getId() + "/ativar")
                        .with(httpBasic("admin", SENHA_ADMIN_BOOTSTRAP)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ATIVO"));
    }
}
