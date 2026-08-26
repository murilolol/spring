package com.curso.restaurante.domain.pedido;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StatusPedidoTest {

    @Test
    void abertoDevePermitirEmPreparoECancelado() {
        assertProximos(StatusPedido.ABERTO, EnumSet.of(StatusPedido.EM_PREPARO, StatusPedido.CANCELADO));
    }

    @Test
    void emPreparoDevePermitirProntoECancelado() {
        assertProximos(StatusPedido.EM_PREPARO, EnumSet.of(StatusPedido.PRONTO, StatusPedido.CANCELADO));
    }

    @Test
    void prontoDevePermitirEntregueECancelado() {
        assertProximos(StatusPedido.PRONTO, EnumSet.of(StatusPedido.ENTREGUE, StatusPedido.CANCELADO));
    }

    @Test
    void entregueDevePermitirApenasPago() {
        assertProximos(StatusPedido.ENTREGUE, EnumSet.of(StatusPedido.PAGO));
    }

    @Test
    void pagoEhEstadoFinal() {
        assertProximos(StatusPedido.PAGO, EnumSet.noneOf(StatusPedido.class));
        assertTrue(StatusPedido.PAGO.ehFinal());
    }

    @Test
    void canceladoEhEstadoFinal() {
        assertProximos(StatusPedido.CANCELADO, EnumSet.noneOf(StatusPedido.class));
        assertTrue(StatusPedido.CANCELADO.ehFinal());
    }

    @Test
    void nenhumEstadoIntermediarioEhFinal() {
        assertFalse(StatusPedido.ABERTO.ehFinal());
        assertFalse(StatusPedido.EM_PREPARO.ehFinal());
        assertFalse(StatusPedido.PRONTO.ehFinal());
        assertFalse(StatusPedido.ENTREGUE.ehFinal());
    }

    @Test
    void matrizCompletaDeTransicoes() {
        for (StatusPedido origem : StatusPedido.values()) {
            for (StatusPedido destino : StatusPedido.values()) {
                boolean esperado = origem.proximos().contains(destino);
                assertEquals(
                        esperado,
                        origem.podeTransicionarPara(destino),
                        "transição " + origem + " -> " + destino);
            }
        }
    }

    private void assertProximos(StatusPedido status, Set<StatusPedido> esperados) {
        assertEquals(esperados, status.proximos());

        for (StatusPedido destino : StatusPedido.values()) {
            assertEquals(esperados.contains(destino), status.podeTransicionarPara(destino));
        }
    }
}
