package com.curso.restaurante.api.pedido;

import com.curso.restaurante.api.comum.CancelarRequest;
import com.curso.restaurante.api.comum.PaginaResponse;
import com.curso.restaurante.api.pedido.dto.AdicionarItemPedidoRequest;
import com.curso.restaurante.api.pedido.dto.CriarPedidoRequest;
import com.curso.restaurante.api.pedido.dto.ItemPedidoResponse;
import com.curso.restaurante.api.pedido.dto.PedidoResponse;
import com.curso.restaurante.domain.pedido.ItemPedido;
import com.curso.restaurante.domain.pedido.Pedido;
import com.curso.restaurante.domain.pedido.StatusPedido;
import com.curso.restaurante.service.cozinha.FilaCozinhaService;
import com.curso.restaurante.service.pedido.PedidoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
public class PedidoController {

    private final PedidoService pedidoService;
    private final FilaCozinhaService filaCozinhaService;

    public PedidoController(PedidoService pedidoService, FilaCozinhaService filaCozinhaService) {
        this.pedidoService = pedidoService;
        this.filaCozinhaService = filaCozinhaService;
    }

    @PostMapping("/api/comandas/{comandaId}/pedidos")
    public ResponseEntity<PedidoResponse> criar(
            @PathVariable Long comandaId, @RequestBody CriarPedidoRequest request, Authentication authentication) {
        Pedido pedido = pedidoService.criarPedido(comandaId, authentication.getName(), request.observacao());
        return ResponseEntity
                .created(URI.create("/api/pedidos/" + pedido.getId()))
                .body(PedidoResponse.de(pedido));
    }

    @GetMapping("/api/pedidos")
    public PaginaResponse<PedidoResponse> listar(
            @RequestParam(required = false) StatusPedido status,
            @RequestParam(required = false) Long comandaId,
            @RequestParam(required = false) Long solicitanteId,
            Pageable pageable) {
        return PaginaResponse.de(
                pedidoService.listar(status, comandaId, solicitanteId, pageable).map(PedidoResponse::de));
    }

    @GetMapping("/api/pedidos/{id}")
    public PedidoResponse buscarPorId(@PathVariable Long id) {
        return PedidoResponse.de(pedidoService.buscarPorId(id));
    }

    @PostMapping("/api/pedidos/{id}/itens")
    public ResponseEntity<ItemPedidoResponse> adicionarItem(
            @PathVariable Long id, @Valid @RequestBody AdicionarItemPedidoRequest request) {
        ItemPedido item = pedidoService.adicionarItem(
                id, request.itemCardapioId(), request.quantidade(), request.observacao());
        return ResponseEntity.status(HttpStatus.CREATED).body(ItemPedidoResponse.de(item));
    }

    @DeleteMapping("/api/pedidos/{id}/itens/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removerItem(@PathVariable Long id, @PathVariable Long itemId) {
        pedidoService.removerItem(id, itemId);
    }

    @PostMapping("/api/pedidos/{id}/enviar-para-preparo")
    public PedidoResponse enviarParaPreparo(@PathVariable Long id) {
        Pedido pedido = pedidoService.enviarParaPreparo(id);
        filaCozinhaService.enfileirarItensDoPedido(pedido);
        return PedidoResponse.de(pedidoService.buscarPorId(id));
    }

    @PostMapping("/api/pedidos/{id}/marcar-pronto")
    public PedidoResponse marcarComoPronto(@PathVariable Long id) {
        return PedidoResponse.de(pedidoService.marcarComoPronto(id));
    }

    @PostMapping("/api/pedidos/{id}/marcar-entregue")
    public PedidoResponse marcarComoEntregue(@PathVariable Long id) {
        return PedidoResponse.de(pedidoService.marcarComoEntregue(id));
    }

    @PostMapping("/api/pedidos/{id}/cancelar")
    public PedidoResponse cancelar(@PathVariable Long id, @Valid @RequestBody CancelarRequest request) {
        return PedidoResponse.de(pedidoService.cancelar(id, request.motivo()));
    }
}
