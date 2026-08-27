package com.curso.restaurante.service.comanda;

import com.curso.restaurante.domain.cliente.Cliente;
import com.curso.restaurante.domain.comanda.Comanda;
import com.curso.restaurante.domain.comanda.StatusComanda;
import com.curso.restaurante.domain.comanda.TipoAtendimento;
import com.curso.restaurante.domain.comum.RecursoNaoEncontradoException;
import com.curso.restaurante.domain.mesa.Mesa;
import com.curso.restaurante.domain.usuario.Usuario;
import com.curso.restaurante.repository.cliente.ClienteRepository;
import com.curso.restaurante.repository.comanda.ComandaRepository;
import com.curso.restaurante.repository.mesa.MesaRepository;
import com.curso.restaurante.repository.usuario.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Transactional
public class ComandaService {

    private final ComandaRepository comandaRepository;
    private final MesaRepository mesaRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;

    public ComandaService(
            ComandaRepository comandaRepository,
            MesaRepository mesaRepository,
            ClienteRepository clienteRepository,
            UsuarioRepository usuarioRepository) {
        this.comandaRepository = comandaRepository;
        this.mesaRepository = mesaRepository;
        this.clienteRepository = clienteRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public Comanda abrir(
            TipoAtendimento tipoAtendimento,
            Long mesaId,
            Long clienteId,
            String usernameResponsavel,
            int numeroPessoas,
            BigDecimal percentualTaxaServico,
            String observacao) {
        Mesa mesa = mesaId == null ? null : buscarMesa(mesaId);
        Cliente cliente = clienteId == null ? null : buscarCliente(clienteId);
        Usuario responsavel = usuarioRepository.findByUsername(usernameResponsavel)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));

        Comanda comanda = new Comanda(
                gerarCodigo(), tipoAtendimento, mesa, cliente, responsavel, numeroPessoas,
                percentualTaxaServico, observacao);

        if (mesa != null) {
            mesa.ocupar();
        }

        return comandaRepository.save(comanda);
    }

    public Comanda buscarPorId(Long id) {
        return comandaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Comanda não encontrada"));
    }

    public Page<Comanda> listar(
            StatusComanda status, Long mesaId, Long clienteId, TipoAtendimento tipo, Pageable pageable) {
        Specification<Comanda> especificacao = Specification.allOf();

        if (status != null) {
            especificacao = especificacao.and((raiz, consulta, cb) -> cb.equal(raiz.get("status"), status));
        }
        if (mesaId != null) {
            especificacao = especificacao.and((raiz, consulta, cb) -> cb.equal(raiz.get("mesa").get("id"), mesaId));
        }
        if (clienteId != null) {
            especificacao = especificacao.and(
                    (raiz, consulta, cb) -> cb.equal(raiz.get("cliente").get("id"), clienteId));
        }
        if (tipo != null) {
            especificacao = especificacao.and(
                    (raiz, consulta, cb) -> cb.equal(raiz.get("tipoAtendimento"), tipo));
        }

        return comandaRepository.findAll(especificacao, pageable);
    }

    public Page<Comanda> listarPorCliente(Long clienteId, Pageable pageable) {
        buscarCliente(clienteId);
        return comandaRepository.findByClienteId(clienteId, pageable);
    }

    public Comanda fechar(Long id) {
        Comanda comanda = buscarPorId(id);
        comanda.fechar();
        if (comanda.getMesa() != null) {
            comanda.getMesa().liberar();
        }
        return comanda;
    }

    public Comanda reabrir(Long id) {
        Comanda comanda = buscarPorId(id);
        comanda.reabrir();
        if (comanda.getMesa() != null) {
            comanda.getMesa().ocupar();
        }
        return comanda;
    }

    public Comanda cancelar(Long id, String motivo) {
        Comanda comanda = buscarPorId(id);
        comanda.cancelar(motivo);
        if (comanda.getMesa() != null) {
            comanda.getMesa().liberar();
        }
        return comanda;
    }

    public Comanda transferirMesa(Long id, Long mesaDestinoId) {
        Comanda comanda = buscarPorId(id);
        Mesa mesaDestino = buscarMesa(mesaDestinoId);
        Mesa mesaOrigem = comanda.getMesa();

        mesaDestino.ocupar();
        if (mesaOrigem != null) {
            mesaOrigem.liberar();
        }
        comanda.transferirParaMesa(mesaDestino);

        return comanda;
    }

    private Mesa buscarMesa(Long id) {
        return mesaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Mesa não encontrada"));
    }

    private Cliente buscarCliente(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado"));
    }

    private String gerarCodigo() {
        LocalDate hoje = LocalDate.now();
        LocalDateTime inicioDoDia = hoje.atStartOfDay();
        LocalDateTime fimDoDia = hoje.plusDays(1).atStartOfDay();

        long quantidadeHoje = comandaRepository.countByAbertaEmBetween(inicioDoDia, fimDoDia);

        return "CMD-" + hoje.format(DateTimeFormatter.BASIC_ISO_DATE) + "-" + String.format("%04d", quantidadeHoje + 1);
    }
}
