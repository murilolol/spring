package com.curso.restaurante.api.caixa;

import com.curso.restaurante.api.caixa.dto.AbrirSessaoCaixaRequest;
import com.curso.restaurante.api.caixa.dto.FecharSessaoCaixaRequest;
import com.curso.restaurante.api.caixa.dto.RegistrarSangriaRequest;
import com.curso.restaurante.api.caixa.dto.PagamentoResponse;
import com.curso.restaurante.api.caixa.dto.SangriaResponse;
import com.curso.restaurante.api.caixa.dto.SessaoCaixaResponse;
import com.curso.restaurante.api.comum.PaginaResponse;
import com.curso.restaurante.domain.caixa.FormaPagamento;
import com.curso.restaurante.domain.caixa.Sangria;
import com.curso.restaurante.domain.caixa.SessaoCaixa;
import com.curso.restaurante.domain.caixa.StatusSessaoCaixa;
import com.curso.restaurante.service.caixa.PagamentoService;
import com.curso.restaurante.service.caixa.SessaoCaixaService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/caixa/sessoes")
public class SessaoCaixaController {

    private final SessaoCaixaService sessaoCaixaService;
    private final PagamentoService pagamentoService;

    public SessaoCaixaController(SessaoCaixaService sessaoCaixaService, PagamentoService pagamentoService) {
        this.sessaoCaixaService = sessaoCaixaService;
        this.pagamentoService = pagamentoService;
    }

    @PostMapping
    public ResponseEntity<SessaoCaixaResponse> abrir(
            @Valid @RequestBody AbrirSessaoCaixaRequest request, Authentication authentication) {
        SessaoCaixa sessao = sessaoCaixaService.abrir(authentication.getName(), request.valorAbertura());
        return ResponseEntity
                .created(URI.create("/api/caixa/sessoes/" + sessao.getId()))
                .body(SessaoCaixaResponse.de(sessao));
    }

    @GetMapping
    public PaginaResponse<SessaoCaixaResponse> listar(
            @RequestParam(required = false) StatusSessaoCaixa status,
            @RequestParam(required = false) LocalDateTime de,
            @RequestParam(required = false) LocalDateTime ate,
            Pageable pageable) {
        return PaginaResponse.de(sessaoCaixaService.listar(status, de, ate, pageable).map(SessaoCaixaResponse::de));
    }

    @GetMapping("/aberta")
    public SessaoCaixaResponse buscarAberta() {
        return SessaoCaixaResponse.de(sessaoCaixaService.buscarSessaoAberta());
    }

    @GetMapping("/{id}")
    public SessaoCaixaResponse buscarPorId(@PathVariable Long id) {
        return SessaoCaixaResponse.de(sessaoCaixaService.buscarPorId(id));
    }

    @PostMapping("/{id}/fechar")
    public SessaoCaixaResponse fechar(
            @PathVariable Long id, @Valid @RequestBody FecharSessaoCaixaRequest request, Authentication authentication) {
        SessaoCaixa sessao = sessaoCaixaService.fechar(
                id, request.valorContado(), request.observacao(), authentication.getName());
        return SessaoCaixaResponse.de(sessao);
    }

    @PostMapping("/{id}/sangrias")
    public ResponseEntity<SangriaResponse> registrarSangria(
            @PathVariable Long id, @Valid @RequestBody RegistrarSangriaRequest request, Authentication authentication) {
        Sangria sangria = sessaoCaixaService.registrarSangria(
                id, request.valor(), request.motivo(), authentication.getName());
        return ResponseEntity.status(201).body(SangriaResponse.de(sangria));
    }

    @GetMapping("/{id}/sangrias")
    public PaginaResponse<SangriaResponse> listarSangrias(@PathVariable Long id, Pageable pageable) {
        return PaginaResponse.de(sessaoCaixaService.listarSangrias(id, pageable).map(SangriaResponse::de));
    }

    @GetMapping("/{id}/pagamentos")
    public PaginaResponse<PagamentoResponse> listarPagamentos(
            @PathVariable Long id, @RequestParam(required = false) FormaPagamento forma, Pageable pageable) {
        return PaginaResponse.de(
                pagamentoService.listarPagamentosPorSessao(id, forma, pageable).map(PagamentoResponse::de));
    }
}
