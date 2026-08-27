package com.curso.restaurante.persistencia;

import com.curso.restaurante.domain.Status;
import com.curso.restaurante.domain.fornecedor.Fornecedor;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
class FornecedorPersistenciaTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Transactional
    void devePersistirERelerFornecedor() {
        Fornecedor fornecedor = new Fornecedor("Distribuidora Persistencia Ltda", "11122233344455");

        entityManager.persist(fornecedor);
        entityManager.flush();

        Long id = fornecedor.getId();
        entityManager.clear();

        Fornecedor recuperado = entityManager.find(Fornecedor.class, id);

        assertNotNull(recuperado);
        assertEquals("Distribuidora Persistencia Ltda", recuperado.getRazaoSocial());
        assertEquals(Status.ATIVO, recuperado.getStatus());
    }

    @Test
    @Transactional
    void bancoDeveImpedirCnpjDuplicado() {
        inserirFornecedorDiretamente("Primeiro Fornecedor Persist", "22233344455566");

        assertThrows(
                DataIntegrityViolationException.class,
                () -> inserirFornecedorDiretamente("Segundo Fornecedor Persist", "22233344455566"));
    }

    @Test
    @Transactional
    void bancoDeveImpedirStatusInvalido() {
        assertThrows(
                DataIntegrityViolationException.class,
                () -> jdbcTemplate.update(
                        """
                        INSERT INTO fornecedor (razao_social, cnpj, status)
                        VALUES ('Fornecedor Status Invalido', '33344455566677', 'SUSPENSO')
                        """));
    }

    private void inserirFornecedorDiretamente(String razaoSocial, String cnpj) {
        jdbcTemplate.update(
                """
                INSERT INTO fornecedor (razao_social, cnpj, status)
                VALUES (?, ?, 'ATIVO')
                """,
                razaoSocial,
                cnpj);
    }
}
