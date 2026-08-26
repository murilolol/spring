package com.curso.restaurante.domain.cardapio;

import com.curso.restaurante.domain.Status;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CategoriaCardapioTest {

    @Test
    void deveCriarCategoriaAtivaComOsDadosInformados() {
        CategoriaCardapio categoria = new CategoriaCardapio("Bebidas", "Sucos, refrigerantes e água", 3);

        assertEquals("Bebidas", categoria.getNome());
        assertEquals("Sucos, refrigerantes e água", categoria.getDescricao());
        assertEquals(3, categoria.getOrdemExibicao());
        assertEquals(Status.ATIVO, categoria.getStatus());
    }

    @Test
    void deveRejeitarNomeEmBranco() {
        assertThrows(IllegalArgumentException.class, () -> new CategoriaCardapio("   ", "descrição", 1));
    }

    @Test
    void deveRejeitarOrdemDeExibicaoNegativa() {
        assertThrows(IllegalArgumentException.class, () -> new CategoriaCardapio("Bebidas", "descrição", -1));
    }

    @Test
    void deveAceitarDescricaoNula() {
        CategoriaCardapio categoria = new CategoriaCardapio("Bebidas", null, 1);

        assertEquals(null, categoria.getDescricao());
    }

    @Test
    void deveAdicionarItemEManejarOsDoisLadosDaAssociacao() {
        CategoriaCardapio categoria = new CategoriaCardapio("Bebidas", "descrição", 1);
        ItemCardapio item = novoItem("BEB-0001");

        categoria.adicionarItem(item);

        assertEquals(1, categoria.getItens().size());
        assertSame(item, categoria.getItens().getFirst());
        assertSame(categoria, item.getCategoria());
    }

    @Test
    void naoDeveAdicionarItemNulo() {
        CategoriaCardapio categoria = new CategoriaCardapio("Bebidas", "descrição", 1);

        assertThrows(NullPointerException.class, () -> categoria.adicionarItem(null));
    }

    @Test
    void naoDeveAdicionarDoisItensComOMesmoCodigo() {
        CategoriaCardapio categoria = new CategoriaCardapio("Bebidas", "descrição", 1);
        categoria.adicionarItem(novoItem("BEB-0001"));

        IllegalArgumentException excecao = assertThrows(
                IllegalArgumentException.class,
                () -> categoria.adicionarItem(novoItem("BEB-0001")));

        assertEquals("Código já utilizado na categoria", excecao.getMessage());
    }

    @Test
    void naoDevePermitirQueItemPertencaADuasCategorias() {
        CategoriaCardapio bebidas = new CategoriaCardapio("Bebidas", "descrição", 1);
        CategoriaCardapio sobremesas = new CategoriaCardapio("Sobremesas", "descrição", 2);
        ItemCardapio item = novoItem("BEB-0001");
        bebidas.adicionarItem(item);

        IllegalStateException excecao = assertThrows(
                IllegalStateException.class,
                () -> sobremesas.adicionarItem(item));

        assertEquals("Item já pertence a outra categoria", excecao.getMessage());
    }

    @Test
    void naoDeveExporUmaListaInternaModificavel() {
        CategoriaCardapio categoria = new CategoriaCardapio("Bebidas", "descrição", 1);
        categoria.adicionarItem(novoItem("BEB-0001"));

        assertThrows(
                UnsupportedOperationException.class,
                () -> categoria.getItens().add(novoItem("BEB-0002")));
    }

    @Test
    void deveRenomear() {
        CategoriaCardapio categoria = new CategoriaCardapio("Bebidas", "descrição", 1);

        categoria.renomear("Bebidas Geladas");

        assertEquals("Bebidas Geladas", categoria.getNome());
    }

    @Test
    void deveAlterarDescricao() {
        CategoriaCardapio categoria = new CategoriaCardapio("Bebidas", "descrição antiga", 1);

        categoria.alterarDescricao("descrição nova");

        assertEquals("descrição nova", categoria.getDescricao());
    }

    @Test
    void deveAlterarDescricaoParaNula() {
        CategoriaCardapio categoria = new CategoriaCardapio("Bebidas", "descrição antiga", 1);

        categoria.alterarDescricao(null);

        assertEquals(null, categoria.getDescricao());
    }

    @Test
    void deveAlterarOrdemDeExibicao() {
        CategoriaCardapio categoria = new CategoriaCardapio("Bebidas", "descrição", 1);

        categoria.alterarOrdemExibicao(5);

        assertEquals(5, categoria.getOrdemExibicao());
    }

    @Test
    void deveInativarEAtivar() {
        CategoriaCardapio categoria = new CategoriaCardapio("Bebidas", "descrição", 1);

        categoria.inativar();
        assertEquals(Status.INATIVO, categoria.getStatus());

        categoria.ativar();
        assertEquals(Status.ATIVO, categoria.getStatus());
        assertTrue(categoria.getStatus() == Status.ATIVO);
    }

    private ItemCardapio novoItem(String codigo) {
        return new ItemCardapio(
                codigo,
                "Suco Natural",
                "Suco de laranja natural",
                new BigDecimal("9.00"),
                5,
                SecaoPreparo.BAR,
                true,
                true,
                new BigDecimal("30.000"),
                LocalDate.of(2026, 8, 20));
    }
}
