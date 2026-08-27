package com.curso.restaurante.domain.cardapio;

import com.curso.restaurante.domain.Status;
import com.curso.restaurante.domain.fornecedor.Fornecedor;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemCardapioTest {

    @Test
    void deveCriarItemAtivoComDadosValidos() {
        ItemCardapio item = novoItem("30.000", "9.00", true);

        assertEquals("BEB-0001", item.getCodigo());
        assertEquals("Suco Natural", item.getNome());
        assertEquals("Suco de laranja natural", item.getDescricao());
        assertEquals(Status.ATIVO, item.getStatus());
        assertEquals(LocalDate.of(2026, 8, 20), item.getDataCadastro());
        assertEquals(5, item.getTempoPreparoMinutos());
        assertEquals(SecaoPreparo.BAR, item.getSecaoPreparo());
        assertTrue(item.isExigePreparo());
        assertTrue(item.isControlaEstoque());
    }

    @Test
    void deveCalcularValorEmEstoque() {
        ItemCardapio item = novoItem("30.000", "9.00", true);

        BigDecimal valorEstoque = item.calcularValorEmEstoque();

        assertEquals(0, new BigDecimal("270.00").compareTo(valorEstoque));
    }

    @Test
    void deveReceberEBaixarEstoque() {
        ItemCardapio item = novoItem("30.000", "9.00", true);

        item.receberEstoque(new BigDecimal("10.000"));
        item.baixarEstoque(new BigDecimal("5.000"));

        assertEquals(0, new BigDecimal("35.000").compareTo(item.getSaldoEstoque()));
    }

    @Test
    void naoDeveBaixarQuantidadeMaiorQueOSaldo() {
        ItemCardapio item = novoItem("30.000", "9.00", true);

        IllegalArgumentException excecao = assertThrows(
                IllegalArgumentException.class,
                () -> item.baixarEstoque(new BigDecimal("30.001")));

        assertEquals("Saldo de estoque insuficiente", excecao.getMessage());
    }

    @Test
    void naoDeveCriarItemComCodigoEmBranco() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ItemCardapio(
                        "  ",
                        "Suco Natural",
                        "Suco de laranja natural",
                        new BigDecimal("9.00"),
                        5,
                        SecaoPreparo.BAR,
                        true,
                        true,
                        BigDecimal.ZERO,
                        LocalDate.of(2026, 8, 20)));
    }

    @Test
    void naoDeveCriarItemComSaldoNegativo() {
        assertThrows(IllegalArgumentException.class, () -> novoItem("-0.001", "9.00", true));
    }

    @Test
    void naoDeveCriarItemComPrecoNegativo() {
        assertThrows(IllegalArgumentException.class, () -> novoItem("30.000", "-9.00", true));
    }

    @Test
    void naoDeveCriarItemComTempoDePreparoNegativo() {
        IllegalArgumentException excecao = assertThrows(
                IllegalArgumentException.class,
                () -> new ItemCardapio(
                        "BEB-0001",
                        "Suco Natural",
                        "Suco de laranja natural",
                        new BigDecimal("9.00"),
                        -1,
                        SecaoPreparo.BAR,
                        true,
                        true,
                        new BigDecimal("30.000"),
                        LocalDate.of(2026, 8, 20)));

        assertEquals("Tempo de preparo não pode ser negativo", excecao.getMessage());
    }

    @Test
    void deveAtualizarDados() {
        ItemCardapio item = novoItem("30.000", "9.00", true);

        item.atualizarDados(
                "Suco Especial", "Descrição nova", new BigDecimal("11.00"), 8, SecaoPreparo.SOBREMESA, false, false);

        assertEquals("Suco Especial", item.getNome());
        assertEquals("Descrição nova", item.getDescricao());
        assertEquals(0, new BigDecimal("11.00").compareTo(item.getPrecoVenda()));
        assertEquals(8, item.getTempoPreparoMinutos());
        assertEquals(SecaoPreparo.SOBREMESA, item.getSecaoPreparo());
        assertFalse(item.isExigePreparo());
        assertFalse(item.isControlaEstoque());
    }

    @Test
    void deveAlterarPreco() {
        ItemCardapio item = novoItem("30.000", "9.00", true);

        item.alterarPreco(new BigDecimal("12.50"));

        assertEquals(0, new BigDecimal("12.50").compareTo(item.getPrecoVenda()));
    }

    @Test
    void deveAlterarOStatusPorComportamentoExplicito() {
        ItemCardapio item = novoItem("30.000", "9.00", true);

        item.inativar();
        assertEquals(Status.INATIVO, item.getStatus());

        item.ativar();
        assertEquals(Status.ATIVO, item.getStatus());
    }

    @Test
    void estaDisponivelParaDeveConsiderarSaldoQuandoControlaEstoque() {
        ItemCardapio item = novoItem("10.000", "9.00", true);

        assertTrue(item.estaDisponivelPara(new BigDecimal("10.000")));
        assertFalse(item.estaDisponivelPara(new BigDecimal("10.001")));
    }

    @Test
    void estaDisponivelParaDeveIgnorarSaldoQuandoNaoControlaEstoque() {
        ItemCardapio item = novoItem("0.000", "9.00", false);

        assertTrue(item.estaDisponivelPara(new BigDecimal("999.000")));
    }

    @Test
    void exigeEntradaNaFilaDeCozinhaDeveRefletirExigePreparo() {
        ItemCardapio exigePreparo = novoItem("10.000", "9.00", true);
        ItemCardapio naoExigePreparo = new ItemCardapio(
                "BEB-0002",
                "Refrigerante Lata",
                "Refrigerante em lata",
                new BigDecimal("6.00"),
                0,
                SecaoPreparo.BAR,
                false,
                true,
                new BigDecimal("50.000"),
                LocalDate.of(2026, 8, 20));

        assertTrue(exigePreparo.exigeEntradaNaFilaDeCozinha());
        assertFalse(naoExigePreparo.exigeEntradaNaFilaDeCozinha());
    }

    @Test
    void construtorAntigoDeveDefinirEstoqueMinimoComoZero() {
        ItemCardapio item = novoItem("30.000", "9.00", true);

        assertEquals(0, BigDecimal.ZERO.compareTo(item.getEstoqueMinimo()));
    }

    @Test
    void construtorAntigoNaoDeveAssociarFornecedor() {
        ItemCardapio item = novoItem("30.000", "9.00", true);

        assertEquals(null, item.getFornecedor());
    }

    @Test
    void naoDeveCriarItemComEstoqueMinimoNegativo() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ItemCardapio(
                        "BEB-0001",
                        "Suco Natural",
                        "Suco de laranja natural",
                        new BigDecimal("9.00"),
                        5,
                        SecaoPreparo.BAR,
                        true,
                        true,
                        new BigDecimal("30.000"),
                        LocalDate.of(2026, 8, 20),
                        new BigDecimal("-1")));
    }

    @Test
    void deveCriarItemComEstoqueMinimoInformado() {
        ItemCardapio item = new ItemCardapio(
                "BEB-0001",
                "Suco Natural",
                "Suco de laranja natural",
                new BigDecimal("9.00"),
                5,
                SecaoPreparo.BAR,
                true,
                true,
                new BigDecimal("30.000"),
                LocalDate.of(2026, 8, 20),
                new BigDecimal("5.000"));

        assertEquals(0, new BigDecimal("5.000").compareTo(item.getEstoqueMinimo()));
    }

    @Test
    void deveAssociarERemoverFornecedor() {
        ItemCardapio item = novoItem("30.000", "9.00", true);
        Fornecedor fornecedor = new Fornecedor("Distribuidora Bom Sabor Ltda", "12345678901234");

        item.associarFornecedor(fornecedor);
        assertEquals(fornecedor, item.getFornecedor());

        item.removerFornecedor();
        assertEquals(null, item.getFornecedor());
    }

    @Test
    void naoDeveAssociarFornecedorNulo() {
        ItemCardapio item = novoItem("30.000", "9.00", true);

        assertThrows(NullPointerException.class, () -> item.associarFornecedor(null));
    }

    @Test
    void deveAlterarEstoqueMinimo() {
        ItemCardapio item = novoItem("30.000", "9.00", true);

        item.alterarEstoqueMinimo(new BigDecimal("2.000"));

        assertEquals(0, new BigDecimal("2.000").compareTo(item.getEstoqueMinimo()));
    }

    @Test
    void naoDeveAlterarEstoqueMinimoParaNegativo() {
        ItemCardapio item = novoItem("30.000", "9.00", true);

        assertThrows(
                IllegalArgumentException.class,
                () -> item.alterarEstoqueMinimo(new BigDecimal("-1")));
    }

    private ItemCardapio novoItem(String saldo, String precoVenda, boolean controlaEstoque) {
        return new ItemCardapio(
                "BEB-0001",
                "Suco Natural",
                "Suco de laranja natural",
                new BigDecimal(precoVenda),
                5,
                SecaoPreparo.BAR,
                true,
                controlaEstoque,
                new BigDecimal(saldo),
                LocalDate.of(2026, 8, 20));
    }
}
