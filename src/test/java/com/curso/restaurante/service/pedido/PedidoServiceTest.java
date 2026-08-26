package com.curso.restaurante.service.pedido;

import com.curso.restaurante.domain.cardapio.CategoriaCardapio;
import com.curso.restaurante.domain.cardapio.ItemCardapio;
import com.curso.restaurante.domain.cardapio.SecaoPreparo;
import com.curso.restaurante.domain.comanda.Comanda;
import com.curso.restaurante.domain.comum.RecursoNaoEncontradoException;
import com.curso.restaurante.domain.comum.RegraDeNegocioException;
import com.curso.restaurante.domain.pedido.ItemPedido;
import com.curso.restaurante.domain.pedido.Pedido;
import com.curso.restaurante.domain.pedido.StatusPedido;
import com.curso.restaurante.domain.usuario.PerfilUsuario;
import com.curso.restaurante.repository.cardapio.CategoriaCardapioRepository;
import com.curso.restaurante.repository.cardapio.ItemCardapioRepository;
import com.curso.restaurante.service.comanda.ComandaService;
import com.curso.restaurante.service.usuario.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PedidoServiceTest {

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private ComandaService comandaService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private CategoriaCardapioRepository categoriaCardapioRepository;

    @Autowired
    private ItemCardapioRepository itemCardapioRepository;

    @Test
    void deveCriarPedidoVinculadoAComanda() {
        Comanda comanda = umaComandaAberta();
        var solicitante = usuarioService.buscarPorUsername(comanda.getResponsavel().getUsername());

        Pedido pedido = pedidoService.criarPedido(comanda.getId(), solicitante.getUsername(), "sem pressa");

        assertEquals(StatusPedido.ABERTO, pedido.getStatus());
        assertTrue(pedido.getCodigo().startsWith("PED-"));
    }

    @Test
    void buscarPorIdDeveLancarQuandoNaoExiste() {
        assertThrows(RecursoNaoEncontradoException.class, () -> pedidoService.buscarPorId(-1L));
    }

    @Test
    void deveAdicionarERemoverItem() {
        Comanda comanda = umaComandaAberta();
        Pedido pedido = pedidoService.criarPedido(
                comanda.getId(), comanda.getResponsavel().getUsername(), null);
        ItemCardapio itemCardapio = umItemCardapio("9.00", true, new BigDecimal("10.000"));

        ItemPedido item = pedidoService.adicionarItem(pedido.getId(), itemCardapio.getId(), 2, "sem gelo");
        Pedido recarregado = pedidoService.buscarPorId(pedido.getId());
        assertEquals(1, recarregado.getItens().size());

        pedidoService.removerItem(pedido.getId(), item.getId());
        Pedido semItens = pedidoService.buscarPorId(pedido.getId());
        assertTrue(semItens.getItens().isEmpty());
    }

    @Test
    void enviarParaPreparoDeveBaixarEstoqueQuandoItemControlaEstoque() {
        Comanda comanda = umaComandaAberta();
        Pedido pedido = pedidoService.criarPedido(
                comanda.getId(), comanda.getResponsavel().getUsername(), null);
        ItemCardapio itemCardapio = umItemCardapio("9.00", true, new BigDecimal("10.000"));
        pedidoService.adicionarItem(pedido.getId(), itemCardapio.getId(), 3, null);

        pedidoService.enviarParaPreparo(pedido.getId());

        ItemCardapio recarregado = itemCardapioRepository.findById(itemCardapio.getId()).orElseThrow();
        assertEquals(0, new BigDecimal("7.000").compareTo(recarregado.getSaldoEstoque()));
    }

    @Test
    void enviarParaPreparoDeveRejeitarQuandoSaldoInsuficiente() {
        Comanda comanda = umaComandaAberta();
        Pedido pedido = pedidoService.criarPedido(
                comanda.getId(), comanda.getResponsavel().getUsername(), null);
        ItemCardapio itemCardapio = umItemCardapio("9.00", true, new BigDecimal("2.000"));
        pedidoService.adicionarItem(pedido.getId(), itemCardapio.getId(), 5, null);

        assertThrows(RegraDeNegocioException.class, () -> pedidoService.enviarParaPreparo(pedido.getId()));
    }

    @Test
    void cancelarDeveDevolverEstoqueQuandoJaHaviaSidoDeduzido() {
        Comanda comanda = umaComandaAberta();
        Pedido pedido = pedidoService.criarPedido(
                comanda.getId(), comanda.getResponsavel().getUsername(), null);
        ItemCardapio itemCardapio = umItemCardapio("9.00", true, new BigDecimal("10.000"));
        pedidoService.adicionarItem(pedido.getId(), itemCardapio.getId(), 4, null);
        pedidoService.enviarParaPreparo(pedido.getId());

        pedidoService.cancelar(pedido.getId(), "Cliente desistiu");

        ItemCardapio recarregado = itemCardapioRepository.findById(itemCardapio.getId()).orElseThrow();
        assertEquals(0, new BigDecimal("10.000").compareTo(recarregado.getSaldoEstoque()));
    }

    @Test
    void cancelarNaoDeveMexerNoEstoqueQuandoAindaEstavaAberto() {
        Comanda comanda = umaComandaAberta();
        Pedido pedido = pedidoService.criarPedido(
                comanda.getId(), comanda.getResponsavel().getUsername(), null);
        ItemCardapio itemCardapio = umItemCardapio("9.00", true, new BigDecimal("10.000"));
        pedidoService.adicionarItem(pedido.getId(), itemCardapio.getId(), 4, null);

        pedidoService.cancelar(pedido.getId(), "Cliente desistiu");

        ItemCardapio recarregado = itemCardapioRepository.findById(itemCardapio.getId()).orElseThrow();
        assertEquals(0, new BigDecimal("10.000").compareTo(recarregado.getSaldoEstoque()));
    }

    @Test
    void deveSeguirOFluxoAteEntregue() {
        Comanda comanda = umaComandaAberta();
        Pedido pedido = pedidoService.criarPedido(
                comanda.getId(), comanda.getResponsavel().getUsername(), null);
        ItemCardapio itemCardapio = umItemCardapio("9.00", false, BigDecimal.ZERO);
        pedidoService.adicionarItem(pedido.getId(), itemCardapio.getId(), 1, null);

        pedidoService.enviarParaPreparo(pedido.getId());
        pedidoService.marcarComoPronto(pedido.getId());
        Pedido entregue = pedidoService.marcarComoEntregue(pedido.getId());

        assertEquals(StatusPedido.ENTREGUE, entregue.getStatus());
    }

    @Autowired
    private com.curso.restaurante.service.mesa.MesaService mesaService;

    private Comanda umaComandaAberta() {
        var responsavel = usuarioService.criar(
                "Garçom Pedido Svc " + Math.random(), "garcom.pedido.svc." + Math.random(), "senha", PerfilUsuario.GARCOM);
        var mesa = mesaService.criar((int) (Math.random() * 1000000), 4, "Salão Pedido Svc");

        return comandaService.abrir(
                com.curso.restaurante.domain.comanda.TipoAtendimento.SALAO,
                mesa.getId(), null, responsavel.getUsername(), 2, BigDecimal.ZERO, null);
    }

    private ItemCardapio umItemCardapio(String precoVenda, boolean controlaEstoque, BigDecimal saldoInicial) {
        CategoriaCardapio categoria = categoriaCardapioRepository.save(
                new CategoriaCardapio("Categoria Pedido Svc " + Math.random(), null, 1));
        ItemCardapio item = new ItemCardapio(
                "ITEM-PEDIDO-SVC-" + Math.random(),
                "Item Pedido Svc",
                null,
                new BigDecimal(precoVenda),
                5,
                SecaoPreparo.COZINHA,
                true,
                controlaEstoque,
                saldoInicial,
                LocalDate.of(2026, 8, 20));
        categoria.adicionarItem(item);
        return itemCardapioRepository.save(item);
    }
}
