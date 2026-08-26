package com.curso.restaurante.persistencia;

import com.curso.restaurante.domain.Status;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
class UsuarioPersistenciaTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Transactional
    void devePersistirERelerUsuario() {
        Usuario usuario = new Usuario("Ana Caixa", "ana.caixa", "hash-bcrypt", PerfilUsuario.CAIXA);

        entityManager.persist(usuario);
        entityManager.flush();

        Long id = usuario.getId();
        entityManager.clear();

        Usuario recuperado = entityManager.find(Usuario.class, id);

        assertNotNull(recuperado);
        assertEquals("Ana Caixa", recuperado.getNome());
        assertEquals("ana.caixa", recuperado.getUsername());
        assertEquals(PerfilUsuario.CAIXA, recuperado.getPerfil());
        assertEquals(Status.ATIVO, recuperado.getStatus());
    }

    @Test
    @Transactional
    void bancoDeveImpedirUsernameDuplicado() {
        inserirUsuarioDiretamente("Primeiro Admin", "admin.duplicado", "hash-1", "ADMIN");

        assertThrows(
                DataIntegrityViolationException.class,
                () -> inserirUsuarioDiretamente("Segundo Admin", "admin.duplicado", "hash-2", "ADMIN"));
    }

    @Test
    @Transactional
    void bancoDeveImpedirPerfilInvalido() {
        assertThrows(
                DataIntegrityViolationException.class,
                () -> inserirUsuarioDiretamente("Perfil Inválido", "perfil.invalido", "hash", "GERENTE"));
    }

    private void inserirUsuarioDiretamente(String nome, String username, String senhaHash, String perfil) {
        jdbcTemplate.update(
                """
                INSERT INTO usuario (nome, username, senha_hash, perfil, status, criado_em)
                VALUES (?, ?, ?, ?, 'ATIVO', NOW())
                """,
                nome,
                username,
                senhaHash,
                perfil);
    }
}
