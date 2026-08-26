package com.curso.restaurante.persistencia;

import com.curso.restaurante.domain.Status;
import com.curso.restaurante.domain.cliente.Cliente;
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
class ClientePersistenciaTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Transactional
    void devePersistirERelerCliente() {
        Cliente cliente = new Cliente(
                "Maria Persistencia", "11122233344", "(45) 90000-0001", "maria.persist@example.com", null, null);

        entityManager.persist(cliente);
        entityManager.flush();

        Long id = cliente.getId();
        entityManager.clear();

        Cliente recuperado = entityManager.find(Cliente.class, id);

        assertNotNull(recuperado);
        assertEquals("Maria Persistencia", recuperado.getNome());
        assertEquals(Status.ATIVO, recuperado.getStatus());
    }

    @Test
    @Transactional
    void devePermitirVariosClientesSemDocumento() {
        entityManager.persist(new Cliente("Cliente Sem Doc 1", null, "(45) 90000-0002", null, null, null));
        entityManager.persist(new Cliente("Cliente Sem Doc 2", null, "(45) 90000-0003", null, null, null));
        entityManager.flush();
    }

    @Test
    @Transactional
    void bancoDeveImpedirDocumentoDuplicado() {
        inserirClienteDiretamente("Primeiro Documento", "99988877766", "(45) 90000-0004");

        assertThrows(
                DataIntegrityViolationException.class,
                () -> inserirClienteDiretamente("Segundo Documento", "99988877766", "(45) 90000-0005"));
    }

    private void inserirClienteDiretamente(String nome, String documento, String telefone) {
        jdbcTemplate.update(
                """
                INSERT INTO cliente (nome, documento, telefone, data_cadastro, status)
                VALUES (?, ?, ?, CURRENT_DATE, 'ATIVO')
                """,
                nome,
                documento,
                telefone);
    }
}
