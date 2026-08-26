package com.curso.restaurante.service.caixa;

import com.curso.restaurante.domain.caixa.FormaPagamento;
import com.curso.restaurante.domain.caixa.Pagamento;
import com.curso.restaurante.domain.comanda.Comanda;
import com.curso.restaurante.domain.comanda.StatusComanda;
import com.curso.restaurante.domain.comanda.TipoAtendimento;
import com.curso.restaurante.domain.comum.ConflitoDeEstadoException;
import com.curso.restaurante.domain.comum.RegraDeNegocioException;
import com.curso.restaurante.domain.pedido.Pedido;
import com.curso.restaurante.domain.pedido.StatusPedido;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PagamentoServiceTest {

    @Autowired
    private PagamentoService pagamentoService;

    @Autowired
    private SessaoCaixaService sessaoCaixaService;

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
    void naoDeveRegistrarPagamentoSemSessaoDeCaixaAberta() {
        Comanda comanda = umaComandaFechadaComTotal("40.00");

        assertThrows(
                ConflitoDeEstadoException.class,
                () -> pagamentoService.registrarPagamento(
                        comanda.getId(), FormaPagamento.PIX, new BigDecimal("40.00"), null,
                        comanda.getResponsavel().getUsername()));
    }

    @Test
    void deveRegistrarPagamentoIntegralEMarcarComandaComoPaga() {
        var operador = usuarioService.criar(
                "Caixa Pagto Svc " + Math.random(), "caixa.pagto.svc." + Math.random(), "senha", PerfilUsuario.CAIXA);
        sessaoCaixaService.abrir(operador.getUsername(), BigDecimal.ZERO);
        Comanda comanda = umaComandaFechadaComTotal("40.00");

        Pagamento pagamento = pagamentoService.registrarPagamento(
                comanda.getId(), FormaPagamento.PIX, new BigDecimal("40.00"), null, operador.getUsername());

        assertEquals(0, new BigDecimal("40.00").compareTo(pagamento.getValor()));
        Comanda recarregada = comandaService.buscarPorId(comanda.getId());
        assertEquals(StatusComanda.PAGA, recarregada.getStatus());
    }

    @Test
    void devePermitirPagamentoDivididoEDeixarSaldoDevedorAteQuitar() {
        var operador = usuarioService.criar(
                "Caixa Pagto Dividido Svc " + Math.random(), "caixa.pagto.dividido.svc." + Math.random(), "senha", PerfilUsuario.CAIXA);
        sessaoCaixaService.abrir(operador.getUsername(), BigDecimal.ZERO);
        Comanda comanda = umaComandaFechadaComTotal("40.00");

        pagamentoService.registrarPagamento(
                comanda.getId(), FormaPagamento.PIX, new BigDecimal("15.00"), null, operador.getUsername());
        Comanda aindaFechada = comandaService.buscarPorId(comanda.getId());
        assertEquals(StatusComanda.FECHADA, aindaFechada.getStatus());

        pagamentoService.registrarPagamento(
                comanda.getId(), FormaPagamento.CARTAO_DEBITO, new BigDecimal("25.00"), null, operador.getUsername());
        Comanda paga = comandaService.buscarPorId(comanda.getId());
        assertEquals(StatusComanda.PAGA, paga.getStatus());
    }

    @Test
    void naoDeveRegistrarPagamentoAcimaDoSaldoDevedor() {
        var operador = usuarioService.criar(
                "Caixa Pagto Excede Svc " + Math.random(), "caixa.pagto.excede.svc." + Math.random(), "senha", PerfilUsuario.CAIXA);
        sessaoCaixaService.abrir(operador.getUsername(), BigDecimal.ZERO);
        Comanda comanda = umaComandaFechadaComTotal("40.00");

        assertThrows(
                RegraDeNegocioException.class,
                () -> pagamentoService.registrarPagamento(
                        comanda.getId(), FormaPagamento.PIX, new BigDecimal("41.00"), null, operador.getUsername()));
    }

    @Test
    void pagamentoIntegralDeveMarcarPedidosEntreguesComoPagos() {
        var operador = usuarioService.criar(
                "Caixa Pagto Cascata Svc " + Math.random(), "caixa.pagto.cascata.svc." + Math.random(), "senha", PerfilUsuario.CAIXA);
        sessaoCaixaService.abrir(operador.getUsername(), BigDecimal.ZERO);
        var responsavel = usuarioService.criar(
                "Garçom Pagto Cascata Svc " + Math.random(), "garcom.pagto.cascata.svc." + Math.random(), "senha", PerfilUsuario.GARCOM);
        var mesa = mesaService.criar((int) (Math.random() * 1000000), 4, "Salão Pagto Cascata Svc");
        Comanda comanda = comandaService.abrir(
                TipoAtendimento.SALAO, mesa.getId(), null, responsavel.getUsername(), 2, BigDecimal.ZERO, null);
        Pedido pedido = pedidoService.criarPedido(comanda.getId(), responsavel.getUsername(), null);
        var item = umItemCardapio("40.00");
        pedidoService.adicionarItem(pedido.getId(), item.getId(), 1, null);
        pedidoService.enviarParaPreparo(pedido.getId());
        pedidoService.marcarComoPronto(pedido.getId());
        pedidoService.marcarComoEntregue(pedido.getId());
        comandaService.fechar(comanda.getId());

        pagamentoService.registrarPagamento(
                comanda.getId(), FormaPagamento.PIX, new BigDecimal("40.00"), null, operador.getUsername());

        Pedido pedidoRecarregado = pedidoService.buscarPorId(pedido.getId());
        assertEquals(StatusPedido.PAGO, pedidoRecarregado.getStatus());
    }

    @Test
    void deveListarPagamentosPorSessao() {
        var operador = usuarioService.criar(
                "Caixa Lista Pagto Svc " + Math.random(), "caixa.lista.pagto.svc." + Math.random(), "senha", PerfilUsuario.CAIXA);
        var sessao = sessaoCaixaService.abrir(operador.getUsername(), BigDecimal.ZERO);
        Comanda comanda = umaComandaFechadaComTotal("40.00");
        pagamentoService.registrarPagamento(
                comanda.getId(), FormaPagamento.PIX, new BigDecimal("40.00"), null, operador.getUsername());

        var pagina = pagamentoService.listarPagamentosPorSessao(
                sessao.getId(), null, org.springframework.data.domain.PageRequest.of(0, 50));

        assertEquals(1, pagina.getContent().size());
    }

    private Comanda umaComandaFechadaComTotal(String valorItem) {
        var responsavel = usuarioService.criar(
                "Garçom Pagto Svc " + Math.random(), "garcom.pagto.svc." + Math.random(), "senha", PerfilUsuario.GARCOM);
        var mesa = mesaService.criar((int) (Math.random() * 1000000), 4, "Salão Pagto Svc");
        Comanda comanda = comandaService.abrir(
                TipoAtendimento.SALAO, mesa.getId(), null, responsavel.getUsername(), 2, BigDecimal.ZERO, null);
        Pedido pedido = pedidoService.criarPedido(comanda.getId(), responsavel.getUsername(), null);
        var item = umItemCardapio(valorItem);
        pedidoService.adicionarItem(pedido.getId(), item.getId(), 1, null);
        pedidoService.enviarParaPreparo(pedido.getId());
        pedidoService.marcarComoPronto(pedido.getId());
        pedidoService.marcarComoEntregue(pedido.getId());
        return comandaService.fechar(comanda.getId());
    }

    private com.curso.restaurante.domain.cardapio.ItemCardapio umItemCardapio(String precoVenda) {
        var categoria = categoriaCardapioRepository.save(
                new com.curso.restaurante.domain.cardapio.CategoriaCardapio("Categoria Pagto Svc " + Math.random(), null, 1));
        var item = new com.curso.restaurante.domain.cardapio.ItemCardapio(
                "ITEM-PAGTO-SVC-" + Math.random(), "Item Pagto Svc", null, new BigDecimal(precoVenda), 5,
                com.curso.restaurante.domain.cardapio.SecaoPreparo.BAR, false, false, BigDecimal.ZERO,
                LocalDate.of(2026, 8, 20));
        categoria.adicionarItem(item);
        return itemCardapioRepository.save(item);
    }
}
