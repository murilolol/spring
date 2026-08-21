package com.curso.restaurante.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CategoriaProdutoTest {

    @Test
    void deveAdicionarProdutoEManejarOsDoisLadosDaAssociacao() {
        CategoriaProduto categoria = new CategoriaProduto("Bebidas");
        Produto produto = novoProduto("PRD-0001");

        categoria.adicionarProduto(produto);

        assertEquals(1, categoria.getProdutos().size());
        assertSame(produto, categoria.getProdutos().getFirst());
        assertSame(categoria, produto.getCategoria());
    }

    @Test
    void naoDeveAdicionarProdutoNulo() {
        CategoriaProduto categoria = new CategoriaProduto("Bebidas");

        assertThrows(NullPointerException.class, () -> categoria.adicionarProduto(null));
    }

    @Test
    void naoDeveAdicionarDoisProdutosComOMesmoCodigo() {
        CategoriaProduto categoria = new CategoriaProduto("Bebidas");
        categoria.adicionarProduto(novoProduto("PRD-0001"));

        IllegalArgumentException excecao = assertThrows(
                IllegalArgumentException.class,
                () -> categoria.adicionarProduto(novoProduto("PRD-0001")));

        assertEquals("Código já utilizado na categoria", excecao.getMessage());
    }

    @Test
    void naoDevePermitirQueProdutoPertençaADuasCategorias() {
        CategoriaProduto bebidas = new CategoriaProduto("Bebidas");
        CategoriaProduto sobremesas = new CategoriaProduto("Sobremesas");
        Produto produto = novoProduto("PRD-0001");
        bebidas.adicionarProduto(produto);

        IllegalStateException excecao = assertThrows(
                IllegalStateException.class,
                () -> sobremesas.adicionarProduto(produto));

        assertEquals("Produto já pertence a outra categoria", excecao.getMessage());
    }

    @Test
    void naoDeveExporUmaListaInternaModificavel() {
        CategoriaProduto categoria = new CategoriaProduto("Bebidas");
        Produto produto = novoProduto("PRD-0001");
        categoria.adicionarProduto(produto);

        assertThrows(
                UnsupportedOperationException.class,
                () -> categoria.getProdutos().add(novoProduto("PRD-0002")));
    }

    private Produto novoProduto(String codigo) {
        return new Produto(
                codigo,
                "Suco Natural",
                new BigDecimal("30.000"),
                new BigDecimal("9.00"),
                LocalDate.of(2026, 8, 20));
    }
}
