package com.curso.restaurante.domain.pedido;

import com.curso.restaurante.domain.cardapio.CategoriaCardapio;
import com.curso.restaurante.domain.cardapio.ItemCardapio;
import com.curso.restaurante.domain.cardapio.SecaoPreparo;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ItemPedidoTest {

    @Test
    void deveCalcularSubtotal() {
        ItemPedido itemPedido = new ItemPedido(umItemCardapio("9.50"), 3, null);

        assertEquals(0, new BigDecimal("28.50").compareTo(itemPedido.calcularSubtotal()));
    }

    @Test
    void deveGuardarUmSnapshotDoPrecoNoMomentoDoPedido() {
        ItemCardapio itemCardapio = umItemCardapio("9.50");
        ItemPedido itemPedido = new ItemPedido(itemCardapio, 1, null);

        itemCardapio.alterarPreco(new BigDecimal("15.00"));

        assertEquals(0, new BigDecimal("9.50").compareTo(itemPedido.getPrecoUnitario()));
    }

    @Test
    void deveRejeitarQuantidadeMenorOuIgualAZero() {
        ItemCardapio itemCardapio = umItemCardapio("9.50");

        assertThrows(IllegalArgumentException.class, () -> new ItemPedido(itemCardapio, 0, null));
        assertThrows(IllegalArgumentException.class, () -> new ItemPedido(itemCardapio, -1, null));
    }

    @Test
    void deveAceitarObservacao() {
        ItemPedido itemPedido = new ItemPedido(umItemCardapio("9.50"), 1, "sem cebola");

        assertEquals("sem cebola", itemPedido.getObservacao());
    }

    private ItemCardapio umItemCardapio(String precoVenda) {
        CategoriaCardapio categoria = new CategoriaCardapio("Categoria Item Pedido Teste", null, 1);
        ItemCardapio itemCardapio = new ItemCardapio(
                "ITEM-PEDIDO-TESTE-" + Math.random(),
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
