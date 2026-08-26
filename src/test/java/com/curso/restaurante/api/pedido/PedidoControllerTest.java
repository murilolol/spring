package com.curso.restaurante.api.pedido;

import com.curso.restaurante.domain.cardapio.CategoriaCardapio;
import com.curso.restaurante.domain.cardapio.ItemCardapio;
import com.curso.restaurante.domain.cardapio.SecaoPreparo;
import com.curso.restaurante.domain.comanda.Comanda;
import com.curso.restaurante.domain.comanda.TipoAtendimento;
import com.curso.restaurante.domain.pedido.ItemPedido;
import com.curso.restaurante.domain.pedido.Pedido;
import com.curso.restaurante.domain.usuario.PerfilUsuario;
import com.curso.restaurante.repository.cardapio.CategoriaCardapioRepository;
import com.curso.restaurante.repository.cardapio.ItemCardapioRepository;
import com.curso.restaurante.service.comanda.ComandaService;
import com.curso.restaurante.service.mesa.MesaService;
import com.curso.restaurante.service.pedido.PedidoService;
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
import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PedidoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ComandaService comandaService;

    @Autowired
    private MesaService mesaService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private CategoriaCardapioRepository categoriaCardapioRepository;

    @Autowired
    private ItemCardapioRepository itemCardapioRepository;

    @Test
    void garcomDeveCriarPedidoNaComanda() throws Exception {
        var responsavel = usuarioService.criar("Garçom Pedido Ctrl", "garcom.pedido.ctrl", "senha", PerfilUsuario.GARCOM);
        var mesa = mesaService.criar(1101, 4, "Salão Pedido Ctrl");
        Comanda comanda = comandaService.abrir(
                TipoAtendimento.SALAO, mesa.getId(), null, responsavel.getUsername(), 2, BigDecimal.ZERO, null);

        mockMvc.perform(post("/api/comandas/" + comanda.getId() + "/pedidos")
                        .with(httpBasic("garcom.pedido.ctrl", "senha"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"observacao":"sem pressa"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.status").value("ABERTO"));
    }

    @Test
    void cozinhaNaoDeveCriarPedido() throws Exception {
        usuarioService.criar("Cozinha Pedido Ctrl", "cozinha.pedido.ctrl", "senha", PerfilUsuario.COZINHA);
        var responsavel = usuarioService.criar("Garçom Resp Pedido Ctrl", "garcom.resp.pedido.ctrl", "senha", PerfilUsuario.GARCOM);
        var mesa = mesaService.criar(1102, 4, "Salão Pedido Ctrl Sem Permissao");
        Comanda comanda = comandaService.abrir(
                TipoAtendimento.SALAO, mesa.getId(), null, responsavel.getUsername(), 2, BigDecimal.ZERO, null);

        mockMvc.perform(post("/api/comandas/" + comanda.getId() + "/pedidos")
                        .with(httpBasic("cozinha.pedido.ctrl", "senha"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"observacao":null}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void qualquerAutenticadoDeveListarEBuscarPedido() throws Exception {
        usuarioService.criar("Cozinha Lista Pedido Ctrl", "cozinha.lista.pedido.ctrl", "senha", PerfilUsuario.COZINHA);
        var responsavel = usuarioService.criar("Garçom Lista Pedido Ctrl", "garcom.lista.pedido.ctrl", "senha", PerfilUsuario.GARCOM);
        var mesa = mesaService.criar(1103, 4, "Salão Lista Pedido Ctrl");
        Comanda comanda = comandaService.abrir(
                TipoAtendimento.SALAO, mesa.getId(), null, responsavel.getUsername(), 2, BigDecimal.ZERO, null);
        Pedido pedido = pedidoService.criarPedido(comanda.getId(), responsavel.getUsername(), null);

        mockMvc.perform(get("/api/pedidos").with(httpBasic("cozinha.lista.pedido.ctrl", "senha")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/pedidos/" + pedido.getId()).with(httpBasic("cozinha.lista.pedido.ctrl", "senha")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value(pedido.getCodigo()));
    }

    @Test
    void garcomDeveAdicionarERemoverItem() throws Exception {
        var responsavel = usuarioService.criar("Garçom Item Pedido Ctrl", "garcom.item.pedido.ctrl", "senha", PerfilUsuario.GARCOM);
        var mesa = mesaService.criar(1104, 4, "Salão Item Pedido Ctrl");
        Comanda comanda = comandaService.abrir(
                TipoAtendimento.SALAO, mesa.getId(), null, responsavel.getUsername(), 2, BigDecimal.ZERO, null);
        Pedido pedido = pedidoService.criarPedido(comanda.getId(), responsavel.getUsername(), null);
        ItemCardapio itemCardapio = umItemCardapio();

        String resposta = mockMvc.perform(post("/api/pedidos/" + pedido.getId() + "/itens")
                        .with(httpBasic("garcom.item.pedido.ctrl", "senha"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"itemCardapioId":%d,"quantidade":2,"observacao":"sem gelo"}
                                """.formatted(itemCardapio.getId())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Number idNaResposta = com.jayway.jsonpath.JsonPath.read(resposta, "$.id");
        Long itemPedidoId = idNaResposta.longValue();

        mockMvc.perform(delete("/api/pedidos/" + pedido.getId() + "/itens/" + itemPedidoId)
                        .with(httpBasic("garcom.item.pedido.ctrl", "senha")))
                .andExpect(status().isNoContent());
    }

    @Test
    void deveSeguirOFluxoDeTransicoesComOsPapeisCorretos() throws Exception {
        usuarioService.criar("Cozinha Fluxo Ctrl", "cozinha.fluxo.ctrl", "senha", PerfilUsuario.COZINHA);
        var responsavel = usuarioService.criar("Garçom Fluxo Ctrl", "garcom.fluxo.ctrl", "senha", PerfilUsuario.GARCOM);
        var mesa = mesaService.criar(1105, 4, "Salão Fluxo Ctrl");
        Comanda comanda = comandaService.abrir(
                TipoAtendimento.SALAO, mesa.getId(), null, responsavel.getUsername(), 2, BigDecimal.ZERO, null);
        Pedido pedido = pedidoService.criarPedido(comanda.getId(), responsavel.getUsername(), null);
        ItemCardapio itemCardapio = umItemCardapio();
        pedidoService.adicionarItem(pedido.getId(), itemCardapio.getId(), 1, null);

        mockMvc.perform(post("/api/pedidos/" + pedido.getId() + "/enviar-para-preparo")
                        .with(httpBasic("garcom.fluxo.ctrl", "senha")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_PREPARO"));

        mockMvc.perform(post("/api/pedidos/" + pedido.getId() + "/marcar-pronto")
                        .with(httpBasic("garcom.fluxo.ctrl", "senha")))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/pedidos/" + pedido.getId() + "/marcar-pronto")
                        .with(httpBasic("cozinha.fluxo.ctrl", "senha")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PRONTO"));

        mockMvc.perform(post("/api/pedidos/" + pedido.getId() + "/marcar-entregue")
                        .with(httpBasic("garcom.fluxo.ctrl", "senha")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ENTREGUE"));
    }

    @Test
    void caixaDeveCancelarPedido() throws Exception {
        usuarioService.criar("Caixa Cancela Pedido Ctrl", "caixa.cancela.pedido.ctrl", "senha", PerfilUsuario.CAIXA);
        var responsavel = usuarioService.criar("Garçom Cancela Pedido Ctrl", "garcom.cancela.pedido.ctrl", "senha", PerfilUsuario.GARCOM);
        var mesa = mesaService.criar(1106, 4, "Salão Cancela Pedido Ctrl");
        Comanda comanda = comandaService.abrir(
                TipoAtendimento.SALAO, mesa.getId(), null, responsavel.getUsername(), 2, BigDecimal.ZERO, null);
        Pedido pedido = pedidoService.criarPedido(comanda.getId(), responsavel.getUsername(), null);

        mockMvc.perform(post("/api/pedidos/" + pedido.getId() + "/cancelar")
                        .with(httpBasic("caixa.cancela.pedido.ctrl", "senha"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"motivo":"Item indisponível"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADO"));
    }

    private ItemCardapio umItemCardapio() {
        CategoriaCardapio categoria = categoriaCardapioRepository.save(
                new CategoriaCardapio("Categoria Pedido Ctrl " + Math.random(), null, 1));
        ItemCardapio item = new ItemCardapio(
                "ITEM-PEDIDO-CTRL-" + Math.random(), "Item Pedido Ctrl", null, new BigDecimal("9.00"), 5,
                SecaoPreparo.COZINHA, true, false, BigDecimal.ZERO, LocalDate.of(2026, 8, 20));
        categoria.adicionarItem(item);
        return itemCardapioRepository.save(item);
    }
}
