package com.curso.restaurante.persistencia;

import com.curso.restaurante.domain.mesa.Mesa;
import com.curso.restaurante.domain.mesa.StatusMesa;
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
class MesaPersistenciaTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Transactional
    void devePersistirERelerMesa() {
        Mesa mesa = new Mesa(101, 4, "Salão Principal Persistência");

        entityManager.persist(mesa);
        entityManager.flush();

        Long id = mesa.getId();
        entityManager.clear();

        Mesa recuperada = entityManager.find(Mesa.class, id);

        assertNotNull(recuperada);
        assertEquals(101, recuperada.getNumero());
        assertEquals(StatusMesa.LIVRE, recuperada.getStatus());
    }

    @Test
    @Transactional
    void bancoDeveImpedirNumeroDuplicado() {
        inserirMesaDiretamente(202, 4, "Setor A");

        assertThrows(
                DataIntegrityViolationException.class,
                () -> inserirMesaDiretamente(202, 6, "Setor B"));
    }

    @Test
    @Transactional
    void bancoDeveImpedirCapacidadeZeroOuNegativa() {
        assertThrows(
                DataIntegrityViolationException.class,
                () -> inserirMesaDiretamente(303, 0, "Setor C"));
    }

    private void inserirMesaDiretamente(int numero, int capacidade, String setor) {
        jdbcTemplate.update(
                """
                INSERT INTO mesa (numero, capacidade, setor, status)
                VALUES (?, ?, ?, 'LIVRE')
                """,
                numero,
                capacidade,
                setor);
    }
}
