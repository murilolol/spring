package com.curso.restaurante.service.cozinha;

import com.curso.restaurante.domain.cardapio.SecaoPreparo;
import com.curso.restaurante.domain.comum.RecursoNaoEncontradoException;
import com.curso.restaurante.domain.cozinha.PreparoItem;
import com.curso.restaurante.domain.cozinha.PrioridadePreparo;
import com.curso.restaurante.domain.cozinha.StatusPreparo;
import com.curso.restaurante.domain.pedido.ItemPedido;
import com.curso.restaurante.domain.pedido.Pedido;
import com.curso.restaurante.domain.usuario.Usuario;
import com.curso.restaurante.repository.cozinha.PreparoItemRepository;
import com.curso.restaurante.repository.usuario.UsuarioRepository;
import com.curso.restaurante.service.pedido.PedidoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class FilaCozinhaService {

    private final PreparoItemRepository preparoItemRepository;
    private final UsuarioRepository usuarioRepository;
    private final PedidoService pedidoService;

    public FilaCozinhaService(
            PreparoItemRepository preparoItemRepository,
            UsuarioRepository usuarioRepository,
            PedidoService pedidoService) {
        this.preparoItemRepository = preparoItemRepository;
        this.usuarioRepository = usuarioRepository;
        this.pedidoService = pedidoService;
    }

    public List<PreparoItem> enfileirarItensDoPedido(Pedido pedido) {
        List<PreparoItem> enfileirados = new ArrayList<>();

        for (ItemPedido itemPedido : pedido.getItens()) {
            if (itemPedido.getItemCardapio().exigeEntradaNaFilaDeCozinha()) {
                int tempoEstimado = itemPedido.getItemCardapio().getTempoPreparoMinutos() * itemPedido.getQuantidade();
                PreparoItem preparoItem = new PreparoItem(
                        itemPedido, itemPedido.getItemCardapio().getSecaoPreparo(), tempoEstimado);
                enfileirados.add(preparoItemRepository.save(preparoItem));
            }
        }

        if (enfileirados.isEmpty()) {
            pedidoService.marcarComoPronto(pedido.getId());
        }

        return enfileirados;
    }

    public PreparoItem buscarPorId(Long id) {
        return preparoItemRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Item de preparo não encontrado"));
    }

    public Page<PreparoItem> listarFila(
            SecaoPreparo secao, StatusPreparo status, PrioridadePreparo prioridade, Pageable pageable) {
        Specification<PreparoItem> especificacao = Specification.allOf();

        if (secao != null) {
            especificacao = especificacao.and((raiz, consulta, cb) -> cb.equal(raiz.get("secao"), secao));
        }
        if (status != null) {
            especificacao = especificacao.and((raiz, consulta, cb) -> cb.equal(raiz.get("status"), status));
        }
        if (prioridade != null) {
            especificacao = especificacao.and((raiz, consulta, cb) -> cb.equal(raiz.get("prioridade"), prioridade));
        }

        Sort ordemDaFila = Sort.by(Sort.Direction.DESC, "prioridade").and(Sort.by(Sort.Direction.ASC, "enfileiradoEm"));
        Pageable pageableOrdenado = pageable.getSort().isSorted()
                ? pageable
                : org.springframework.data.domain.PageRequest.of(
                        pageable.getPageNumber(), pageable.getPageSize(), ordemDaFila);

        return preparoItemRepository.findAll(especificacao, pageableOrdenado);
    }

    public PreparoItem iniciar(Long id, String usernameResponsavel) {
        PreparoItem preparoItem = buscarPorId(id);
        Usuario responsavel = usuarioRepository.findByUsername(usernameResponsavel)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));

        preparoItem.iniciar(responsavel);
        return preparoItem;
    }

    public PreparoItem concluir(Long id) {
        PreparoItem preparoItem = buscarPorId(id);
        preparoItem.concluir();

        cascatearParaPedidoProntoSeAplicavel(preparoItem);

        return preparoItem;
    }

    public PreparoItem cancelar(Long id) {
        PreparoItem preparoItem = buscarPorId(id);
        preparoItem.cancelar();

        cascatearParaPedidoProntoSeAplicavel(preparoItem);

        return preparoItem;
    }

    public PreparoItem alterarPrioridade(Long id, PrioridadePreparo prioridade) {
        PreparoItem preparoItem = buscarPorId(id);
        preparoItem.alterarPrioridade(prioridade);
        return preparoItem;
    }

    private void cascatearParaPedidoProntoSeAplicavel(PreparoItem preparoItem) {
        Long pedidoId = preparoItem.getItemPedido().getPedido().getId();

        List<PreparoItem> pendentes = preparoItemRepository.findByItemPedido_Pedido_IdAndStatusNot(
                pedidoId, StatusPreparo.CANCELADO);
        boolean existePendente = pendentes.stream()
                .anyMatch(item -> item.getStatus() != StatusPreparo.CONCLUIDO);
        boolean existeConcluido = pendentes.stream()
                .anyMatch(item -> item.getStatus() == StatusPreparo.CONCLUIDO);

        if (!existePendente && existeConcluido) {
            pedidoService.marcarComoPronto(pedidoId);
        }
    }
}
