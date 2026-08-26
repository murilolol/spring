package com.curso.restaurante.api.caixa;

import com.curso.restaurante.domain.caixa.SessaoCaixa;
import com.curso.restaurante.domain.usuario.PerfilUsuario;
import com.curso.restaurante.service.caixa.SessaoCaixaService;
import com.curso.restaurante.service.usuario.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SessaoCaixaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SessaoCaixaService sessaoCaixaService;

    @Autowired
    private UsuarioService usuarioService;

    @Test
    void caixaDeveAbrirSessao() throws Exception {
        usuarioService.criar("Caixa Abrir Ctrl", "caixa.abrir.ctrl", "senha", PerfilUsuario.CAIXA);

        mockMvc.perform(post("/api/caixa/sessoes")
                        .with(httpBasic("caixa.abrir.ctrl", "senha"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"valorAbertura":100.00}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ABERTA"));
    }

    @Test
    void garcomNaoDeveAbrirSessao() throws Exception {
        usuarioService.criar("Garçom Caixa Ctrl", "garcom.caixa.ctrl", "senha", PerfilUsuario.GARCOM);

        mockMvc.perform(post("/api/caixa/sessoes")
                        .with(httpBasic("garcom.caixa.ctrl", "senha"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"valorAbertura":100.00}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void caixaDeveBuscarSessaoAberta() throws Exception {
        var operador = usuarioService.criar("Caixa Aberta Ctrl", "caixa.aberta.ctrl", "senha", PerfilUsuario.CAIXA);
        sessaoCaixaService.abrir(operador.getUsername(), BigDecimal.ZERO);

        mockMvc.perform(get("/api/caixa/sessoes/aberta").with(httpBasic("caixa.aberta.ctrl", "senha")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ABERTA"));
    }

    @Test
    void caixaDeveRegistrarSangria() throws Exception {
        var operador = usuarioService.criar("Caixa Sangria Ctrl", "caixa.sangria.ctrl", "senha", PerfilUsuario.CAIXA);
        SessaoCaixa sessao = sessaoCaixaService.abrir(operador.getUsername(), new BigDecimal("100.00"));

        mockMvc.perform(post("/api/caixa/sessoes/" + sessao.getId() + "/sangrias")
                        .with(httpBasic("caixa.sangria.ctrl", "senha"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"valor":30.00,"motivo":"Reforço de troco"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.valor").value(30.0));
    }

    @Test
    void caixaDeveListarPagamentosDaSessao() throws Exception {
        var operador = usuarioService.criar("Caixa Lista Pagto Ctrl", "caixa.lista.pagto.ctrl", "senha", PerfilUsuario.CAIXA);
        SessaoCaixa sessao = sessaoCaixaService.abrir(operador.getUsername(), BigDecimal.ZERO);

        mockMvc.perform(get("/api/caixa/sessoes/" + sessao.getId() + "/pagamentos")
                        .with(httpBasic("caixa.lista.pagto.ctrl", "senha")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo").isArray());
    }

    @Test
    void caixaDeveFecharSessao() throws Exception {
        var operador = usuarioService.criar("Caixa Fechar Ctrl", "caixa.fechar.ctrl", "senha", PerfilUsuario.CAIXA);
        SessaoCaixa sessao = sessaoCaixaService.abrir(operador.getUsername(), new BigDecimal("100.00"));

        mockMvc.perform(post("/api/caixa/sessoes/" + sessao.getId() + "/fechar")
                        .with(httpBasic("caixa.fechar.ctrl", "senha"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"valorContado":100.00,"observacao":"Confere"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FECHADA"));
    }
}
