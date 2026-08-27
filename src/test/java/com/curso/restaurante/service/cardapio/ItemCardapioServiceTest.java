package com.curso.restaurante.service.cardapio;

import com.curso.restaurante.domain.Status;
import com.curso.restaurante.domain.cardapio.CategoriaCardapio;
import com.curso.restaurante.domain.cardapio.ItemCardapio;
import com.curso.restaurante.domain.cardapio.SecaoPreparo;
import com.curso.restaurante.domain.comum.ConflitoDeEstadoException;
import com.curso.restaurante.domain.comum.RecursoNaoEncontradoException;
import com.curso.restaurante.domain.fornecedor.Fornecedor;
import com.curso.restaurante.service.fornecedor.FornecedorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ItemCardapioServiceTest {

    @Autowired
    private ItemCardapioService itemCardapioService;

    @Autowired
    private CategoriaCardapioService categoriaCardapioService;

    @Autowired
    private FornecedorService fornecedorService;

    @Test
    void deveCriarItemComEstoqueMinimoEFornecedor() {
        CategoriaCardapio categoria = categoriaCardapioService.criar("Categoria Item Fornecedor Svc", null, 1);
        Fornecedor fornecedor = fornecedorService.cadastrar("Fornecedor Item Svc", "55566677788899");

        ItemCardapio item = itemCardapioService.criar(
                categoria.getId(), "ITEM-FORN-SVC-0001", "Suco Svc", null, new BigDecimal("9.00"), 5,
                SecaoPreparo.BAR, true, true, new BigDecimal("10.000"), LocalDate.of(2026, 8, 20),
                new BigDecimal("2.000"), fornecedor.getId());

        assertEquals(0, new BigDecimal("2.000").compareTo(item.getEstoqueMinimo()));
        assertEquals(fornecedor.getId(), item.getFornecedor().getId());
    }

    @Test
    void deveCriarItemSemFornecedorUsandoOMetodoAntigo() {
        CategoriaCardapio categoria = categoriaCardapioService.criar("Categoria Item Sem Fornecedor Svc", null, 1);

        ItemCardapio item = itemCardapioService.criar(
                categoria.getId(), "ITEM-SEM-FORN-SVC", "Suco Svc", null, new BigDecimal("9.00"), 5,
                SecaoPreparo.BAR, true, true, new BigDecimal("10.000"), LocalDate.of(2026, 8, 20));

        assertEquals(0, BigDecimal.ZERO.compareTo(item.getEstoqueMinimo()));
    }

    @Test
    void deveRejeitarFornecedorInexistenteENaoDevePersistirNada() {
        CategoriaCardapio categoria = categoriaCardapioService.criar("Categoria Item Fornecedor Inexistente Svc", null, 1);

        assertThrows(
                RecursoNaoEncontradoException.class,
                () -> itemCardapioService.criar(
                        categoria.getId(), "ITEM-FORN-INEXISTENTE-SVC", "Suco Svc", null, new BigDecimal("9.00"), 5,
                        SecaoPreparo.BAR, true, true, new BigDecimal("10.000"), LocalDate.of(2026, 8, 20),
                        BigDecimal.ZERO, -1L));

        var pagina = itemCardapioService.listar(categoria.getId(), null, null, null, PageRequest.of(0, 50));
        assertTrue(pagina.getContent().isEmpty());
    }

    @Test
    void deveAssociarERemoverFornecedor() {
        CategoriaCardapio categoria = categoriaCardapioService.criar("Categoria Associar Fornecedor Svc", null, 1);
        Fornecedor fornecedor = fornecedorService.cadastrar("Fornecedor Associar Svc", "66677788899900");
        ItemCardapio criado = itemCardapioService.criar(
                categoria.getId(), "ITEM-ASSOCIAR-FORN-SVC", "Suco Svc", null, new BigDecimal("9.00"), 5,
                SecaoPreparo.BAR, true, true, BigDecimal.ZERO, LocalDate.of(2026, 8, 20));

        ItemCardapio associado = itemCardapioService.associarFornecedor(criado.getId(), fornecedor.getId());
        assertEquals(fornecedor.getId(), associado.getFornecedor().getId());

        ItemCardapio removido = itemCardapioService.removerFornecedor(criado.getId());
        assertEquals(null, removido.getFornecedor());
    }

    @Test
    void deveAlterarEstoqueMinimo() {
        CategoriaCardapio categoria = categoriaCardapioService.criar("Categoria Alterar Estoque Minimo Svc", null, 1);
        ItemCardapio criado = itemCardapioService.criar(
                categoria.getId(), "ITEM-ALTERAR-ESTOQUE-MINIMO-SVC", "Suco Svc", null, new BigDecimal("9.00"), 5,
                SecaoPreparo.BAR, true, true, BigDecimal.ZERO, LocalDate.of(2026, 8, 20));

        ItemCardapio alterado = itemCardapioService.alterarEstoqueMinimo(criado.getId(), new BigDecimal("3.000"));

        assertEquals(0, new BigDecimal("3.000").compareTo(alterado.getEstoqueMinimo()));
    }

    @Test
    void deveCriarItem() {
        CategoriaCardapio categoria = categoriaCardapioService.criar("Categoria Item Svc", null, 1);

        ItemCardapio item = itemCardapioService.criar(
                categoria.getId(), "ITEM-SVC-0001", "Suco Svc", null, new BigDecimal("9.00"), 5,
                SecaoPreparo.BAR, true, true, new BigDecimal("10.000"), LocalDate.of(2026, 8, 20));

        assertEquals("Suco Svc", item.getNome());
        assertEquals(categoria.getId(), item.getCategoria().getId());
    }

    @Test
    void deveRejeitarCodigoDuplicado() {
        CategoriaCardapio categoria = categoriaCardapioService.criar("Categoria Item Dup Svc", null, 1);
        itemCardapioService.criar(
                categoria.getId(), "ITEM-DUP-SVC", "Item 1", null, new BigDecimal("9.00"), 5,
                SecaoPreparo.BAR, true, true, BigDecimal.ZERO, LocalDate.of(2026, 8, 20));

        assertThrows(
                ConflitoDeEstadoException.class,
                () -> itemCardapioService.criar(
                        categoria.getId(), "ITEM-DUP-SVC", "Item 2", null, new BigDecimal("5.00"), 5,
                        SecaoPreparo.BAR, true, true, BigDecimal.ZERO, LocalDate.of(2026, 8, 20)));
    }

    @Test
    void deveRejeitarCategoriaInexistente() {
        assertThrows(
                RecursoNaoEncontradoException.class,
                () -> itemCardapioService.criar(
                        -1L, "ITEM-CAT-INEXISTENTE-SVC", "Item", null, new BigDecimal("9.00"), 5,
                        SecaoPreparo.BAR, true, true, BigDecimal.ZERO, LocalDate.of(2026, 8, 20)));
    }

    @Test
    void buscarPorIdDeveLancarQuandoNaoExiste() {
        assertThrows(RecursoNaoEncontradoException.class, () -> itemCardapioService.buscarPorId(-1L));
    }

    @Test
    void deveListarComFiltroDeSecao() {
        CategoriaCardapio categoria = categoriaCardapioService.criar("Categoria Filtro Item Svc", null, 1);
        itemCardapioService.criar(
                categoria.getId(), "ITEM-BAR-SVC", "Item Bar", null, new BigDecimal("9.00"), 5,
                SecaoPreparo.BAR, true, false, BigDecimal.ZERO, LocalDate.of(2026, 8, 20));
        itemCardapioService.criar(
                categoria.getId(), "ITEM-COZINHA-SVC", "Item Cozinha", null, new BigDecimal("9.00"), 5,
                SecaoPreparo.COZINHA, true, false, BigDecimal.ZERO, LocalDate.of(2026, 8, 20));

        var pagina = itemCardapioService.listar(null, SecaoPreparo.BAR, null, null, PageRequest.of(0, 50));

        assertTrue(pagina.getContent().stream().allMatch(i -> i.getSecaoPreparo() == SecaoPreparo.BAR));
    }

    @Test
    void deveAtualizarDados() {
        CategoriaCardapio categoria = categoriaCardapioService.criar("Categoria Atualizar Item Svc", null, 1);
        ItemCardapio criado = itemCardapioService.criar(
                categoria.getId(), "ITEM-ATUALIZAR-SVC", "Nome Antigo", null, new BigDecimal("9.00"), 5,
                SecaoPreparo.BAR, true, true, BigDecimal.ZERO, LocalDate.of(2026, 8, 20));

        ItemCardapio atualizado = itemCardapioService.atualizar(
                criado.getId(), "Nome Novo", "Descrição nova", new BigDecimal("12.00"), 10,
                SecaoPreparo.SOBREMESA, false, false);

        assertEquals("Nome Novo", atualizado.getNome());
        assertEquals(0, new BigDecimal("12.00").compareTo(atualizado.getPrecoVenda()));
    }

    @Test
    void deveRegistrarEntradaESaidaDeEstoque() {
        CategoriaCardapio categoria = categoriaCardapioService.criar("Categoria Estoque Svc", null, 1);
        ItemCardapio criado = itemCardapioService.criar(
                categoria.getId(), "ITEM-ESTOQUE-SVC", "Item Estoque", null, new BigDecimal("9.00"), 5,
                SecaoPreparo.BAR, true, true, new BigDecimal("10.000"), LocalDate.of(2026, 8, 20));

        ItemCardapio recebido = itemCardapioService.registrarEntradaEstoque(criado.getId(), new BigDecimal("5.000"));
        assertEquals(0, new BigDecimal("15.000").compareTo(recebido.getSaldoEstoque()));

        ItemCardapio baixado = itemCardapioService.registrarSaidaEstoque(criado.getId(), new BigDecimal("3.000"));
        assertEquals(0, new BigDecimal("12.000").compareTo(baixado.getSaldoEstoque()));
    }

    @Test
    void deveInativarEAtivar() {
        CategoriaCardapio categoria = categoriaCardapioService.criar("Categoria Toggle Item Svc", null, 1);
        ItemCardapio criado = itemCardapioService.criar(
                categoria.getId(), "ITEM-TOGGLE-SVC", "Item Toggle", null, new BigDecimal("9.00"), 5,
                SecaoPreparo.BAR, true, true, BigDecimal.ZERO, LocalDate.of(2026, 8, 20));

        ItemCardapio inativado = itemCardapioService.inativar(criado.getId());
        assertEquals(Status.INATIVO, inativado.getStatus());

        ItemCardapio ativado = itemCardapioService.ativar(criado.getId());
        assertEquals(Status.ATIVO, ativado.getStatus());
    }
}
