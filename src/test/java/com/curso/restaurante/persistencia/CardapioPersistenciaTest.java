package com.curso.restaurante.persistencia;

import com.curso.restaurante.domain.Status;
import com.curso.restaurante.domain.cardapio.CategoriaCardapio;
import com.curso.restaurante.domain.cardapio.ItemCardapio;
import com.curso.restaurante.domain.cardapio.SecaoPreparo;
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
class CardapioPersistenciaTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Transactional
    void devePersistirERelerCategoriaEItemDeCardapio() {
        CategoriaCardapio categoria = new CategoriaCardapio("Pratos Principais Persistencia", "descrição", 1);
        ItemCardapio item = new ItemCardapio(
                "PRD-PERSIST-0001",
                "Risoto de Cogumelos",
                "Risoto cremoso com cogumelos frescos",
                new BigDecimal("48.50"),
                25,
                SecaoPreparo.COZINHA,
                true,
                true,
                new BigDecimal("8.000"),
                LocalDate.of(2026, 3, 10));

        categoria.adicionarItem(item);

        entityManager.persist(categoria);
        entityManager.persist(item);
        entityManager.flush();

        Long itemId = item.getId();
        entityManager.clear();

        ItemCardapio recuperado = entityManager.find(ItemCardapio.class, itemId);

        assertNotNull(recuperado);
        assertEquals("Risoto de Cogumelos", recuperado.getNome());
        assertEquals("Pratos Principais Persistencia", recuperado.getCategoria().getNome());
        assertEquals(Status.ATIVO, recuperado.getStatus());
        assertEquals(SecaoPreparo.COZINHA, recuperado.getSecaoPreparo());
    }

    @Test
    @Transactional
    void bancoDeveImpedirCodigoDuplicado() {
        Long categoriaId = inserirCategoriaDiretamente("Categoria Persistencia Unicidade");

        inserirItemDiretamente(categoriaId, "CODIGO-REPETIDO-PERSIST", "Primeiro item", "1.000", "10.00");

        assertThrows(
                DataIntegrityViolationException.class,
                () -> inserirItemDiretamente(
                        categoriaId, "CODIGO-REPETIDO-PERSIST", "Segundo item", "1.000", "20.00"));
    }

    @Test
    @Transactional
    void bancoDeveImpedirSaldoNegativo() {
        Long categoriaId = inserirCategoriaDiretamente("Categoria Persistencia Saldo");

        assertThrows(
                DataIntegrityViolationException.class,
                () -> inserirItemDiretamente(
                        categoriaId, "CODIGO-SALDO-NEGATIVO-PERSIST", "Item inválido", "-1.000", "10.00"));
    }

    @Test
    @Transactional
    void bancoDeveImpedirEstoqueMinimoNegativo() {
        Long categoriaId = inserirCategoriaDiretamente("Categoria Persistencia Estoque Minimo");

        assertThrows(
                DataIntegrityViolationException.class,
                () -> jdbcTemplate.update(
                        """
                        INSERT INTO item_cardapio (
                            codigo, nome, preco_venda, tempo_preparo_minutos, secao_preparo,
                            exige_preparo, controla_estoque, saldo_estoque, estoque_minimo, data_cadastro, status,
                            categoria_cardapio_id
                        )
                        VALUES (?, 'Item Estoque Mínimo Inválido', 10.00, 5, 'COZINHA', true, true, 1.000, -1, DATE '2026-03-10', 'ATIVO', ?)
                        """,
                        "CODIGO-ESTOQUE-MINIMO-NEGATIVO-PERSIST",
                        categoriaId));
    }

    @Test
    @Transactional
    void bancoDeveImpedirSecaoDePreparoInvalida() {
        Long categoriaId = inserirCategoriaDiretamente("Categoria Persistencia Secao");

        assertThrows(
                DataIntegrityViolationException.class,
                () -> jdbcTemplate.update(
                        """
                        INSERT INTO item_cardapio (
                            codigo, nome, preco_venda, tempo_preparo_minutos, secao_preparo,
                            exige_preparo, controla_estoque, saldo_estoque, estoque_minimo, data_cadastro, status,
                            categoria_cardapio_id
                        )
                        VALUES (?, 'Item Seção Inválida', 10.00, 5, 'PADARIA', true, true, 1.000, 0, DATE '2026-03-10', 'ATIVO', ?)
                        """,
                        "CODIGO-SECAO-INVALIDA-PERSIST",
                        categoriaId));
    }

    private Long inserirCategoriaDiretamente(String nome) {
        return jdbcTemplate.queryForObject(
                """
                INSERT INTO categoria_cardapio (nome, ordem_exibicao, status)
                VALUES (?, 1, 'ATIVO')
                RETURNING id
                """,
                Long.class,
                nome);
    }

    private void inserirItemDiretamente(
            Long categoriaId,
            String codigo,
            String nome,
            String saldo,
            String precoVenda) {
        jdbcTemplate.update(
                """
                INSERT INTO item_cardapio (
                    codigo, nome, preco_venda, tempo_preparo_minutos, secao_preparo,
                    exige_preparo, controla_estoque, saldo_estoque, estoque_minimo, data_cadastro, status,
                    categoria_cardapio_id
                )
                VALUES (?, ?, CAST(? AS NUMERIC), 5, 'COZINHA', true, true, CAST(? AS NUMERIC), 0, DATE '2026-03-10', 'ATIVO', ?)
                """,
                codigo,
                nome,
                precoVenda,
                saldo,
                categoriaId);
    }
}
