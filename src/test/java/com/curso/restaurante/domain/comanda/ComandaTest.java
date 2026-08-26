package com.curso.restaurante.domain.comanda;

import com.curso.restaurante.domain.cardapio.CategoriaCardapio;
import com.curso.restaurante.domain.cardapio.ItemCardapio;
import com.curso.restaurante.domain.cardapio.SecaoPreparo;
import com.curso.restaurante.domain.cliente.Cliente;
import com.curso.restaurante.domain.comum.RegraDeNegocioException;
import com.curso.restaurante.domain.comum.TransicaoDeStatusInvalidaException;
import com.curso.restaurante.domain.mesa.Mesa;
import com.curso.restaurante.domain.pedido.Pedido;
import com.curso.restaurante.domain.usuario.PerfilUsuario;
import com.curso.restaurante.domain.usuario.Usuario;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ComandaTest {

    @Test
    void deveAbrirComandaDeSalaoComMesa() {
        Mesa mesa = new Mesa(1, 4, "Salão");
        Comanda comanda = new Comanda(
                "CMD-0001", TipoAtendimento.SALAO, mesa, null, umResponsavel(), 4, new BigDecimal("10.00"), null);

        assertEquals("CMD-0001", comanda.getCodigo());
        assertEquals(TipoAtendimento.SALAO, comanda.getTipoAtendimento());
        assertEquals(mesa, comanda.getMesa());
        assertNull(comanda.getCliente());
        assertEquals(StatusComanda.ABERTA, comanda.getStatus());
        assertNotNull(comanda.getAbertaEm());
    }

    @Test
    void salaoDeveExigirMesa() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Comanda(
                        "CMD-0002", TipoAtendimento.SALAO, null, umCliente(), umResponsavel(), 2,
                        BigDecimal.ZERO, null));
    }

    @Test
    void deliveryDeveExigirClienteEProibirMesa() {
        Mesa mesa = new Mesa(2, 4, "Salão");

        assertThrows(
                IllegalArgumentException.class,
                () -> new Comanda(
                        "CMD-0003", TipoAtendimento.DELIVERY, null, null, umResponsavel(), 1,
                        BigDecimal.ZERO, null));

        assertThrows(
                IllegalArgumentException.class,
                () -> new Comanda(
                        "CMD-0004", TipoAtendimento.DELIVERY, mesa, umCliente(), umResponsavel(), 1,
                        BigDecimal.ZERO, null));
    }

    @Test
    void retiradaDeveExigirClienteEProibirMesa() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Comanda(
                        "CMD-0005", TipoAtendimento.RETIRADA, null, null, umResponsavel(), 1,
                        BigDecimal.ZERO, null));
    }

    @Test
    void balcaoAceitaMesaOuCliente() {
        Mesa mesa = new Mesa(3, 4, "Salão");
        Comanda comMesa = new Comanda(
                "CMD-0006", TipoAtendimento.BALCAO, mesa, null, umResponsavel(), 2, BigDecimal.ZERO, null);
        Comanda comCliente = new Comanda(
                "CMD-0007", TipoAtendimento.BALCAO, null, umCliente(), umResponsavel(), 1, BigDecimal.ZERO, null);

        assertEquals(mesa, comMesa.getMesa());
        assertEquals(umCliente().getNome(), comCliente.getCliente().getNome());
    }

    @Test
    void deveExigirAoMenosMesaOuCliente() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Comanda(
                        "CMD-0008", TipoAtendimento.BALCAO, null, null, umResponsavel(), 1,
                        BigDecimal.ZERO, null));
    }

    @Test
    void deveRejeitarNumeroDePessoasMenorOuIgualAZero() {
        Mesa mesa = new Mesa(4, 4, "Salão");

        assertThrows(
                IllegalArgumentException.class,
                () -> new Comanda(
                        "CMD-0009", TipoAtendimento.SALAO, mesa, null, umResponsavel(), 0,
                        BigDecimal.ZERO, null));
    }

    @Test
    void deveRejeitarPercentualDeTaxaForaDoIntervalo() {
        Mesa mesa = new Mesa(5, 4, "Salão");

        assertThrows(
                IllegalArgumentException.class,
                () -> new Comanda(
                        "CMD-0010", TipoAtendimento.SALAO, mesa, null, umResponsavel(), 2,
                        new BigDecimal("101"), null));
        assertThrows(
                IllegalArgumentException.class,
                () -> new Comanda(
                        "CMD-0011", TipoAtendimento.SALAO, mesa, null, umResponsavel(), 2,
                        new BigDecimal("-1"), null));
    }

    @Test
    void deveFecharComandaComPedidoEntregue() {
        Comanda comanda = umaComandaAberta();
        Pedido pedido = umPedidoEntregue(comanda);

        comanda.fechar();

        assertEquals(StatusComanda.FECHADA, comanda.getStatus());
        assertNotNull(comanda.getFechadaEm());
        assertEquals(1, comanda.getPedidos().size());
        assertTrue(comanda.getPedidos().contains(pedido));
    }

    @Test
    void naoDeveFecharComandaSemNenhumPedidoEntregue() {
        Comanda comanda = umaComandaAberta();
        new Pedido("PED-SEM-ENTREGA", comanda, umResponsavel(), null);

        assertThrows(RegraDeNegocioException.class, comanda::fechar);
    }

    @Test
    void naoDeveFecharComandaComPedidoAindaEmAberto() {
        Comanda comanda = umaComandaAberta();
        umPedidoEntregue(comanda);
        new Pedido("PED-AINDA-ABERTO", comanda, umResponsavel(), null);

        assertThrows(RegraDeNegocioException.class, comanda::fechar);
    }

    @Test
    void naoDeveFecharComandaJaFechada() {
        Comanda comanda = umaComandaAberta();
        umPedidoEntregue(comanda);
        comanda.fechar();

        assertThrows(TransicaoDeStatusInvalidaException.class, comanda::fechar);
    }

    @Test
    void deveCancelarComandaSemPedidoEntregue() {
        Comanda comanda = umaComandaAberta();
        new Pedido("PED-CANCELAVEL", comanda, umResponsavel(), null);

        comanda.cancelar("Cliente desistiu");

        assertEquals(StatusComanda.CANCELADA, comanda.getStatus());
        assertNotNull(comanda.getCanceladaEm());
    }

    @Test
    void naoDeveCancelarComandaComPedidoEntregue() {
        Comanda comanda = umaComandaAberta();
        umPedidoEntregue(comanda);

        assertThrows(RegraDeNegocioException.class, () -> comanda.cancelar("motivo"));
    }

    @Test
    void naoDeveCancelarComandaFechada() {
        Comanda comanda = umaComandaAberta();
        umPedidoEntregue(comanda);
        comanda.fechar();

        assertThrows(TransicaoDeStatusInvalidaException.class, () -> comanda.cancelar("motivo"));
    }

    @Test
    void deveReabrirComandaFechada() {
        Comanda comanda = umaComandaAberta();
        umPedidoEntregue(comanda);
        comanda.fechar();

        comanda.reabrir();

        assertEquals(StatusComanda.ABERTA, comanda.getStatus());
    }

    @Test
    void naoDeveReabrirComandaAberta() {
        Comanda comanda = umaComandaAberta();

        assertThrows(TransicaoDeStatusInvalidaException.class, comanda::reabrir);
    }

    @Test
    void deveMarcarComoPagaComandaFechada() {
        Comanda comanda = umaComandaAberta();
        umPedidoEntregue(comanda);
        comanda.fechar();

        comanda.marcarComoPaga();

        assertEquals(StatusComanda.PAGA, comanda.getStatus());
    }

    @Test
    void marcarComoPagaDeveCascatearParaOsPedidosEntregues() {
        Comanda comanda = umaComandaAberta();
        Pedido pedidoEntregue = umPedidoEntregue(comanda);
        comanda.fechar();

        comanda.marcarComoPaga();

        assertEquals(com.curso.restaurante.domain.pedido.StatusPedido.PAGO, pedidoEntregue.getStatus());
    }

    @Test
    void naoDeveMarcarComoPagaComandaAberta() {
        Comanda comanda = umaComandaAberta();

        assertThrows(TransicaoDeStatusInvalidaException.class, comanda::marcarComoPaga);
    }

    @Test
    void deveCalcularSubtotalTaxaETotal() {
        Comanda comanda = umaComandaComTaxa(new BigDecimal("10.00"));
        umPedidoEntregueComValor(comanda, "20.00");
        umPedidoEntregueComValor(comanda, "30.00");

        assertEquals(0, new BigDecimal("50.00").compareTo(comanda.calcularSubtotal()));
        assertEquals(0, new BigDecimal("5.00").compareTo(comanda.calcularTaxaServico()));
        assertEquals(0, new BigDecimal("55.00").compareTo(comanda.calcularTotal()));
    }

    @Test
    void calcularSubtotalDeveIgnorarPedidosCancelados() {
        Comanda comanda = umaComandaComTaxa(BigDecimal.ZERO);
        umPedidoEntregueComValor(comanda, "20.00");
        Pedido cancelado = new Pedido("PED-CANCELADO-SUBTOTAL", comanda, umResponsavel(), null);
        cancelado.cancelar("motivo");

        assertEquals(0, new BigDecimal("20.00").compareTo(comanda.calcularSubtotal()));
    }

    @Test
    void deveCalcularSaldoDevedor() {
        Comanda comanda = umaComandaComTaxa(BigDecimal.ZERO);
        umPedidoEntregueComValor(comanda, "40.00");

        assertEquals(0, new BigDecimal("15.00").compareTo(comanda.calcularSaldoDevedor(new BigDecimal("25.00"))));
    }

    @Test
    void deveTransferirParaOutraMesa() {
        Comanda comanda = umaComandaAberta();
        Mesa novaMesa = new Mesa((int) (Math.random() * 100000) + 200000, 6, "Varanda");

        comanda.transferirParaMesa(novaMesa);

        assertEquals(novaMesa, comanda.getMesa());
    }

    @Test
    void naoDeveTransferirMesaDeComandaFechada() {
        Comanda comanda = umaComandaAberta();
        umPedidoEntregue(comanda);
        comanda.fechar();
        Mesa novaMesa = new Mesa((int) (Math.random() * 100000) + 300000, 6, "Varanda");

        assertThrows(TransicaoDeStatusInvalidaException.class, () -> comanda.transferirParaMesa(novaMesa));
    }

    @Test
    void naoDeveTransferirMesaDeComandaDeDelivery() {
        Comanda comanda = new Comanda(
                "CMD-DELIVERY-TRANSF", TipoAtendimento.DELIVERY, null, umCliente(), umResponsavel(), 1,
                BigDecimal.ZERO, null);
        Mesa novaMesa = new Mesa((int) (Math.random() * 100000) + 400000, 6, "Varanda");

        assertThrows(IllegalArgumentException.class, () -> comanda.transferirParaMesa(novaMesa));
    }

    private Comanda umaComandaAberta() {
        Mesa mesa = new Mesa((int) (Math.random() * 100000) + 1, 4, "Salão");
        return new Comanda(
                "CMD-000" + (int) (Math.random() * 1000),
                TipoAtendimento.SALAO,
                mesa,
                null,
                umResponsavel(),
                2,
                BigDecimal.ZERO,
                null);
    }

    private Comanda umaComandaComTaxa(BigDecimal percentualTaxaServico) {
        Mesa mesa = new Mesa((int) (Math.random() * 100000) + 1, 4, "Salão");
        return new Comanda(
                "CMD-TAXA-" + Math.random(),
                TipoAtendimento.SALAO,
                mesa,
                null,
                umResponsavel(),
                2,
                percentualTaxaServico,
                null);
    }

    private Pedido umPedidoEntregue(Comanda comanda) {
        return umPedidoEntregueComValor(comanda, "9.50");
    }

    private Pedido umPedidoEntregueComValor(Comanda comanda, String valorUnitario) {
        Pedido pedido = new Pedido("PED-ENTREGUE-" + Math.random(), comanda, umResponsavel(), null);
        pedido.adicionarItem(umItemCardapio(valorUnitario), 1, null);
        pedido.enviarParaPreparo();
        pedido.marcarComoPronto();
        pedido.marcarComoEntregue();
        return pedido;
    }

    private ItemCardapio umItemCardapio(String valorUnitario) {
        CategoriaCardapio categoria = new CategoriaCardapio("Categoria Comanda Teste " + Math.random(), null, 1);
        ItemCardapio item = new ItemCardapio(
                "ITEM-COMANDA-TESTE-" + Math.random(),
                "Item de Teste",
                null,
                new BigDecimal(valorUnitario),
                5,
                SecaoPreparo.COZINHA,
                true,
                false,
                BigDecimal.ZERO,
                LocalDate.of(2026, 8, 20));
        categoria.adicionarItem(item);
        return item;
    }

    private Usuario umResponsavel() {
        return new Usuario("Garçom Comanda", "garcom.comanda.dominio", "hash", PerfilUsuario.GARCOM);
    }

    private Cliente umCliente() {
        return new Cliente("Cliente Comanda", null, "(45) 90000-0000", null, null, null);
    }
}
