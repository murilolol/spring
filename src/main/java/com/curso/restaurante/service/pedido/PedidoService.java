package com.curso.restaurante.service.pedido;

import com.curso.restaurante.domain.cardapio.ItemCardapio;
import com.curso.restaurante.domain.comanda.Comanda;
import com.curso.restaurante.domain.comum.RecursoNaoEncontradoException;
import com.curso.restaurante.domain.comum.RegraDeNegocioException;
import com.curso.restaurante.domain.pedido.ItemPedido;
import com.curso.restaurante.domain.pedido.Pedido;
import com.curso.restaurante.domain.pedido.StatusPedido;
import com.curso.restaurante.domain.usuario.Usuario;
import com.curso.restaurante.repository.cardapio.ItemCardapioRepository;
import com.curso.restaurante.repository.comanda.ComandaRepository;
import com.curso.restaurante.repository.pedido.ItemPedidoRepository;
import com.curso.restaurante.repository.pedido.PedidoRepository;
import com.curso.restaurante.repository.usuario.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ItemPedidoRepository itemPedidoRepository;
    private final ComandaRepository comandaRepository;
    private final ItemCardapioRepository itemCardapioRepository;
    private final UsuarioRepository usuarioRepository;

    public PedidoService(
            PedidoRepository pedidoRepository,
            ItemPedidoRepository itemPedidoRepository,
            ComandaRepository comandaRepository,
            ItemCardapioRepository itemCardapioRepository,
            UsuarioRepository usuarioRepository) {
        this.pedidoRepository = pedidoRepository;
        this.itemPedidoRepository = itemPedidoRepository;
        this.comandaRepository = comandaRepository;
        this.itemCardapioRepository = itemCardapioRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public Pedido criarPedido(Long comandaId, String usernameSolicitante, String observacao) {
        Comanda comanda = comandaRepository.findById(comandaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Comanda não encontrada"));
        Usuario solicitante = usuarioRepository.findByUsername(usernameSolicitante)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));

        Pedido pedido = new Pedido(gerarCodigo(), comanda, solicitante, observacao);
        return pedidoRepository.save(pedido);
    }

    public Pedido buscarPorId(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido não encontrado"));
    }

    public Page<Pedido> listar(StatusPedido status, Long comandaId, Long solicitanteId, Pageable pageable) {
        Specification<Pedido> especificacao = Specification.allOf();

        if (status != null) {
            especificacao = especificacao.and((raiz, consulta, cb) -> cb.equal(raiz.get("status"), status));
        }
        if (comandaId != null) {
            especificacao = especificacao.and(
                    (raiz, consulta, cb) -> cb.equal(raiz.get("comanda").get("id"), comandaId));
        }
        if (solicitanteId != null) {
            especificacao = especificacao.and(
                    (raiz, consulta, cb) -> cb.equal(raiz.get("solicitante").get("id"), solicitanteId));
        }

        return pedidoRepository.findAll(especificacao, pageable);
    }

    public ItemPedido adicionarItem(Long pedidoId, Long itemCardapioId, int quantidade, String observacao) {
        Pedido pedido = buscarPorId(pedidoId);
        ItemCardapio itemCardapio = itemCardapioRepository.findById(itemCardapioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Item de cardápio não encontrado"));

        ItemPedido item = pedido.adicionarItem(itemCardapio, quantidade, observacao);
        return itemPedidoRepository.save(item);
    }

    public void removerItem(Long pedidoId, Long itemPedidoId) {
        Pedido pedido = buscarPorId(pedidoId);
        ItemPedido item = itemPedidoRepository.findById(itemPedidoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Item de pedido não encontrado"));

        pedido.removerItem(item);
    }

    public Pedido enviarParaPreparo(Long id) {
        Pedido pedido = buscarPorId(id);

        for (ItemPedido item : pedido.getItens()) {
            ItemCardapio itemCardapio = item.getItemCardapio();
            if (itemCardapio.isControlaEstoque()
                    && !itemCardapio.estaDisponivelPara(BigDecimal.valueOf(item.getQuantidade()))) {
                throw new RegraDeNegocioException(
                        "Saldo em estoque insuficiente para o item " + itemCardapio.getCodigo());
            }
        }

        for (ItemPedido item : pedido.getItens()) {
            ItemCardapio itemCardapio = item.getItemCardapio();
            if (itemCardapio.isControlaEstoque()) {
                itemCardapio.baixarEstoque(BigDecimal.valueOf(item.getQuantidade()));
            }
        }

        pedido.enviarParaPreparo();
        return pedido;
    }

    public Pedido marcarComoPronto(Long id) {
        Pedido pedido = buscarPorId(id);
        pedido.marcarComoPronto();
        return pedido;
    }

    public Pedido marcarComoEntregue(Long id) {
        Pedido pedido = buscarPorId(id);
        pedido.marcarComoEntregue();
        return pedido;
    }

    public Pedido marcarComoPago(Long id) {
        Pedido pedido = buscarPorId(id);
        pedido.marcarComoPago();
        return pedido;
    }

    public Pedido cancelar(Long id, String motivo) {
        Pedido pedido = buscarPorId(id);
        boolean estoqueFoiDeduzido = pedido.getEnviadoPreparoEm() != null;

        pedido.cancelar(motivo);

        if (estoqueFoiDeduzido) {
            for (ItemPedido item : pedido.getItens()) {
                ItemCardapio itemCardapio = item.getItemCardapio();
                if (itemCardapio.isControlaEstoque()) {
                    itemCardapio.receberEstoque(BigDecimal.valueOf(item.getQuantidade()));
                }
            }
        }

        return pedido;
    }

    private String gerarCodigo() {
        LocalDate hoje = LocalDate.now();
        LocalDateTime inicioDoDia = hoje.atStartOfDay();
        LocalDateTime fimDoDia = hoje.plusDays(1).atStartOfDay();

        long quantidadeHoje = pedidoRepository.countByAbertoEmBetween(inicioDoDia, fimDoDia);

        return "PED-" + hoje.format(DateTimeFormatter.BASIC_ISO_DATE) + "-" + String.format("%04d", quantidadeHoje + 1);
    }
}
