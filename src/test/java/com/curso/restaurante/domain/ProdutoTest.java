package com.curso.restaurante.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProdutoTest {

    @Test
    void deveCriarProdutoAtivoComDadosValidos() {
        Produto produto = novoProduto("30.000", "9.00");

        assertEquals("PRD-0001", produto.getCodigo());
        assertEquals("Suco Natural", produto.getDescricao());
        assertEquals(Status.ATIVO, produto.getStatus());
        assertEquals(LocalDate.of(2026, 8, 20), produto.getDataCadastro());
    }

    @Test
    void deveCalcularValorDoEstoque() {
        Produto produto = novoProduto("30.000", "9.00");

        BigDecimal valorEstoque = produto.calcularValorEstoque();

        assertEquals(0, new BigDecimal("270.00").compareTo(valorEstoque));
    }

    @Test
    void deveReceberERetirarEstoque() {
        Produto produto = novoProduto("30.000", "9.00");

        produto.receberEstoque(new BigDecimal("10.000"));
        produto.retirarEstoque(new BigDecimal("5.000"));

        assertEquals(0, new BigDecimal("35.000").compareTo(produto.getSaldoEstoque()));
    }

    @Test
    void naoDeveRetirarQuantidadeMaiorQueOSaldo() {
        Produto produto = novoProduto("30.000", "9.00");

        IllegalArgumentException excecao = assertThrows(
                IllegalArgumentException.class,
                () -> produto.retirarEstoque(new BigDecimal("30.001")));

        assertEquals("Saldo de estoque insuficiente", excecao.getMessage());
    }

    @Test
    void naoDeveCriarProdutoComCodigoEmBranco() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Produto(
                        "  ",
                        "Suco Natural",
                        BigDecimal.ZERO,
                        new BigDecimal("9.00"),
                        LocalDate.of(2026, 8, 20)));
    }

    @Test
    void naoDeveCriarProdutoComSaldoNegativo() {
        assertThrows(
                IllegalArgumentException.class,
                () -> novoProduto("-0.001", "9.00"));
    }

    @Test
    void deveAlterarOStatusPorComportamentoExplicito() {
        Produto produto = novoProduto("30.000", "9.00");

        produto.inativar();
        assertEquals(Status.INATIVO, produto.getStatus());

        produto.ativar();
        assertEquals(Status.ATIVO, produto.getStatus());
    }

    private Produto novoProduto(String saldo, String valorUnitario) {
        return new Produto(
                "PRD-0001",
                "Suco Natural",
                new BigDecimal(saldo),
                new BigDecimal(valorUnitario),
                LocalDate.of(2026, 8, 20));
    }
}
