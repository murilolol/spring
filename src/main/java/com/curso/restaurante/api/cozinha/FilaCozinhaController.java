package com.curso.restaurante.api.cozinha;

import com.curso.restaurante.api.comum.PaginaResponse;
import com.curso.restaurante.api.cozinha.dto.AlterarPrioridadeRequest;
import com.curso.restaurante.api.cozinha.dto.PreparoItemResponse;
import com.curso.restaurante.domain.cardapio.SecaoPreparo;
import com.curso.restaurante.domain.cozinha.PrioridadePreparo;
import com.curso.restaurante.domain.cozinha.StatusPreparo;
import com.curso.restaurante.service.cozinha.FilaCozinhaService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cozinha/fila")
public class FilaCozinhaController {

    private final FilaCozinhaService filaCozinhaService;

    public FilaCozinhaController(FilaCozinhaService filaCozinhaService) {
        this.filaCozinhaService = filaCozinhaService;
    }

    @GetMapping
    public PaginaResponse<PreparoItemResponse> listar(
            @RequestParam(required = false) SecaoPreparo secao,
            @RequestParam(required = false) StatusPreparo status,
            @RequestParam(required = false) PrioridadePreparo prioridade,
            Pageable pageable) {
        return PaginaResponse.de(
                filaCozinhaService.listarFila(secao, status, prioridade, pageable).map(PreparoItemResponse::de));
    }

    @GetMapping("/{id}")
    public PreparoItemResponse buscarPorId(@PathVariable Long id) {
        return PreparoItemResponse.de(filaCozinhaService.buscarPorId(id));
    }

    @PostMapping("/{id}/iniciar")
    public PreparoItemResponse iniciar(@PathVariable Long id, Authentication authentication) {
        return PreparoItemResponse.de(filaCozinhaService.iniciar(id, authentication.getName()));
    }

    @PostMapping("/{id}/concluir")
    public PreparoItemResponse concluir(@PathVariable Long id) {
        return PreparoItemResponse.de(filaCozinhaService.concluir(id));
    }

    @PostMapping("/{id}/cancelar")
    public PreparoItemResponse cancelar(@PathVariable Long id) {
        return PreparoItemResponse.de(filaCozinhaService.cancelar(id));
    }

    @PostMapping("/{id}/alterar-prioridade")
    public PreparoItemResponse alterarPrioridade(
            @PathVariable Long id, @Valid @RequestBody AlterarPrioridadeRequest request) {
        return PreparoItemResponse.de(filaCozinhaService.alterarPrioridade(id, request.prioridade()));
    }
}
