package com.curso.restaurante.domain.cozinha;

import com.curso.restaurante.domain.cardapio.CategoriaCardapio;
import com.curso.restaurante.domain.cardapio.ItemCardapio;
import com.curso.restaurante.domain.cardapio.SecaoPreparo;
import com.curso.restaurante.domain.comanda.Comanda;
import com.curso.restaurante.domain.comanda.TipoAtendimento;
import com.curso.restaurante.domain.comum.TransicaoDeStatusInvalidaException;
import com.curso.restaurante.domain.mesa.Mesa;
import com.curso.restaurante.domain.pedido.ItemPedido;
import com.curso.restaurante.domain.pedido.Pedido;
import com.curso.restaurante.domain.usuario.PerfilUsuario;
import com.curso.restaurante.domain.usuario.Usuario;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PreparoItemTest {

    @Test
    void deveEnfileirarComStatusAguardandoEPrioridadeNormal() {
        PreparoItem preparoItem = umPreparoItem();

        assertEquals(StatusPreparo.AGUARDANDO, preparoItem.getStatus());
        assertEquals(PrioridadePreparo.NORMAL, preparoItem.getPrioridade());
        assertEquals(SecaoPreparo.COZINHA, preparoItem.getSecao());
        assertNotNull(preparoItem.getEnfileiradoEm());
    }

    @Test
    void deveIniciarPreparo() {
        PreparoItem preparoItem = umPreparoItem();
        Usuario cozinheiro = umUsuario(PerfilUsuario.COZINHA);

        preparoItem.iniciar(cozinheiro);

        assertEquals(StatusPreparo.EM_PREPARO, preparoItem.getStatus());
        assertEquals(cozinheiro, preparoItem.getResponsavel());
        assertNotNull(preparoItem.getIniciadoEm());
    }

    @Test
    void naoDeveIniciarPreparoQueNaoEstaAguardando() {
        PreparoItem preparoItem = umPreparoItem();
        preparoItem.iniciar(umUsuario(PerfilUsuario.COZINHA));

        assertThrows(
                TransicaoDeStatusInvalidaException.class,
                () -> preparoItem.iniciar(umUsuario(PerfilUsuario.COZINHA)));
    }

    @Test
    void deveConcluirPreparoEmAndamento() {
        PreparoItem preparoItem = umPreparoItem();
        preparoItem.iniciar(umUsuario(PerfilUsuario.COZINHA));

        preparoItem.concluir();

        assertEquals(StatusPreparo.CONCLUIDO, preparoItem.getStatus());
        assertNotNull(preparoItem.getConcluidoEm());
    }

    @Test
    void naoDeveConcluirPreparoAindaAguardando() {
        PreparoItem preparoItem = umPreparoItem();

        assertThrows(TransicaoDeStatusInvalidaException.class, preparoItem::concluir);
    }

    @Test
    void deveCancelarPreparoAguardandoOuEmPreparo() {
        PreparoItem aguardando = umPreparoItem();
        aguardando.cancelar();
        assertEquals(StatusPreparo.CANCELADO, aguardando.getStatus());

        PreparoItem emPreparo = umPreparoItem();
        emPreparo.iniciar(umUsuario(PerfilUsuario.COZINHA));
        emPreparo.cancelar();
        assertEquals(StatusPreparo.CANCELADO, emPreparo.getStatus());
    }

    @Test
    void naoDeveCancelarPreparoJaConcluido() {
        PreparoItem preparoItem = umPreparoItem();
        preparoItem.iniciar(umUsuario(PerfilUsuario.COZINHA));
        preparoItem.concluir();

        assertThrows(TransicaoDeStatusInvalidaException.class, preparoItem::cancelar);
    }

    @Test
    void deveAlterarPrioridade() {
        PreparoItem preparoItem = umPreparoItem();

        preparoItem.alterarPrioridade(PrioridadePreparo.URGENTE);

        assertEquals(PrioridadePreparo.URGENTE, preparoItem.getPrioridade());
    }

    @Test
    void deveCalcularTempoDecorridoEDetectarAtraso() {
        PreparoItem preparoItem = umPreparoItem();
        LocalDateTime referencia = preparoItem.getEnfileiradoEm().plusMinutes(20);

        assertEquals(20, preparoItem.calcularTempoDecorridoMinutos(referencia));
        assertTrue(preparoItem.estaAtrasado(referencia));
    }

    @Test
    void naoDeveEstarAtrasadoDentroDoTempoEstimado() {
        PreparoItem preparoItem = umPreparoItem();
        LocalDateTime referencia = preparoItem.getEnfileiradoEm().plusMinutes(2);

        assertFalse(preparoItem.estaAtrasado(referencia));
    }

    @Test
    void itemConcluidoNuncaEstaAtrasado() {
        PreparoItem preparoItem = umPreparoItem();
        preparoItem.iniciar(umUsuario(PerfilUsuario.COZINHA));
        preparoItem.concluir();

        assertFalse(preparoItem.estaAtrasado(preparoItem.getEnfileiradoEm().plusMinutes(999)));
    }

    private PreparoItem umPreparoItem() {
        ItemPedido itemPedido = umItemPedido();
        return new PreparoItem(itemPedido, SecaoPreparo.COZINHA, 5);
    }

    private ItemPedido umItemPedido() {
        Mesa mesa = new Mesa((int) (Math.random() * 1000000), 4, "Salão Preparo Teste");
        Usuario responsavel = umUsuario(PerfilUsuario.GARCOM);
        Comanda comanda = new Comanda(
                "CMD-PREPARO-TESTE-" + Math.random(), TipoAtendimento.SALAO, mesa, null, responsavel, 2,
                BigDecimal.ZERO, null);
        Pedido pedido = new Pedido("PED-PREPARO-TESTE-" + Math.random(), comanda, responsavel, null);
        CategoriaCardapio categoria = new CategoriaCardapio("Categoria Preparo Teste " + Math.random(), null, 1);
        ItemCardapio itemCardapio = new ItemCardapio(
                "ITEM-PREPARO-TESTE-" + Math.random(), "Item Preparo Teste", null, new BigDecimal("9.00"), 5,
                SecaoPreparo.COZINHA, true, false, BigDecimal.ZERO, LocalDate.of(2026, 8, 20));
        categoria.adicionarItem(itemCardapio);
        return pedido.adicionarItem(itemCardapio, 1, null);
    }

    private Usuario umUsuario(PerfilUsuario perfil) {
        return new Usuario("Usuário Preparo Teste", "usuario.preparo.teste." + Math.random(), "hash", perfil);
    }
}
