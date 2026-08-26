package com.curso.restaurante.domain.pedido;

import com.curso.restaurante.domain.cardapio.CategoriaCardapio;
import com.curso.restaurante.domain.cardapio.ItemCardapio;
import com.curso.restaurante.domain.cardapio.SecaoPreparo;
import com.curso.restaurante.domain.comanda.Comanda;
import com.curso.restaurante.domain.comanda.TipoAtendimento;
import com.curso.restaurante.domain.comum.RegraDeNegocioException;
import com.curso.restaurante.domain.comum.TransicaoDeStatusInvalidaException;
import com.curso.restaurante.domain.mesa.Mesa;
import com.curso.restaurante.domain.usuario.PerfilUsuario;
import com.curso.restaurante.domain.usuario.Usuario;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PedidoTest {

    @Test
    void deveAbrirPedidoAbertoSemItens() {
        Pedido pedido = new Pedido("PED-0001", umaComanda(), umUsuario(), null);

        assertEquals(StatusPedido.ABERTO, pedido.getStatus());
        assertNotNull(pedido.getAbertoEm());
        assertTrue(pedido.getItens().isEmpty());
    }

    @Test
    void deveAdicionarItemAoPedido() {
        Pedido pedido = new Pedido("PED-0002", umaComanda(), umUsuario(), null);

        ItemPedido item = pedido.adicionarItem(umItemCardapio("9.50"), 2, "sem gelo");

        assertEquals(1, pedido.getItens().size());
        assertEquals(item, pedido.getItens().getFirst());
        assertEquals(0, new BigDecimal("19.00").compareTo(pedido.calcularTotal()));
    }

    @Test
    void naoDeveAdicionarItemAPedidoQueNaoEstaAberto() {
        Pedido pedido = new Pedido("PED-0003", umaComanda(), umUsuario(), null);
        pedido.adicionarItem(umItemCardapio("9.50"), 1, null);
        pedido.enviarParaPreparo();

        assertThrows(
                TransicaoDeStatusInvalidaException.class,
                () -> pedido.adicionarItem(umItemCardapio("5.00"), 1, null));
    }

    @Test
    void deveRemoverItemDoPedido() {
        Pedido pedido = new Pedido("PED-0004", umaComanda(), umUsuario(), null);
        ItemPedido item = pedido.adicionarItem(umItemCardapio("9.50"), 1, null);

        pedido.removerItem(item);

        assertTrue(pedido.getItens().isEmpty());
    }

    @Test
    void naoDeveEnviarParaPreparoSemItens() {
        Pedido pedido = new Pedido("PED-0005", umaComanda(), umUsuario(), null);

        assertThrows(RegraDeNegocioException.class, pedido::enviarParaPreparo);
    }

    @Test
    void deveSeguirOFluxoCompletoAteEntregue() {
        Pedido pedido = new Pedido("PED-0006", umaComanda(), umUsuario(), null);
        pedido.adicionarItem(umItemCardapio("9.50"), 1, null);

        pedido.enviarParaPreparo();
        assertEquals(StatusPedido.EM_PREPARO, pedido.getStatus());
        assertNotNull(pedido.getEnviadoPreparoEm());

        pedido.marcarComoPronto();
        assertEquals(StatusPedido.PRONTO, pedido.getStatus());
        assertNotNull(pedido.getProntoEm());

        pedido.marcarComoEntregue();
        assertEquals(StatusPedido.ENTREGUE, pedido.getStatus());
        assertNotNull(pedido.getEntregueEm());

        pedido.marcarComoPago();
        assertEquals(StatusPedido.PAGO, pedido.getStatus());
        assertNotNull(pedido.getPagoEm());
    }

    @Test
    void deveCancelarPedidoAbertoComMotivo() {
        Pedido pedido = new Pedido("PED-0007", umaComanda(), umUsuario(), null);

        pedido.cancelar("Cliente desistiu");

        assertEquals(StatusPedido.CANCELADO, pedido.getStatus());
        assertEquals("Cliente desistiu", pedido.getMotivoCancelamento());
        assertNotNull(pedido.getCanceladoEm());
    }

    @Test
    void naoDeveCancelarSemMotivo() {
        Pedido pedido = new Pedido("PED-0008", umaComanda(), umUsuario(), null);

        assertThrows(IllegalArgumentException.class, () -> pedido.cancelar("   "));
    }

    @Test
    void naoDeveCancelarPedidoEntregue() {
        Pedido pedido = new Pedido("PED-0009", umaComanda(), umUsuario(), null);
        pedido.adicionarItem(umItemCardapio("9.50"), 1, null);
        pedido.enviarParaPreparo();
        pedido.marcarComoPronto();
        pedido.marcarComoEntregue();

        assertThrows(TransicaoDeStatusInvalidaException.class, () -> pedido.cancelar("motivo"));
    }

    @Test
    void naoDeveMarcarComoPagoSemEstarEntregue() {
        Pedido pedido = new Pedido("PED-0010", umaComanda(), umUsuario(), null);

        assertThrows(TransicaoDeStatusInvalidaException.class, pedido::marcarComoPago);
    }

    private Comanda umaComanda() {
        Mesa mesa = new Mesa((int) (Math.random() * 1000000), 4, "Salão Pedido Teste");
        return new Comanda(
                "CMD-PEDIDO-TESTE-" + Math.random(), TipoAtendimento.SALAO, mesa, null, umUsuario(), 2,
                BigDecimal.ZERO, null);
    }

    private Usuario umUsuario() {
        return new Usuario("Garçom Pedido Teste", "garcom.pedido.dominio." + Math.random(), "hash", PerfilUsuario.GARCOM);
    }

    private ItemCardapio umItemCardapio(String precoVenda) {
        CategoriaCardapio categoria = new CategoriaCardapio("Categoria Pedido Teste " + Math.random(), null, 1);
        ItemCardapio itemCardapio = new ItemCardapio(
                "ITEM-PEDIDO-DOMINIO-" + Math.random(),
                "Item de Teste",
                null,
                new BigDecimal(precoVenda),
                5,
                SecaoPreparo.COZINHA,
                true,
                false,
                BigDecimal.ZERO,
                LocalDate.of(2026, 8, 20));
        categoria.adicionarItem(itemCardapio);
        return itemCardapio;
    }
}
