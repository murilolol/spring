package com.curso.restaurante.api.cliente;

import com.curso.restaurante.domain.cliente.Cliente;
import com.curso.restaurante.domain.comanda.TipoAtendimento;
import com.curso.restaurante.domain.usuario.PerfilUsuario;
import com.curso.restaurante.service.cliente.ClienteService;
import com.curso.restaurante.service.comanda.ComandaService;
import com.curso.restaurante.service.usuario.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ClienteControllerTest {

    private static final String SENHA_ADMIN_BOOTSTRAP = "admin123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ComandaService comandaService;

    @Test
    void garcomDeveCriarCliente() throws Exception {
        usuarioService.criar("Garçom Cliente Ctrl", "garcom.cliente.ctrl", "senha", PerfilUsuario.GARCOM);

        mockMvc.perform(post("/api/clientes")
                        .with(httpBasic("garcom.cliente.ctrl", "senha"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Cliente Novo Ctrl","documento":null,"telefone":"(45) 93333-0001","email":null,"endereco":null,"dataNascimento":null}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.nome").value("Cliente Novo Ctrl"));
    }

    @Test
    void cozinhaNaoDeveCriarCliente() throws Exception {
        usuarioService.criar("Cozinha Cliente Ctrl", "cozinha.cliente.ctrl", "senha", PerfilUsuario.COZINHA);

        mockMvc.perform(post("/api/clientes")
                        .with(httpBasic("cozinha.cliente.ctrl", "senha"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Outro Ctrl","documento":null,"telefone":"(45) 93333-0002","email":null,"endereco":null,"dataNascimento":null}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void semCredencialDeveRetornar401() throws Exception {
        mockMvc.perform(get("/api/clientes"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void qualquerAutenticadoDeveListarClientes() throws Exception {
        usuarioService.criar("Cozinha Lista Ctrl", "cozinha.lista.ctrl", "senha", PerfilUsuario.COZINHA);

        mockMvc.perform(get("/api/clientes").with(httpBasic("cozinha.lista.ctrl", "senha")))
                .andExpect(status().isOk());
    }

    @Test
    void adminDeveBuscarClientePorId() throws Exception {
        Cliente criado = clienteService.criar("Buscar Id Ctrl", null, "(45) 93333-0003", null, null, null);

        mockMvc.perform(get("/api/clientes/" + criado.getId())
                        .with(httpBasic("admin", SENHA_ADMIN_BOOTSTRAP)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Buscar Id Ctrl"));
    }

    @Test
    void adminDeveReceber404ParaClienteInexistente() throws Exception {
        mockMvc.perform(get("/api/clientes/999999")
                        .with(httpBasic("admin", SENHA_ADMIN_BOOTSTRAP)))
                .andExpect(status().isNotFound());
    }

    @Test
    void caixaDeveAtualizarCliente() throws Exception {
        usuarioService.criar("Caixa Atualiza Ctrl", "caixa.atualiza.ctrl", "senha", PerfilUsuario.CAIXA);
        Cliente criado = clienteService.criar("Nome Original Ctrl", null, "(45) 93333-0004", null, null, null);

        mockMvc.perform(put("/api/clientes/" + criado.getId())
                        .with(httpBasic("caixa.atualiza.ctrl", "senha"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Nome Atualizado Ctrl","telefone":"(45) 94444-0004","email":null,"endereco":null}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Nome Atualizado Ctrl"));
    }

    @Test
    void adminDeveAtivarEInativarCliente() throws Exception {
        Cliente criado = clienteService.criar("Toggle Ctrl", null, "(45) 93333-0005", null, null, null);

        mockMvc.perform(post("/api/clientes/" + criado.getId() + "/inativar")
                        .with(httpBasic("admin", SENHA_ADMIN_BOOTSTRAP)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INATIVO"));

        mockMvc.perform(post("/api/clientes/" + criado.getId() + "/ativar")
                        .with(httpBasic("admin", SENHA_ADMIN_BOOTSTRAP)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ATIVO"));
    }

    @Test
    void qualquerAutenticadoDeveListarComandasDoCliente() throws Exception {
        usuarioService.criar("Cozinha Comandas Cliente Ctrl", "cozinha.comandas.cliente.ctrl", "senha", PerfilUsuario.COZINHA);
        var responsavel = usuarioService.criar("Garçom Comandas Cliente Ctrl", "garcom.comandas.cliente.ctrl", "senha", PerfilUsuario.GARCOM);
        Cliente cliente = clienteService.criar("Cliente Com Comandas Ctrl", null, "(45) 95555-0001", null, null, null);
        comandaService.abrir(
                TipoAtendimento.DELIVERY, null, cliente.getId(), responsavel.getUsername(), 1,
                BigDecimal.ZERO, null);

        mockMvc.perform(get("/api/clientes/" + cliente.getId() + "/comandas")
                        .with(httpBasic("cozinha.comandas.cliente.ctrl", "senha")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo.length()").value(1));
    }

    @Test
    void garcomNaoDeveAtivarCliente() throws Exception {
        usuarioService.criar("Garçom Sem Permissao Ctrl", "garcom.sem.permissao.ctrl", "senha", PerfilUsuario.GARCOM);
        Cliente criado = clienteService.criar("Sem Permissao Ctrl", null, "(45) 93333-0006", null, null, null);

        mockMvc.perform(post("/api/clientes/" + criado.getId() + "/inativar")
                        .with(httpBasic("garcom.sem.permissao.ctrl", "senha")))
                .andExpect(status().isForbidden());
    }
}
