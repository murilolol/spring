package com.curso.restaurante.persistencia;

import com.curso.restaurante.domain.cardapio.CategoriaCardapio;
import com.curso.restaurante.domain.cardapio.ItemCardapio;
import com.curso.restaurante.domain.cardapio.SecaoPreparo;
import com.curso.restaurante.domain.comanda.Comanda;
import com.curso.restaurante.domain.comanda.TipoAtendimento;
import com.curso.restaurante.domain.cozinha.PreparoItem;
import com.curso.restaurante.domain.cozinha.StatusPreparo;
import com.curso.restaurante.domain.mesa.Mesa;
import com.curso.restaurante.domain.pedido.ItemPedido;
import com.curso.restaurante.domain.pedido.Pedido;
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
class PreparoItemPersistenciaTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Transactional
    void devePersistirERelerPreparoItem() {
        Usuario responsavel = new Usuario("Garçom Persist Preparo", "garcom.persist.preparo", "hash", PerfilUsuario.GARCOM);
        Mesa mesa = new Mesa(1201, 4, "Salão Persistência Preparo");
        entityManager.persist(responsavel);
        entityManager.persist(mesa);

        Comanda comanda = new Comanda(
                "CMD-PERSIST-PREPARO-0001", TipoAtendimento.SALAO, mesa, null, responsavel, 2,
                BigDecimal.ZERO, null);
        entityManager.persist(comanda);

        CategoriaCardapio categoria = new CategoriaCardapio("Categoria Persist Preparo", null, 1);
        ItemCardapio itemCardapio = new ItemCardapio(
                "ITEM-PERSIST-PREPARO-0001", "Risoto", null, new BigDecimal("48.50"), 25,
                SecaoPreparo.COZINHA, true, false, BigDecimal.ZERO, LocalDate.of(2026, 8, 20));
        categoria.adicionarItem(itemCardapio);
        entityManager.persist(categoria);
        entityManager.persist(itemCardapio);

        Pedido pedido = new Pedido("PED-PERSIST-PREPARO-0001", comanda, responsavel, null);
        ItemPedido itemPedido = pedido.adicionarItem(itemCardapio, 1, null);
        entityManager.persist(pedido);
        entityManager.persist(itemPedido);

        PreparoItem preparoItem = new PreparoItem(itemPedido, SecaoPreparo.COZINHA, 25);
        entityManager.persist(preparoItem);
        entityManager.flush();

        Long preparoItemId = preparoItem.getId();
        entityManager.clear();

        PreparoItem recuperado = entityManager.find(PreparoItem.class, preparoItemId);

        assertNotNull(recuperado);
        assertEquals(StatusPreparo.AGUARDANDO, recuperado.getStatus());
        assertEquals(SecaoPreparo.COZINHA, recuperado.getSecao());
    }

    @Test
    @Transactional
    void bancoDeveImpedirDoisPreparosParaOMesmoItemDePedido() {
        Usuario responsavel = new Usuario("Garçom Dup Preparo", "garcom.dup.preparo", "hash", PerfilUsuario.GARCOM);
        Mesa mesa = new Mesa(1202, 4, "Salão Dup Preparo");
        entityManager.persist(responsavel);
        entityManager.persist(mesa);
        Comanda comanda = new Comanda(
                "CMD-DUP-PREPARO-0001", TipoAtendimento.SALAO, mesa, null, responsavel, 2, BigDecimal.ZERO, null);
        entityManager.persist(comanda);
        CategoriaCardapio categoria = new CategoriaCardapio("Categoria Dup Preparo", null, 1);
        ItemCardapio itemCardapio = new ItemCardapio(
                "ITEM-DUP-PREPARO-0001", "Risoto", null, new BigDecimal("48.50"), 25,
                SecaoPreparo.COZINHA, true, false, BigDecimal.ZERO, LocalDate.of(2026, 8, 20));
        categoria.adicionarItem(itemCardapio);
        entityManager.persist(categoria);
        entityManager.persist(itemCardapio);
        Pedido pedido = new Pedido("PED-DUP-PREPARO-0001", comanda, responsavel, null);
        ItemPedido itemPedido = pedido.adicionarItem(itemCardapio, 1, null);
        entityManager.persist(pedido);
        entityManager.persist(itemPedido);
        entityManager.flush();

        jdbcTemplate.update(
                """
                INSERT INTO preparo_item (item_pedido_id, secao, status, prioridade, tempo_estimado_minutos, enfileirado_em)
                VALUES (?, 'COZINHA', 'AGUARDANDO', 'NORMAL', 25, NOW())
                """,
                itemPedido.getId());

        assertThrows(
                DataIntegrityViolationException.class,
                () -> jdbcTemplate.update(
                        """
                        INSERT INTO preparo_item (item_pedido_id, secao, status, prioridade, tempo_estimado_minutos, enfileirado_em)
                        VALUES (?, 'COZINHA', 'AGUARDANDO', 'NORMAL', 25, NOW())
                        """,
                        itemPedido.getId()));
    }
}
