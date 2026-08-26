package com.curso.restaurante.api.mesa;

import com.curso.restaurante.api.comum.PaginaResponse;
import com.curso.restaurante.api.mesa.dto.AtualizarMesaRequest;
import com.curso.restaurante.api.mesa.dto.CriarMesaRequest;
import com.curso.restaurante.api.mesa.dto.MesaResponse;
import com.curso.restaurante.domain.mesa.Mesa;
import com.curso.restaurante.domain.mesa.StatusMesa;
import com.curso.restaurante.service.mesa.MesaService;
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
@RequestMapping("/api/mesas")
public class MesaController {

    private final MesaService mesaService;

    public MesaController(MesaService mesaService) {
        this.mesaService = mesaService;
    }

    @PostMapping
    public ResponseEntity<MesaResponse> criar(@Valid @RequestBody CriarMesaRequest request) {
        Mesa mesa = mesaService.criar(request.numero(), request.capacidade(), request.setor());
        return ResponseEntity
                .created(URI.create("/api/mesas/" + mesa.getId()))
                .body(MesaResponse.de(mesa));
    }

    @GetMapping
    public PaginaResponse<MesaResponse> listar(
            @RequestParam(required = false) StatusMesa status,
            @RequestParam(required = false) String setor,
            Pageable pageable) {
        return PaginaResponse.de(mesaService.listar(status, setor, pageable).map(MesaResponse::de));
    }

    @GetMapping("/{id}")
    public MesaResponse buscarPorId(@PathVariable Long id) {
        return MesaResponse.de(mesaService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public MesaResponse atualizar(@PathVariable Long id, @Valid @RequestBody AtualizarMesaRequest request) {
        return MesaResponse.de(mesaService.atualizar(id, request.capacidade(), request.setor()));
    }

    @PostMapping("/{id}/reservar")
    public MesaResponse reservar(@PathVariable Long id) {
        return MesaResponse.de(mesaService.reservar(id));
    }

    @PostMapping("/{id}/cancelar-reserva")
    public MesaResponse cancelarReserva(@PathVariable Long id) {
        return MesaResponse.de(mesaService.cancelarReserva(id));
    }

    @PostMapping("/{id}/interditar")
    public MesaResponse interditar(@PathVariable Long id) {
        return MesaResponse.de(mesaService.interditar(id));
    }

    @PostMapping("/{id}/liberar-interdicao")
    public MesaResponse liberarInterdicao(@PathVariable Long id) {
        return MesaResponse.de(mesaService.liberarInterdicao(id));
    }
}
