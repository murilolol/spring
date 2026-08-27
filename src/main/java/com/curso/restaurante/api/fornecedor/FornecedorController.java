package com.curso.restaurante.api.fornecedor;

import com.curso.restaurante.api.comum.PaginaResponse;
import com.curso.restaurante.api.fornecedor.dto.CriarFornecedorRequest;
import com.curso.restaurante.api.fornecedor.dto.FornecedorResponse;
import com.curso.restaurante.domain.Status;
import com.curso.restaurante.domain.fornecedor.Fornecedor;
import com.curso.restaurante.service.fornecedor.FornecedorService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/fornecedores")
public class FornecedorController {

    private final FornecedorService fornecedorService;

    public FornecedorController(FornecedorService fornecedorService) {
        this.fornecedorService = fornecedorService;
    }

    @PostMapping
    public ResponseEntity<FornecedorResponse> criar(@Valid @RequestBody CriarFornecedorRequest request) {
        Fornecedor fornecedor = fornecedorService.cadastrar(request.razaoSocial(), request.cnpj());
        return ResponseEntity
                .created(URI.create("/api/fornecedores/" + fornecedor.getId()))
                .body(FornecedorResponse.de(fornecedor));
    }

    @GetMapping
    public PaginaResponse<FornecedorResponse> listar(
            @RequestParam(required = false) Status status, Pageable pageable) {
        return PaginaResponse.de(fornecedorService.listar(status, pageable).map(FornecedorResponse::de));
    }

    @GetMapping("/{id}")
    public FornecedorResponse buscarPorId(@PathVariable Long id) {
        return FornecedorResponse.de(fornecedorService.buscarPorId(id));
    }

    @PostMapping("/{id}/ativar")
    public FornecedorResponse ativar(@PathVariable Long id) {
        return FornecedorResponse.de(fornecedorService.ativar(id));
    }

    @PostMapping("/{id}/inativar")
    public FornecedorResponse inativar(@PathVariable Long id) {
        return FornecedorResponse.de(fornecedorService.inativar(id));
    }
}
