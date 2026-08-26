package com.curso.restaurante.api.caixa;

import com.curso.restaurante.api.caixa.dto.PagamentoResponse;
import com.curso.restaurante.api.caixa.dto.RegistrarPagamentoRequest;
import com.curso.restaurante.domain.caixa.Pagamento;
import com.curso.restaurante.service.caixa.PagamentoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
public class PagamentoController {

    private final PagamentoService pagamentoService;

    public PagamentoController(PagamentoService pagamentoService) {
        this.pagamentoService = pagamentoService;
    }

    @PostMapping("/api/comandas/{comandaId}/pagamentos")
    public ResponseEntity<PagamentoResponse> registrar(
            @PathVariable Long comandaId,
            @Valid @RequestBody RegistrarPagamentoRequest request,
            Authentication authentication) {
        Pagamento pagamento = pagamentoService.registrarPagamento(
                comandaId, request.formaPagamento(), request.valor(), request.valorRecebido(),
                authentication.getName());
        return ResponseEntity
                .created(URI.create("/api/comandas/" + comandaId + "/pagamentos/" + pagamento.getId()))
                .body(PagamentoResponse.de(pagamento));
    }

    @GetMapping("/api/comandas/{comandaId}/pagamentos")
    public List<PagamentoResponse> listar(@PathVariable Long comandaId) {
        return pagamentoService.listarPagamentosDaComanda(comandaId).stream().map(PagamentoResponse::de).toList();
    }
}
