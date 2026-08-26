package com.curso.restaurante.api.cardapio;

import com.curso.restaurante.api.cardapio.dto.AtualizarItemCardapioRequest;
import com.curso.restaurante.api.cardapio.dto.CriarItemCardapioRequest;
import com.curso.restaurante.api.cardapio.dto.ItemCardapioResponse;
import com.curso.restaurante.api.cardapio.dto.MovimentoEstoqueRequest;
import com.curso.restaurante.api.comum.PaginaResponse;
import com.curso.restaurante.domain.Status;
import com.curso.restaurante.domain.cardapio.ItemCardapio;
import com.curso.restaurante.domain.cardapio.SecaoPreparo;
import com.curso.restaurante.service.cardapio.ItemCardapioService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/itens-cardapio")
public class ItemCardapioController {

    private final ItemCardapioService itemCardapioService;

    public ItemCardapioController(ItemCardapioService itemCardapioService) {
        this.itemCardapioService = itemCardapioService;
    }

    @PostMapping
    public ResponseEntity<ItemCardapioResponse> criar(@Valid @RequestBody CriarItemCardapioRequest request) {
        ItemCardapio item = itemCardapioService.criar(
                request.categoriaId(), request.codigo(), request.nome(), request.descricao(),
                request.precoVenda(), request.tempoPreparoMinutos(), request.secaoPreparo(),
                request.exigePreparo(), request.controlaEstoque(), request.saldoEstoque(),
                request.dataCadastro());
        return ResponseEntity
                .created(URI.create("/api/itens-cardapio/" + item.getId()))
                .body(ItemCardapioResponse.de(item));
    }

    @GetMapping
    public PaginaResponse<ItemCardapioResponse> listar(
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) SecaoPreparo secao,
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) String nome,
            Pageable pageable) {
        return PaginaResponse.de(
                itemCardapioService.listar(categoriaId, secao, status, nome, pageable).map(ItemCardapioResponse::de));
    }

    @GetMapping("/{id}")
    public ItemCardapioResponse buscarPorId(@PathVariable Long id) {
        return ItemCardapioResponse.de(itemCardapioService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ItemCardapioResponse atualizar(@PathVariable Long id, @Valid @RequestBody AtualizarItemCardapioRequest request) {
        return ItemCardapioResponse.de(itemCardapioService.atualizar(
                id, request.nome(), request.descricao(), request.precoVenda(), request.tempoPreparoMinutos(),
                request.secaoPreparo(), request.exigePreparo(), request.controlaEstoque()));
    }

    @PostMapping("/{id}/entradas-estoque")
    public ItemCardapioResponse entradaEstoque(@PathVariable Long id, @Valid @RequestBody MovimentoEstoqueRequest request) {
        return ItemCardapioResponse.de(itemCardapioService.registrarEntradaEstoque(id, request.quantidade()));
    }

    @PostMapping("/{id}/saidas-estoque")
    public ItemCardapioResponse saidaEstoque(@PathVariable Long id, @Valid @RequestBody MovimentoEstoqueRequest request) {
        return ItemCardapioResponse.de(itemCardapioService.registrarSaidaEstoque(id, request.quantidade()));
    }

    @PostMapping("/{id}/ativar")
    public ItemCardapioResponse ativar(@PathVariable Long id) {
        return ItemCardapioResponse.de(itemCardapioService.ativar(id));
    }

    @PostMapping("/{id}/inativar")
    public ItemCardapioResponse inativar(@PathVariable Long id) {
        return ItemCardapioResponse.de(itemCardapioService.inativar(id));
    }
}
