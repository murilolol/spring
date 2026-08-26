package com.curso.restaurante.service.comanda;

import com.curso.restaurante.domain.cardapio.CategoriaCardapio;
import com.curso.restaurante.domain.cardapio.ItemCardapio;
import com.curso.restaurante.domain.cardapio.SecaoPreparo;
import com.curso.restaurante.domain.comanda.Comanda;
import com.curso.restaurante.domain.comanda.StatusComanda;
import com.curso.restaurante.domain.comanda.TipoAtendimento;
import com.curso.restaurante.domain.comum.RecursoNaoEncontradoException;
import com.curso.restaurante.domain.mesa.StatusMesa;
import com.curso.restaurante.domain.pedido.Pedido;
import com.curso.restaurante.domain.usuario.PerfilUsuario;
import com.curso.restaurante.repository.cardapio.CategoriaCardapioRepository;
import com.curso.restaurante.repository.cardapio.ItemCardapioRepository;
import com.curso.restaurante.service.cliente.ClienteService;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ComandaServiceTest {

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
    void deveAbrirComandaDeSalaoEOcuparAMesa() {
        var responsavel = usuarioService.criar("Garçom Abrir Svc", "garcom.abrir.comanda.svc", "senha", PerfilUsuario.GARCOM);
        var mesa = mesaService.criar(801, 4, "Salão Svc Comanda");

        Comanda comanda = comandaService.abrir(
                TipoAtendimento.SALAO, mesa.getId(), null, responsavel.getUsername(), 4,
                new BigDecimal("10.00"), null);

        assertEquals(StatusComanda.ABERTA, comanda.getStatus());
        assertTrue(comanda.getCodigo().startsWith("CMD-"));
        assertEquals(StatusMesa.OCUPADA, mesaService.buscarPorId(mesa.getId()).getStatus());
    }

    @Test
    void naoDeveAbrirDuasComandasNaMesmaMesaLivre() {
        var responsavel = usuarioService.criar("Garçom Dupla Svc", "garcom.dupla.comanda.svc", "senha", PerfilUsuario.GARCOM);
        var mesa = mesaService.criar(802, 4, "Salão Svc Comanda Dupla");

        comandaService.abrir(
                TipoAtendimento.SALAO, mesa.getId(), null, responsavel.getUsername(), 2, BigDecimal.ZERO, null);

        assertThrows(
                Exception.class,
                () -> comandaService.abrir(
                        TipoAtendimento.SALAO, mesa.getId(), null, responsavel.getUsername(), 2,
                        BigDecimal.ZERO, null));
    }

    @Test
    void deveAbrirComandaDeDeliveryComCliente() {
        var responsavel = usuarioService.criar("Garçom Delivery Svc", "garcom.delivery.comanda.svc", "senha", PerfilUsuario.GARCOM);
        var cliente = clienteService.criar("Cliente Delivery Svc", null, "(45) 90000-1000", null, null, null);

        Comanda comanda = comandaService.abrir(
                TipoAtendimento.DELIVERY, null, cliente.getId(), responsavel.getUsername(), 1,
                BigDecimal.ZERO, null);

        assertEquals(cliente.getId(), comanda.getCliente().getId());
    }

    @Test
    void buscarPorIdDeveLancarQuandoNaoExiste() {
        assertThrows(RecursoNaoEncontradoException.class, () -> comandaService.buscarPorId(-1L));
    }

    @Test
    void deveFecharComandaELiberarAMesa() {
        var responsavel = usuarioService.criar("Garçom Fechar Svc", "garcom.fechar.comanda.svc", "senha", PerfilUsuario.GARCOM);
        var mesa = mesaService.criar(803, 4, "Salão Svc Fechar");
        Comanda comanda = comandaService.abrir(
                TipoAtendimento.SALAO, mesa.getId(), null, responsavel.getUsername(), 2, BigDecimal.ZERO, null);
        entregarUmPedido(comanda, responsavel.getUsername());

        Comanda fechada = comandaService.fechar(comanda.getId());

        assertEquals(StatusComanda.FECHADA, fechada.getStatus());
        assertEquals(StatusMesa.LIVRE, mesaService.buscarPorId(mesa.getId()).getStatus());
    }

    @Test
    void deveCancelarComandaELiberarAMesa() {
        var responsavel = usuarioService.criar("Garçom Cancelar Svc", "garcom.cancelar.comanda.svc", "senha", PerfilUsuario.GARCOM);
        var mesa = mesaService.criar(804, 4, "Salão Svc Cancelar");
        Comanda comanda = comandaService.abrir(
                TipoAtendimento.SALAO, mesa.getId(), null, responsavel.getUsername(), 2, BigDecimal.ZERO, null);

        Comanda cancelada = comandaService.cancelar(comanda.getId(), "Cliente desistiu");

        assertEquals(StatusComanda.CANCELADA, cancelada.getStatus());
        assertEquals(StatusMesa.LIVRE, mesaService.buscarPorId(mesa.getId()).getStatus());
    }

    @Test
    void deveReabrirComandaEOcuparNovamenteAMesa() {
        var responsavel = usuarioService.criar("Garçom Reabrir Svc", "garcom.reabrir.comanda.svc", "senha", PerfilUsuario.GARCOM);
        var mesa = mesaService.criar(805, 4, "Salão Svc Reabrir");
        Comanda comanda = comandaService.abrir(
                TipoAtendimento.SALAO, mesa.getId(), null, responsavel.getUsername(), 2, BigDecimal.ZERO, null);
        entregarUmPedido(comanda, responsavel.getUsername());
        comandaService.fechar(comanda.getId());

        Comanda reaberta = comandaService.reabrir(comanda.getId());

        assertEquals(StatusComanda.ABERTA, reaberta.getStatus());
        assertEquals(StatusMesa.OCUPADA, mesaService.buscarPorId(mesa.getId()).getStatus());
    }

    @Test
    void deveTransferirMesa() {
        var responsavel = usuarioService.criar("Garçom Transfere Svc", "garcom.transfere.comanda.svc", "senha", PerfilUsuario.GARCOM);
        var mesaOrigem = mesaService.criar(806, 4, "Salão Svc Origem");
        var mesaDestino = mesaService.criar(807, 4, "Salão Svc Destino");
        Comanda comanda = comandaService.abrir(
                TipoAtendimento.SALAO, mesaOrigem.getId(), null, responsavel.getUsername(), 2, BigDecimal.ZERO, null);

        Comanda transferida = comandaService.transferirMesa(comanda.getId(), mesaDestino.getId());

        assertEquals(mesaDestino.getId(), transferida.getMesa().getId());
        assertEquals(StatusMesa.LIVRE, mesaService.buscarPorId(mesaOrigem.getId()).getStatus());
        assertEquals(StatusMesa.OCUPADA, mesaService.buscarPorId(mesaDestino.getId()).getStatus());
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
                new CategoriaCardapio("Categoria Comanda Svc " + Math.random(), null, 1));
        ItemCardapio item = new ItemCardapio(
                "ITEM-COMANDA-SVC-" + Math.random(), "Item Comanda Svc", null, new BigDecimal("9.00"), 5,
                SecaoPreparo.COZINHA, true, false, BigDecimal.ZERO, LocalDate.of(2026, 8, 20));
        categoria.adicionarItem(item);
        return itemCardapioRepository.save(item);
    }
}
