package com.curso.restaurante.api.comanda;

import com.curso.restaurante.domain.cardapio.CategoriaCardapio;
import com.curso.restaurante.domain.cardapio.ItemCardapio;
import com.curso.restaurante.domain.cardapio.SecaoPreparo;
import com.curso.restaurante.domain.comanda.Comanda;
import com.curso.restaurante.domain.comanda.TipoAtendimento;
import com.curso.restaurante.domain.pedido.Pedido;
import com.curso.restaurante.domain.usuario.PerfilUsuario;
import com.curso.restaurante.repository.cardapio.CategoriaCardapioRepository;
import com.curso.restaurante.repository.cardapio.ItemCardapioRepository;
import com.curso.restaurante.service.cliente.ClienteService;
import com.curso.restaurante.service.comanda.ComandaService;
import com.curso.restaurante.service.mesa.MesaService;
import com.curso.restaurante.service.pedido.PedidoService;
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
import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ComandaControllerTest {

    private static final String SENHA_ADMIN_BOOTSTRAP = "admin123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ComandaService comandaService;

    @Autowired
    private MesaService mesaService;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private CategoriaCardapioRepository categoriaCardapioRepository;

    @Autowired
    private ItemCardapioRepository itemCardapioRepository;

    @Test
    void garcomDeveAbrirComanda() throws Exception {
        usuarioService.criar("Garçom Abrir Ctrl", "garcom.abrir.comanda.ctrl", "senha", PerfilUsuario.GARCOM);
        var mesa = mesaService.criar(901, 4, "Salão Ctrl Abrir");

        mockMvc.perform(post("/api/comandas")
                        .with(httpBasic("garcom.abrir.comanda.ctrl", "senha"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tipoAtendimento":"SALAO","mesaId":%d,"clienteId":null,"numeroPessoas":2,"percentualTaxaServico":10.00,"observacao":null}
                                """.formatted(mesa.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ABERTA"));
    }

    @Test
    void cozinhaNaoDeveAbrirComanda() throws Exception {
        usuarioService.criar("Cozinha Abrir Ctrl", "cozinha.abrir.comanda.ctrl", "senha", PerfilUsuario.COZINHA);
        var mesa = mesaService.criar(902, 4, "Salão Ctrl Sem Permissao");

        mockMvc.perform(post("/api/comandas")
                        .with(httpBasic("cozinha.abrir.comanda.ctrl", "senha"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tipoAtendimento":"SALAO","mesaId":%d,"clienteId":null,"numeroPessoas":2,"percentualTaxaServico":0,"observacao":null}
                                """.formatted(mesa.getId())))
                .andExpect(status().isForbidden());
    }

    @Test
    void qualquerAutenticadoDeveListarEBuscarComanda() throws Exception {
        usuarioService.criar("Cozinha Lista Comanda Ctrl", "cozinha.lista.comanda.ctrl", "senha", PerfilUsuario.COZINHA);
        var responsavel = usuarioService.criar("Garçom Lista Comanda Ctrl", "garcom.lista.comanda.ctrl", "senha", PerfilUsuario.GARCOM);
        var mesa = mesaService.criar(903, 4, "Salão Ctrl Listar");
        Comanda comanda = comandaService.abrir(
                TipoAtendimento.SALAO, mesa.getId(), null, responsavel.getUsername(), 2, BigDecimal.ZERO, null);

        mockMvc.perform(get("/api/comandas").with(httpBasic("cozinha.lista.comanda.ctrl", "senha")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/comandas/" + comanda.getId()).with(httpBasic("cozinha.lista.comanda.ctrl", "senha")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value(comanda.getCodigo()));
    }

    @Test
    void qualquerAutenticadoDeveConsultarAConta() throws Exception {
        usuarioService.criar("Cozinha Conta Ctrl", "cozinha.conta.ctrl", "senha", PerfilUsuario.COZINHA);
        var responsavel = usuarioService.criar("Garçom Conta Ctrl", "garcom.conta.ctrl", "senha", PerfilUsuario.GARCOM);
        var mesa = mesaService.criar(910, 4, "Salão Ctrl Conta");
        Comanda comanda = comandaService.abrir(
                TipoAtendimento.SALAO, mesa.getId(), null, responsavel.getUsername(), 2, new BigDecimal("10.00"), null);
        entregarUmPedido(comanda, responsavel.getUsername());

        mockMvc.perform(get("/api/comandas/" + comanda.getId() + "/conta")
                        .with(httpBasic("cozinha.conta.ctrl", "senha")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subtotal").value(9.0))
                .andExpect(jsonPath("$.taxaServico").value(0.9))
                .andExpect(jsonPath("$.total").value(9.9))
                .andExpect(jsonPath("$.totalPago").value(0))
                .andExpect(jsonPath("$.saldoDevedor").value(9.9));
    }

    @Test
    void garcomDeveFecharComanda() throws Exception {
        var responsavel = usuarioService.criar("Garçom Fechar Ctrl", "garcom.fechar.comanda.ctrl", "senha", PerfilUsuario.GARCOM);
        var mesa = mesaService.criar(904, 4, "Salão Ctrl Fechar");
        Comanda comanda = comandaService.abrir(
                TipoAtendimento.SALAO, mesa.getId(), null, responsavel.getUsername(), 2, BigDecimal.ZERO, null);
        entregarUmPedido(comanda, responsavel.getUsername());

        mockMvc.perform(post("/api/comandas/" + comanda.getId() + "/fechar")
                        .with(httpBasic("garcom.fechar.comanda.ctrl", "senha")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FECHADA"));
    }

    @Test
    void garcomNaoDeveReabrirComanda() throws Exception {
        usuarioService.criar("Garçom Reabrir Ctrl", "garcom.reabrir.comanda.ctrl", "senha", PerfilUsuario.GARCOM);
        var responsavel = usuarioService.criar("Garçom Resp Reabrir Ctrl", "garcom.resp.reabrir.comanda.ctrl", "senha", PerfilUsuario.GARCOM);
        var mesa = mesaService.criar(905, 4, "Salão Ctrl Reabrir");
        Comanda comanda = comandaService.abrir(
                TipoAtendimento.SALAO, mesa.getId(), null, responsavel.getUsername(), 2, BigDecimal.ZERO, null);
        entregarUmPedido(comanda, responsavel.getUsername());
        comandaService.fechar(comanda.getId());

        mockMvc.perform(post("/api/comandas/" + comanda.getId() + "/reabrir")
                        .with(httpBasic("garcom.reabrir.comanda.ctrl", "senha")))
                .andExpect(status().isForbidden());
    }

    @Test
    void caixaDeveReabrirComanda() throws Exception {
        usuarioService.criar("Caixa Reabrir Ctrl", "caixa.reabrir.comanda.ctrl", "senha", PerfilUsuario.CAIXA);
        var responsavel = usuarioService.criar("Garçom Resp Reabrir2 Ctrl", "garcom.resp.reabrir2.comanda.ctrl", "senha", PerfilUsuario.GARCOM);
        var mesa = mesaService.criar(906, 4, "Salão Ctrl Reabrir2");
        Comanda comanda = comandaService.abrir(
                TipoAtendimento.SALAO, mesa.getId(), null, responsavel.getUsername(), 2, BigDecimal.ZERO, null);
        entregarUmPedido(comanda, responsavel.getUsername());
        comandaService.fechar(comanda.getId());

        mockMvc.perform(post("/api/comandas/" + comanda.getId() + "/reabrir")
                        .with(httpBasic("caixa.reabrir.comanda.ctrl", "senha")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ABERTA"));
    }

    @Test
    void adminDeveCancelarComanda() throws Exception {
        var responsavel = usuarioService.criar("Garçom Resp Cancelar Ctrl", "garcom.resp.cancelar.comanda.ctrl", "senha", PerfilUsuario.GARCOM);
        var mesa = mesaService.criar(907, 4, "Salão Ctrl Cancelar");
        Comanda comanda = comandaService.abrir(
                TipoAtendimento.SALAO, mesa.getId(), null, responsavel.getUsername(), 2, BigDecimal.ZERO, null);

        mockMvc.perform(post("/api/comandas/" + comanda.getId() + "/cancelar")
                        .with(httpBasic("admin", SENHA_ADMIN_BOOTSTRAP))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"motivo":"Cliente desistiu"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADA"));
    }

    @Test
    void garcomDeveTransferirMesa() throws Exception {
        var responsavel = usuarioService.criar("Garçom Resp Transf Ctrl", "garcom.resp.transf.comanda.ctrl", "senha", PerfilUsuario.GARCOM);
        var mesaOrigem = mesaService.criar(908, 4, "Salão Ctrl Origem");
        var mesaDestino = mesaService.criar(909, 4, "Salão Ctrl Destino");
        Comanda comanda = comandaService.abrir(
                TipoAtendimento.SALAO, mesaOrigem.getId(), null, responsavel.getUsername(), 2, BigDecimal.ZERO, null);

        mockMvc.perform(post("/api/comandas/" + comanda.getId() + "/transferir-mesa")
                        .with(httpBasic("garcom.resp.transf.comanda.ctrl", "senha"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"mesaDestinoId":%d}
                                """.formatted(mesaDestino.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mesaId").value(mesaDestino.getId()));
    }

    private void entregarUmPedido(Comanda comanda, String usernameSolicitante) {
        Pedido pedido = pedidoService.criarPedido(comanda.getId(), usernameSolicitante, null);
        ItemCardapio itemCardapio = umItemCardapio();
        pedidoService.adicionarItem(pedido.getId(), itemCardapio.getId(), 1, null);
        pedidoService.enviarParaPreparo(pedido.getId());
        pedidoService.marcarComoPronto(pedido.getId());
        pedidoService.marcarComoEntregue(pedido.getId());
    }

    private ItemCardapio umItemCardapio() {
        CategoriaCardapio categoria = categoriaCardapioRepository.save(
                new CategoriaCardapio("Categoria Comanda Ctrl " + Math.random(), null, 1));
        ItemCardapio item = new ItemCardapio(
                "ITEM-COMANDA-CTRL-" + Math.random(), "Item Comanda Ctrl", null, new BigDecimal("9.00"), 5,
                SecaoPreparo.COZINHA, true, false, BigDecimal.ZERO, LocalDate.of(2026, 8, 20));
        categoria.adicionarItem(item);
        return itemCardapioRepository.save(item);
    }
}
