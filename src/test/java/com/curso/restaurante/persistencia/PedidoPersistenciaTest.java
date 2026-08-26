package com.curso.restaurante.persistencia;

import com.curso.restaurante.domain.cardapio.CategoriaCardapio;
import com.curso.restaurante.domain.cardapio.ItemCardapio;
import com.curso.restaurante.domain.cardapio.SecaoPreparo;
import com.curso.restaurante.domain.comanda.Comanda;
import com.curso.restaurante.domain.comanda.TipoAtendimento;
import com.curso.restaurante.domain.mesa.Mesa;
import com.curso.restaurante.domain.pedido.ItemPedido;
import com.curso.restaurante.domain.pedido.Pedido;
import com.curso.restaurante.domain.pedido.StatusPedido;
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
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
class PedidoPersistenciaTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Transactional
    void devePersistirERelerPedidoComItens() {
        Usuario responsavel = new Usuario("Garçom Persist Pedido", "garcom.persist.pedido", "hash", PerfilUsuario.GARCOM);
        Mesa mesa = new Mesa(1001, 4, "Salão Persistência Pedido");
        entityManager.persist(responsavel);
        entityManager.persist(mesa);

        Comanda comanda = new Comanda(
                "CMD-PERSIST-PEDIDO-0001", TipoAtendimento.SALAO, mesa, null, responsavel, 2,
                BigDecimal.ZERO, null);
        entityManager.persist(comanda);

        CategoriaCardapio categoria = new CategoriaCardapio("Categoria Persist Pedido", null, 1);
        ItemCardapio itemCardapio = new ItemCardapio(
                "ITEM-PERSIST-PEDIDO-0001", "Suco Natural", null, new BigDecimal("9.00"), 5,
                SecaoPreparo.BAR, true, true, new BigDecimal("30.000"), LocalDate.of(2026, 8, 20));
        categoria.adicionarItem(itemCardapio);
        entityManager.persist(categoria);
        entityManager.persist(itemCardapio);

        Pedido pedido = new Pedido("PED-PERSIST-0001", comanda, responsavel, null);
        ItemPedido item = pedido.adicionarItem(itemCardapio, 2, "sem gelo");
        entityManager.persist(pedido);
        entityManager.persist(item);
        entityManager.flush();

        Long pedidoId = pedido.getId();
        entityManager.clear();

        Pedido recuperado = entityManager.find(Pedido.class, pedidoId);

        assertNotNull(recuperado);
        assertEquals(StatusPedido.ABERTO, recuperado.getStatus());
        assertEquals(1, recuperado.getItens().size());
        assertEquals("sem gelo", recuperado.getItens().getFirst().getObservacao());
    }

    @Test
    @Transactional
    void bancoDeveImpedirCodigoDuplicado() {
        Usuario responsavel = new Usuario("Garçom Codigo Dup Pedido", "garcom.codigo.dup.pedido", "hash", PerfilUsuario.GARCOM);
        Mesa mesa = new Mesa(1002, 4, "Salão Codigo Dup Pedido");
        entityManager.persist(responsavel);
        entityManager.persist(mesa);
        Comanda comanda = new Comanda(
                "CMD-CODIGO-DUP-PEDIDO", TipoAtendimento.SALAO, mesa, null, responsavel, 2,
                BigDecimal.ZERO, null);
        entityManager.persist(comanda);
        entityManager.flush();

        inserirPedidoDiretamente("PED-DUPLICADO", comanda.getId(), responsavel.getId());

        assertThrows(
                DataIntegrityViolationException.class,
                () -> inserirPedidoDiretamente("PED-DUPLICADO", comanda.getId(), responsavel.getId()));
    }

    private void inserirPedidoDiretamente(String codigo, Long comandaId, Long usuarioId) {
        jdbcTemplate.update(
                """
                INSERT INTO pedido (codigo, comanda_id, usuario_solicitante_id, status, aberto_em)
                VALUES (?, ?, ?, 'ABERTO', NOW())
                """,
                codigo,
                comandaId,
                usuarioId);
    }
}
