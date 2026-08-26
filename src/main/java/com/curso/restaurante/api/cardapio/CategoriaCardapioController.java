package com.curso.restaurante.api.cardapio;

import com.curso.restaurante.api.cardapio.dto.AtualizarCategoriaCardapioRequest;
import com.curso.restaurante.api.cardapio.dto.CategoriaCardapioResponse;
import com.curso.restaurante.api.cardapio.dto.CriarCategoriaCardapioRequest;
import com.curso.restaurante.api.comum.PaginaResponse;
import com.curso.restaurante.domain.Status;
import com.curso.restaurante.domain.cardapio.CategoriaCardapio;
import com.curso.restaurante.service.cardapio.CategoriaCardapioService;
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
@RequestMapping("/api/categorias-cardapio")
public class CategoriaCardapioController {

    private final CategoriaCardapioService categoriaCardapioService;

    public CategoriaCardapioController(CategoriaCardapioService categoriaCardapioService) {
        this.categoriaCardapioService = categoriaCardapioService;
    }

    @PostMapping
    public ResponseEntity<CategoriaCardapioResponse> criar(@Valid @RequestBody CriarCategoriaCardapioRequest request) {
        CategoriaCardapio categoria = categoriaCardapioService.criar(
                request.nome(), request.descricao(), request.ordemExibicao());
        return ResponseEntity
                .created(URI.create("/api/categorias-cardapio/" + categoria.getId()))
                .body(CategoriaCardapioResponse.de(categoria));
    }

    @GetMapping
    public PaginaResponse<CategoriaCardapioResponse> listar(
            @RequestParam(required = false) Status status, Pageable pageable) {
        return PaginaResponse.de(
                categoriaCardapioService.listar(status, pageable).map(CategoriaCardapioResponse::de));
    }

    @GetMapping("/{id}")
    public CategoriaCardapioResponse buscarPorId(@PathVariable Long id) {
        return CategoriaCardapioResponse.de(categoriaCardapioService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public CategoriaCardapioResponse atualizar(
            @PathVariable Long id, @Valid @RequestBody AtualizarCategoriaCardapioRequest request) {
        return CategoriaCardapioResponse.de(categoriaCardapioService.atualizar(
                id, request.nome(), request.descricao(), request.ordemExibicao()));
    }

    @PostMapping("/{id}/ativar")
    public CategoriaCardapioResponse ativar(@PathVariable Long id) {
        return CategoriaCardapioResponse.de(categoriaCardapioService.ativar(id));
    }

    @PostMapping("/{id}/inativar")
    public CategoriaCardapioResponse inativar(@PathVariable Long id) {
        return CategoriaCardapioResponse.de(categoriaCardapioService.inativar(id));
    }
}
