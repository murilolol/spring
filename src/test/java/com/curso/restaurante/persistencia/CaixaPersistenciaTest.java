package com.curso.restaurante.persistencia;

import com.curso.restaurante.domain.caixa.FormaPagamento;
import com.curso.restaurante.domain.caixa.Pagamento;
import com.curso.restaurante.domain.caixa.Sangria;
import com.curso.restaurante.domain.caixa.SessaoCaixa;
import com.curso.restaurante.domain.caixa.StatusSessaoCaixa;
import com.curso.restaurante.domain.comanda.Comanda;
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
class CaixaPersistenciaTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Transactional
    void devePersistirERelerSessaoCaixaSangriaEPagamento() {
        Usuario operador = new Usuario("Caixa Persist", "caixa.persist", "hash", PerfilUsuario.CAIXA);
        entityManager.persist(operador);

        SessaoCaixa sessao = new SessaoCaixa(operador, new BigDecimal("100.00"));
        entityManager.persist(sessao);

        Sangria sangria = new Sangria(sessao, new BigDecimal("20.00"), "Reforço de troco", operador);
        entityManager.persist(sangria);

        Mesa mesa = new Mesa(1301, 4, "Salão Persistência Caixa");
        entityManager.persist(mesa);
        Comanda comanda = new Comanda(
                "CMD-PERSIST-CAIXA-0001", TipoAtendimento.SALAO, mesa, null, operador, 2, BigDecimal.ZERO, null);
        entityManager.persist(comanda);

        Pagamento pagamento = new Pagamento(
                comanda, sessao, FormaPagamento.DINHEIRO, new BigDecimal("50.00"), new BigDecimal("60.00"), operador);
        entityManager.persist(pagamento);
        entityManager.flush();

        Long sessaoId = sessao.getId();
        entityManager.clear();

        SessaoCaixa recuperada = entityManager.find(SessaoCaixa.class, sessaoId);

        assertNotNull(recuperada);
        assertEquals(StatusSessaoCaixa.ABERTA, recuperada.getStatus());
    }

    @Test
    @Transactional
    void bancoDeveImpedirDuasSessoesDeCaixaAbertas() {
        Usuario operador = new Usuario("Caixa Dup Sessao", "caixa.dup.sessao", "hash", PerfilUsuario.CAIXA);
        entityManager.persist(operador);
        entityManager.flush();

        inserirSessaoCaixaDiretamente(operador.getId());

        assertThrows(
                DataIntegrityViolationException.class,
                () -> inserirSessaoCaixaDiretamente(operador.getId()));
    }

    private void inserirSessaoCaixaDiretamente(Long usuarioAberturaId) {
        jdbcTemplate.update(
                """
                INSERT INTO sessao_caixa (usuario_abertura_id, valor_abertura, status, aberta_em)
                VALUES (?, 0, 'ABERTA', NOW())
                """,
                usuarioAberturaId);
    }

    @Test
    @Transactional
    void bancoDeveImpedirSangriaComValorZeroOuNegativo() {
        Usuario operador = new Usuario("Caixa Sangria Invalida", "caixa.sangria.invalida", "hash", PerfilUsuario.CAIXA);
        entityManager.persist(operador);
        SessaoCaixa sessao = new SessaoCaixa(operador, BigDecimal.ZERO);
        entityManager.persist(sessao);
        entityManager.flush();

        assertThrows(
                DataIntegrityViolationException.class,
                () -> jdbcTemplate.update(
                        """
                        INSERT INTO sangria (sessao_caixa_id, valor, motivo, usuario_id, registrada_em)
                        VALUES (?, 0, 'motivo', ?, NOW())
                        """,
                        sessao.getId(),
                        operador.getId()));
    }
}
