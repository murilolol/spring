package com.curso.restaurante.persistencia;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class EsquemaLiquibaseTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void deveRegistrarTodosOsChangeSetsDoCurso() {
        Integer quantidade = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM databasechangelog",
                Integer.class);

        assertTrue(quantidade >= 26);
    }

    @Test
    void deveTerAplicadoOChangeSetQueRemoveOCardapioLegado() {
        assertChangeSetAplicado("004-01-drop-produto");
        assertChangeSetAplicado("004-02-drop-categoria-produto");
    }

    @Test
    void deveTerAplicadoOsChangeSetsDoNovoCardapio() {
        assertChangeSetAplicado("005-01-create-categoria-cardapio");
        assertChangeSetAplicado("006-01-create-item-cardapio");
        assertChangeSetAplicado("006-03-foreign-key-categoria-cardapio");
    }

    private void assertChangeSetAplicado(String id) {
        Integer quantidade = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM databasechangelog WHERE id = ?",
                Integer.class,
                id);

        assertTrue(quantidade == 1, "changeset " + id + " deveria ter sido aplicado");
    }
}
