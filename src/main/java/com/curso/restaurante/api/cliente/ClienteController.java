package com.curso.restaurante.api.cliente;

import com.curso.restaurante.api.cliente.dto.AtualizarClienteRequest;
import com.curso.restaurante.api.cliente.dto.ClienteResponse;
import com.curso.restaurante.api.cliente.dto.CriarClienteRequest;
import com.curso.restaurante.api.comanda.dto.ComandaResponse;
import com.curso.restaurante.api.comum.PaginaResponse;
import com.curso.restaurante.domain.Status;
import com.curso.restaurante.domain.cliente.Cliente;
import com.curso.restaurante.service.cliente.ClienteService;
import com.curso.restaurante.service.comanda.ComandaService;
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
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService clienteService;
    private final ComandaService comandaService;

    public ClienteController(ClienteService clienteService, ComandaService comandaService) {
        this.clienteService = clienteService;
        this.comandaService = comandaService;
    }

    @PostMapping
    public ResponseEntity<ClienteResponse> criar(@Valid @RequestBody CriarClienteRequest request) {
        Cliente cliente = clienteService.criar(
                request.nome(),
                request.documento(),
                request.telefone(),
                request.email(),
                request.endereco(),
                request.dataNascimento());
        return ResponseEntity
                .created(URI.create("/api/clientes/" + cliente.getId()))
                .body(ClienteResponse.de(cliente));
    }

    @GetMapping
    public PaginaResponse<ClienteResponse> listar(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String documento,
            @RequestParam(required = false) Status status,
            Pageable pageable) {
        return PaginaResponse.de(
                clienteService.listar(nome, documento, status, pageable).map(ClienteResponse::de));
    }

    @GetMapping("/{id}")
    public ClienteResponse buscarPorId(@PathVariable Long id) {
        return ClienteResponse.de(clienteService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ClienteResponse atualizar(@PathVariable Long id, @Valid @RequestBody AtualizarClienteRequest request) {
        return ClienteResponse.de(clienteService.atualizar(
                id, request.nome(), request.telefone(), request.email(), request.endereco()));
    }

    @GetMapping("/{id}/comandas")
    public PaginaResponse<ComandaResponse> listarComandas(@PathVariable Long id, Pageable pageable) {
        return PaginaResponse.de(comandaService.listarPorCliente(id, pageable).map(ComandaResponse::de));
    }

    @PostMapping("/{id}/ativar")
    public ClienteResponse ativar(@PathVariable Long id) {
        return ClienteResponse.de(clienteService.ativar(id));
    }

    @PostMapping("/{id}/inativar")
    public ClienteResponse inativar(@PathVariable Long id) {
        return ClienteResponse.de(clienteService.inativar(id));
    }
}
