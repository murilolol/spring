package com.curso.restaurante.service.cozinha;

import com.curso.restaurante.domain.cardapio.CategoriaCardapio;
import com.curso.restaurante.domain.cardapio.ItemCardapio;
import com.curso.restaurante.domain.cardapio.SecaoPreparo;
import com.curso.restaurante.domain.comanda.Comanda;
import com.curso.restaurante.domain.comum.RecursoNaoEncontradoException;
import com.curso.restaurante.domain.cozinha.PreparoItem;
import com.curso.restaurante.domain.cozinha.StatusPreparo;
import com.curso.restaurante.domain.pedido.Pedido;
import com.curso.restaurante.domain.pedido.StatusPedido;
import com.curso.restaurante.domain.usuario.PerfilUsuario;
import com.curso.restaurante.repository.cardapio.CategoriaCardapioRepository;
import com.curso.restaurante.repository.cardapio.ItemCardapioRepository;
import com.curso.restaurante.service.comanda.ComandaService;
import com.curso.restaurante.service.mesa.MesaService;
import com.curso.restaurante.service.pedido.PedidoService;
import com.curso.restaurante.service.usuario.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class FilaCozinhaServiceTest {

    @Autowired
    private FilaCozinhaService filaCozinhaService;

    @Autowired
    private ComandaService comandaService;

    @Autowired
    private MesaService mesaService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private CategoriaCardapioRepository categoriaCardapioRepository;

    @Autowired
    private ItemCardapioRepository itemCardapioRepository;

    @Test
    void deveEnfileirarApenasItensQueExigemPreparo() {
        Pedido pedido = umPedidoAberto();
        ItemCardapio comPreparo = umItemCardapio(true, SecaoPreparo.COZINHA);
        ItemCardapio semPreparo = umItemCardapio(false, SecaoPreparo.BAR);
        pedidoService.adicionarItem(pedido.getId(), comPreparo.getId(), 2, null);
        pedidoService.adicionarItem(pedido.getId(), semPreparo.getId(), 1, null);
        Pedido enviado = pedidoService.enviarParaPreparo(pedido.getId());

        List<PreparoItem> enfileirados = filaCozinhaService.enfileirarItensDoPedido(enviado);

        assertEquals(1, enfileirados.size());
        assertEquals(SecaoPreparo.COZINHA, enfileirados.getFirst().getSecao());
    }

    @Test
    void pedidoSemItensQueExigemPreparoDeveIrDiretoParaPronto() {
        Pedido pedido = umPedidoAberto();
        ItemCardapio semPreparo = umItemCardapio(false, SecaoPreparo.BAR);
        pedidoService.adicionarItem(pedido.getId(), semPreparo.getId(), 1, null);
        Pedido enviado = pedidoService.enviarParaPreparo(pedido.getId());

        filaCozinhaService.enfileirarItensDoPedido(enviado);

        Pedido recarregado = pedidoService.buscarPorId(enviado.getId());
        assertEquals(StatusPedido.PRONTO, recarregado.getStatus());
    }

    @Test
    void buscarPorIdDeveLancarQuandoNaoExiste() {
        assertThrows(RecursoNaoEncontradoException.class, () -> filaCozinhaService.buscarPorId(-1L));
    }

    @Test
    void deveListarFilaOrdenadaPorPrioridadeEEnfileiramento() {
        Pedido pedido = umPedidoAberto();
        ItemCardapio item = umItemCardapio(true, SecaoPreparo.COZINHA);
        pedidoService.adicionarItem(pedido.getId(), item.getId(), 1, null);
        Pedido enviado = pedidoService.enviarParaPreparo(pedido.getId());
        filaCozinhaService.enfileirarItensDoPedido(enviado);

        var pagina = filaCozinhaService.listarFila(SecaoPreparo.COZINHA, StatusPreparo.AGUARDANDO, null, PageRequest.of(0, 50));

        assertTrue(pagina.getContent().stream().allMatch(p -> p.getSecao() == SecaoPreparo.COZINHA));
    }

    @Test
    void deveIniciarEConcluirEMarcarPedidoComoPronto() {
        Pedido pedido = umPedidoAberto();
        ItemCardapio item = umItemCardapio(true, SecaoPreparo.COZINHA);
        pedidoService.adicionarItem(pedido.getId(), item.getId(), 1, null);
        Pedido enviado = pedidoService.enviarParaPreparo(pedido.getId());
        List<PreparoItem> fila = filaCozinhaService.enfileirarItensDoPedido(enviado);
        PreparoItem preparoItem = fila.getFirst();

        var cozinheiro = usuarioService.criar(
                "Cozinheiro Svc " + Math.random(), "cozinheiro.svc." + Math.random(), "senha", PerfilUsuario.COZINHA);

        filaCozinhaService.iniciar(preparoItem.getId(), cozinheiro.getUsername());
        PreparoItem concluido = filaCozinhaService.concluir(preparoItem.getId());

        assertEquals(StatusPreparo.CONCLUIDO, concluido.getStatus());
        Pedido recarregado = pedidoService.buscarPorId(enviado.getId());
        assertEquals(StatusPedido.PRONTO, recarregado.getStatus());
    }

    @Test
    void deveCancelarItemDePreparo() {
        Pedido pedido = umPedidoAberto();
        ItemCardapio item = umItemCardapio(true, SecaoPreparo.COZINHA);
        pedidoService.adicionarItem(pedido.getId(), item.getId(), 1, null);
        Pedido enviado = pedidoService.enviarParaPreparo(pedido.getId());
        List<PreparoItem> fila = filaCozinhaService.enfileirarItensDoPedido(enviado);

        PreparoItem cancelado = filaCozinhaService.cancelar(fila.getFirst().getId());

        assertEquals(StatusPreparo.CANCELADO, cancelado.getStatus());
    }

    @Test
    void deveAlterarPrioridade() {
        Pedido pedido = umPedidoAberto();
        ItemCardapio item = umItemCardapio(true, SecaoPreparo.COZINHA);
        pedidoService.adicionarItem(pedido.getId(), item.getId(), 1, null);
        Pedido enviado = pedidoService.enviarParaPreparo(pedido.getId());
        List<PreparoItem> fila = filaCozinhaService.enfileirarItensDoPedido(enviado);

        PreparoItem alterado = filaCozinhaService.alterarPrioridade(
                fila.getFirst().getId(), com.curso.restaurante.domain.cozinha.PrioridadePreparo.URGENTE);

        assertEquals(com.curso.restaurante.domain.cozinha.PrioridadePreparo.URGENTE, alterado.getPrioridade());
    }

    private Pedido umPedidoAberto() {
        var responsavel = usuarioService.criar(
                "Garçom Fila Svc " + Math.random(), "garcom.fila.svc." + Math.random(), "senha", PerfilUsuario.GARCOM);
        var mesa = mesaService.criar((int) (Math.random() * 1000000), 4, "Salão Fila Svc");
        Comanda comanda = comandaService.abrir(
                com.curso.restaurante.domain.comanda.TipoAtendimento.SALAO,
                mesa.getId(), null, responsavel.getUsername(), 2, BigDecimal.ZERO, null);
        return pedidoService.criarPedido(comanda.getId(), responsavel.getUsername(), null);
    }

    private ItemCardapio umItemCardapio(boolean exigePreparo, SecaoPreparo secao) {
        CategoriaCardapio categoria = categoriaCardapioRepository.save(
                new CategoriaCardapio("Categoria Fila Svc " + Math.random(), null, 1));
        ItemCardapio item = new ItemCardapio(
                "ITEM-FILA-SVC-" + Math.random(), "Item Fila Svc", null, new BigDecimal("9.00"), 5,
                secao, exigePreparo, false, BigDecimal.ZERO, LocalDate.of(2026, 8, 20));
        categoria.adicionarItem(item);
        return itemCardapioRepository.save(item);
    }
}
