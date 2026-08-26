package com.curso.restaurante.api.comanda;

import com.curso.restaurante.api.comanda.dto.AbrirComandaRequest;
import com.curso.restaurante.api.comanda.dto.ComandaResponse;
import com.curso.restaurante.api.comanda.dto.ContaResponse;
import com.curso.restaurante.api.comanda.dto.TransferirMesaRequest;
import com.curso.restaurante.api.comum.CancelarRequest;
import com.curso.restaurante.api.comum.PaginaResponse;
import com.curso.restaurante.domain.caixa.Pagamento;
import com.curso.restaurante.domain.comanda.Comanda;
import com.curso.restaurante.domain.comanda.StatusComanda;
import com.curso.restaurante.domain.comanda.TipoAtendimento;
import com.curso.restaurante.service.caixa.PagamentoService;
import com.curso.restaurante.service.comanda.ComandaService;
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

import java.math.BigDecimal;
import java.net.URI;

@RestController
@RequestMapping("/api/comandas")
public class ComandaController {

    private final ComandaService comandaService;
    private final PagamentoService pagamentoService;

    public ComandaController(ComandaService comandaService, PagamentoService pagamentoService) {
        this.comandaService = comandaService;
        this.pagamentoService = pagamentoService;
    }

    @PostMapping
    public ResponseEntity<ComandaResponse> abrir(
            @Valid @RequestBody AbrirComandaRequest request, Authentication authentication) {
        Comanda comanda = comandaService.abrir(
                request.tipoAtendimento(),
                request.mesaId(),
                request.clienteId(),
                authentication.getName(),
                request.numeroPessoas(),
                request.percentualTaxaServico() == null ? BigDecimal.ZERO : request.percentualTaxaServico(),
                request.observacao());
        return ResponseEntity
                .created(URI.create("/api/comandas/" + comanda.getId()))
                .body(ComandaResponse.de(comanda));
    }

    @GetMapping
    public PaginaResponse<ComandaResponse> listar(
            @RequestParam(required = false) StatusComanda status,
            @RequestParam(required = false) Long mesaId,
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) TipoAtendimento tipo,
            Pageable pageable) {
        return PaginaResponse.de(
                comandaService.listar(status, mesaId, clienteId, tipo, pageable).map(ComandaResponse::de));
    }

    @GetMapping("/{id}")
    public ComandaResponse buscarPorId(@PathVariable Long id) {
        return ComandaResponse.de(comandaService.buscarPorId(id));
    }

    @GetMapping("/{id}/conta")
    public ContaResponse conta(@PathVariable Long id) {
        Comanda comanda = comandaService.buscarPorId(id);
        java.math.BigDecimal totalPago = pagamentoService.listarPagamentosDaComanda(id).stream()
                .map(Pagamento::getValor)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        return ContaResponse.de(comanda, totalPago);
    }

    @PostMapping("/{id}/fechar")
    public ComandaResponse fechar(@PathVariable Long id) {
        return ComandaResponse.de(comandaService.fechar(id));
    }

    @PostMapping("/{id}/reabrir")
    public ComandaResponse reabrir(@PathVariable Long id) {
        return ComandaResponse.de(comandaService.reabrir(id));
    }

    @PostMapping("/{id}/cancelar")
    public ComandaResponse cancelar(@PathVariable Long id, @Valid @RequestBody CancelarRequest request) {
        return ComandaResponse.de(comandaService.cancelar(id, request.motivo()));
    }

    @PostMapping("/{id}/transferir-mesa")
    public ComandaResponse transferirMesa(@PathVariable Long id, @Valid @RequestBody TransferirMesaRequest request) {
        return ComandaResponse.de(comandaService.transferirMesa(id, request.mesaDestinoId()));
    }
}
