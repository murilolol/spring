package com.curso.restaurante.domain.caixa;

import com.curso.restaurante.domain.comanda.Comanda;
import com.curso.restaurante.domain.comanda.TipoAtendimento;
import com.curso.restaurante.domain.comum.ConflitoDeEstadoException;
import com.curso.restaurante.domain.mesa.Mesa;
import com.curso.restaurante.domain.usuario.PerfilUsuario;
import com.curso.restaurante.domain.usuario.Usuario;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PagamentoTest {

    @Test
    void deveRegistrarPagamentoEmDinheiroECalcularTroco() {
        Pagamento pagamento = new Pagamento(
                umaComanda(), umaSessao(), FormaPagamento.DINHEIRO, new BigDecimal("50.00"),
                new BigDecimal("60.00"), umUsuario());

        assertEquals(0, new BigDecimal("50.00").compareTo(pagamento.getValor()));
        assertEquals(0, new BigDecimal("60.00").compareTo(pagamento.getValorRecebido()));
        assertEquals(0, new BigDecimal("10.00").compareTo(pagamento.getTroco()));
    }

    @Test
    void deveRejeitarValorRecebidoInsuficienteEmDinheiro() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Pagamento(
                        umaComanda(), umaSessao(), FormaPagamento.DINHEIRO, new BigDecimal("50.00"),
                        new BigDecimal("40.00"), umUsuario()));
    }

    @Test
    void deveRegistrarPagamentoEmCartaoSemValorRecebidoNemTroco() {
        Pagamento pagamento = new Pagamento(
                umaComanda(), umaSessao(), FormaPagamento.CARTAO_CREDITO, new BigDecimal("50.00"), null, umUsuario());

        assertNull(pagamento.getValorRecebido());
        assertNull(pagamento.getTroco());
    }

    @Test
    void deveRejeitarValorRecebidoParaFormaDiferenteDeDinheiro() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Pagamento(
                        umaComanda(), umaSessao(), FormaPagamento.PIX, new BigDecimal("50.00"),
                        new BigDecimal("50.00"), umUsuario()));
    }

    @Test
    void deveRejeitarValorMenorOuIgualAZero() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Pagamento(umaComanda(), umaSessao(), FormaPagamento.PIX, BigDecimal.ZERO, null, umUsuario()));
    }

    @Test
    void naoDeveRegistrarPagamentoComSessaoFechada() {
        SessaoCaixa sessao = umaSessao();
        sessao.fechar(umUsuario(), BigDecimal.ZERO, BigDecimal.ZERO, null);

        assertThrows(
                ConflitoDeEstadoException.class,
                () -> new Pagamento(
                        umaComanda(), sessao, FormaPagamento.PIX, new BigDecimal("50.00"), null, umUsuario()));
    }

    private Comanda umaComanda() {
        Mesa mesa = new Mesa((int) (Math.random() * 1000000), 4, "Salão Pagamento Teste");
        return new Comanda(
                "CMD-PAGAMENTO-TESTE-" + Math.random(), TipoAtendimento.SALAO, mesa, null, umUsuario(), 2,
                BigDecimal.ZERO, null);
    }

    private SessaoCaixa umaSessao() {
        return new SessaoCaixa(umUsuario(), new BigDecimal("100.00"));
    }

    private Usuario umUsuario() {
        return new Usuario("Caixa Pagamento Teste", "caixa.pagamento.teste." + Math.random(), "hash", PerfilUsuario.CAIXA);
    }
}
