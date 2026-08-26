package com.curso.restaurante.api.caixa;

import com.curso.restaurante.domain.cardapio.CategoriaCardapio;
import com.curso.restaurante.domain.cardapio.ItemCardapio;
import com.curso.restaurante.domain.cardapio.SecaoPreparo;
import com.curso.restaurante.domain.comanda.Comanda;
import com.curso.restaurante.domain.comanda.TipoAtendimento;
import com.curso.restaurante.domain.pedido.Pedido;
import com.curso.restaurante.domain.usuario.PerfilUsuario;
import com.curso.restaurante.repository.cardapio.CategoriaCardapioRepository;
import com.curso.restaurante.repository.cardapio.ItemCardapioRepository;
import com.curso.restaurante.service.caixa.SessaoCaixaService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PagamentoControllerTest {

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
    private SessaoCaixaService sessaoCaixaService;

    @Autowired
    private CategoriaCardapioRepository categoriaCardapioRepository;

    @Autowired
    private ItemCardapioRepository itemCardapioRepository;

    @Test
    void caixaDeveRegistrarPagamentoEListar() throws Exception {
        var operador = usuarioService.criar("Caixa Pagto Ctrl", "caixa.pagto.ctrl", "senha", PerfilUsuario.CAIXA);
        sessaoCaixaService.abrir(operador.getUsername(), BigDecimal.ZERO);
        Comanda comanda = umaComandaFechada();

        mockMvc.perform(post("/api/comandas/" + comanda.getId() + "/pagamentos")
                        .with(httpBasic("caixa.pagto.ctrl", "senha"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"formaPagamento":"DINHEIRO","valor":40.00,"valorRecebido":50.00}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.troco").value(10.0));

        mockMvc.perform(get("/api/comandas/" + comanda.getId() + "/pagamentos")
                        .with(httpBasic("caixa.pagto.ctrl", "senha")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void garcomNaoDeveRegistrarPagamento() throws Exception {
        usuarioService.criar("Garçom Pagto Ctrl", "garcom.pagto.ctrl", "senha", PerfilUsuario.GARCOM);
        Comanda comanda = umaComandaFechada();

        mockMvc.perform(post("/api/comandas/" + comanda.getId() + "/pagamentos")
                        .with(httpBasic("garcom.pagto.ctrl", "senha"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"formaPagamento":"PIX","valor":40.00,"valorRecebido":null}
                                """))
                .andExpect(status().isForbidden());
    }

    private Comanda umaComandaFechada() {
        var responsavel = usuarioService.criar(
                "Garçom Pagto Ctrl Resp " + Math.random(), "garcom.pagto.ctrl.resp." + Math.random(), "senha", PerfilUsuario.GARCOM);
        var mesa = mesaService.criar((int) (Math.random() * 1000000), 4, "Salão Pagto Ctrl");
        Comanda comanda = comandaService.abrir(
                TipoAtendimento.SALAO, mesa.getId(), null, responsavel.getUsername(), 2, BigDecimal.ZERO, null);
        Pedido pedido = pedidoService.criarPedido(comanda.getId(), responsavel.getUsername(), null);

        CategoriaCardapio categoria = categoriaCardapioRepository.save(
                new CategoriaCardapio("Categoria Pagto Ctrl " + Math.random(), null, 1));
        ItemCardapio item = new ItemCardapio(
                "ITEM-PAGTO-CTRL-" + Math.random(), "Item Pagto Ctrl", null, new BigDecimal("40.00"), 5,
                SecaoPreparo.BAR, false, false, BigDecimal.ZERO, LocalDate.of(2026, 8, 20));
        categoria.adicionarItem(item);
        item = itemCardapioRepository.save(item);

        pedidoService.adicionarItem(pedido.getId(), item.getId(), 1, null);
        pedidoService.enviarParaPreparo(pedido.getId());
        pedidoService.marcarComoEntregue(pedidoService.marcarComoPronto(pedido.getId()).getId());
        return comandaService.fechar(comanda.getId());
    }
}
