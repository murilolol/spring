package com.curso.restaurante.service.mesa;

import com.curso.restaurante.domain.comum.ConflitoDeEstadoException;
import com.curso.restaurante.domain.comum.RecursoNaoEncontradoException;
import com.curso.restaurante.domain.mesa.Mesa;
import com.curso.restaurante.domain.mesa.StatusMesa;
import com.curso.restaurante.repository.mesa.MesaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MesaService {

    private final MesaRepository mesaRepository;

    public MesaService(MesaRepository mesaRepository) {
        this.mesaRepository = mesaRepository;
    }

    public Mesa criar(int numero, int capacidade, String setor) {
        if (mesaRepository.existsByNumero(numero)) {
            throw new ConflitoDeEstadoException("Já existe uma mesa com o número " + numero);
        }

        return mesaRepository.save(new Mesa(numero, capacidade, setor));
    }

    public Mesa buscarPorId(Long id) {
        return mesaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Mesa não encontrada"));
    }

    public Page<Mesa> listar(StatusMesa status, String setor, Pageable pageable) {
        Specification<Mesa> especificacao = Specification.allOf();

        if (status != null) {
            especificacao = especificacao.and((raiz, consulta, cb) -> cb.equal(raiz.get("status"), status));
        }
        if (setor != null && !setor.isBlank()) {
            especificacao = especificacao.and((raiz, consulta, cb) -> cb.equal(raiz.get("setor"), setor));
        }

        return mesaRepository.findAll(especificacao, pageable);
    }

    public Mesa atualizar(Long id, int capacidade, String setor) {
        Mesa mesa = buscarPorId(id);
        mesa.alterarCapacidadeESetor(capacidade, setor);
        return mesa;
    }

    public Mesa reservar(Long id) {
        Mesa mesa = buscarPorId(id);
        mesa.reservar();
        return mesa;
    }

    public Mesa cancelarReserva(Long id) {
        Mesa mesa = buscarPorId(id);
        mesa.cancelarReserva();
        return mesa;
    }

    public Mesa interditar(Long id) {
        Mesa mesa = buscarPorId(id);
        mesa.interditar();
        return mesa;
    }

    public Mesa liberarInterdicao(Long id) {
        Mesa mesa = buscarPorId(id);
        mesa.liberarInterdicao();
        return mesa;
    }
}
