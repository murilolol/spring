package com.curso.restaurante.domain;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
class PersistenciaJpaTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Transactional
    void devePersistirERelerCategoriaEProduto() {
        CategoriaProduto categoria = new CategoriaProduto("Pratos Principais");
        Produto produto = new Produto(
                "PRD-0002",
                "Risoto de Cogumelos",
                new BigDecimal("8.000"),
                new BigDecimal("48.50"),
                LocalDate.of(2026, 3, 10));

        categoria.adicionarProduto(produto);

        entityManager.persist(categoria);
        entityManager.persist(produto);
        entityManager.flush();

        Long produtoId = produto.getId();
        entityManager.clear();

        Produto produtoRecuperado = entityManager.find(Produto.class, produtoId);

        assertNotNull(produtoRecuperado);
        assertEquals("Risoto de Cogumelos", produtoRecuperado.getDescricao());
        assertEquals("Pratos Principais", produtoRecuperado.getCategoria().getNome());
        assertEquals(Status.ATIVO, produtoRecuperado.getStatus());
    }

    @Test
    void deveRegistrarTodosOsChangeSetsDoCurso() {
        Integer quantidade = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM databasechangelog",
                Integer.class);

        assertEquals(8, quantidade);
    }

    @Test
    @Transactional
    void bancoDeveImpedirCodigoDuplicado() {
        Long categoriaId = inserirCategoriaDiretamente("Categoria para unicidade");

        inserirProdutoDiretamente(categoriaId, "CODIGO-REPETIDO", "Primeiro produto", "1.000", "10.00");

        assertThrows(
                DataIntegrityViolationException.class,
                () -> inserirProdutoDiretamente(
                        categoriaId,
                        "CODIGO-REPETIDO",
                        "Segundo produto",
                        "1.000",
                        "20.00"));
    }

    @Test
    @Transactional
    void bancoDeveImpedirSaldoNegativo() {
        Long categoriaId = inserirCategoriaDiretamente("Categoria para saldo");

        assertThrows(
                DataIntegrityViolationException.class,
                () -> inserirProdutoDiretamente(
                        categoriaId,
                        "CODIGO-SALDO-NEGATIVO",
                        "Produto inválido",
                        "-1.000",
                        "10.00"));
    }

    private Long inserirCategoriaDiretamente(String nome) {
        return jdbcTemplate.queryForObject(
                """
                INSERT INTO categoria_produto (nome, status)
                VALUES (?, 'ATIVO')
                RETURNING id
                """,
                Long.class,
                nome);
    }

    private void inserirProdutoDiretamente(
            Long categoriaId,
            String codigo,
            String descricao,
            String saldo,
            String valor) {
        jdbcTemplate.update(
                """
                INSERT INTO produto (
                    codigo,
                    descricao,
                    saldo_estoque,
                    valor_unitario,
                    data_cadastro,
                    status,
                    categoria_produto_id
                )
                VALUES (?, ?, CAST(? AS NUMERIC), CAST(? AS NUMERIC), DATE '2026-03-10', 'ATIVO', ?)
                """,
                codigo,
                descricao,
                saldo,
                valor,
                categoriaId);
    }
}
