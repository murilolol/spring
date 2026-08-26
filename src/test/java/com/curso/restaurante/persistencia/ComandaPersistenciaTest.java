package com.curso.restaurante.persistencia;

import com.curso.restaurante.domain.comanda.Comanda;
import com.curso.restaurante.domain.comanda.StatusComanda;
import com.curso.restaurante.domain.comanda.TipoAtendimento;
import com.curso.restaurante.domain.mesa.Mesa;
import com.curso.restaurante.domain.usuario.PerfilUsuario;
import com.curso.restaurante.domain.usuario.Usuario;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
class ComandaPersistenciaTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Transactional
    void devePersistirERelerComanda() {
        Usuario responsavel = new Usuario("Garçom Persist Comanda", "garcom.persist.comanda", "hash", PerfilUsuario.GARCOM);
        Mesa mesa = new Mesa(701, 4, "Salão Persistência Comanda");
        entityManager.persist(responsavel);
        entityManager.persist(mesa);

        Comanda comanda = new Comanda(
                "CMD-PERSIST-0001", TipoAtendimento.SALAO, mesa, null, responsavel, 3,
                new BigDecimal("10.00"), "Aniversário");
        entityManager.persist(comanda);
        entityManager.flush();

        Long id = comanda.getId();
        entityManager.clear();

        Comanda recuperada = entityManager.find(Comanda.class, id);

        assertNotNull(recuperada);
        assertEquals("CMD-PERSIST-0001", recuperada.getCodigo());
        assertEquals(StatusComanda.ABERTA, recuperada.getStatus());
        assertEquals(701, recuperada.getMesa().getNumero());
    }

    @Test
    @Transactional
    void bancoDeveImpedirComandaSemMesaNemCliente() {
        Usuario responsavel = new Usuario("Garçom Sem Vinculo", "garcom.sem.vinculo.comanda", "hash", PerfilUsuario.GARCOM);
        entityManager.persist(responsavel);
        entityManager.flush();

        assertThrows(
                DataIntegrityViolationException.class,
                () -> jdbcTemplate.update(
                        """
                        INSERT INTO comanda (
                            codigo, tipo_atendimento, usuario_responsavel_id, numero_pessoas,
                            percentual_taxa_servico, status, aberta_em
                        )
                        VALUES ('CMD-SEM-VINCULO', 'BALCAO', ?, 1, 0.00, 'ABERTA', NOW())
                        """,
                        responsavel.getId()));
    }

    @Test
    @Transactional
    void bancoDeveImpedirCodigoDuplicado() {
        Usuario responsavel = new Usuario("Garçom Codigo Dup", "garcom.codigo.dup.comanda", "hash", PerfilUsuario.GARCOM);
        Mesa mesa = new Mesa(702, 4, "Salão Codigo Dup");
        entityManager.persist(responsavel);
        entityManager.persist(mesa);
        entityManager.flush();

        inserirComandaDiretamente("CMD-DUPLICADA", mesa.getId(), responsavel.getId());

        assertThrows(
                DataIntegrityViolationException.class,
                () -> inserirComandaDiretamente("CMD-DUPLICADA", mesa.getId(), responsavel.getId()));
    }

    private void inserirComandaDiretamente(String codigo, Long mesaId, Long responsavelId) {
        jdbcTemplate.update(
                """
                INSERT INTO comanda (
                    codigo, tipo_atendimento, mesa_id, usuario_responsavel_id, numero_pessoas,
                    percentual_taxa_servico, status, aberta_em
                )
                VALUES (?, 'SALAO', ?, ?, 2, 0.00, 'ABERTA', NOW())
                """,
                codigo,
                mesaId,
                responsavelId);
    }
}
