package com.curso.restaurante.service.cliente;

import com.curso.restaurante.domain.Status;
import com.curso.restaurante.domain.cliente.Cliente;
import com.curso.restaurante.domain.comum.ConflitoDeEstadoException;
import com.curso.restaurante.domain.comum.RecursoNaoEncontradoException;
import com.curso.restaurante.repository.cliente.ClienteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Transactional
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public Cliente criar(
            String nome,
            String documento,
            String telefone,
            String email,
            String endereco,
            LocalDate dataNascimento) {
        if (documento != null && clienteRepository.existsByDocumento(documento)) {
            throw new ConflitoDeEstadoException("Documento já cadastrado para outro cliente");
        }

        Cliente cliente = new Cliente(nome, documento, telefone, email, endereco, dataNascimento);
        return clienteRepository.save(cliente);
    }

    public Cliente buscarPorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado"));
    }

    public Page<Cliente> listar(String nome, String documento, Status status, Pageable pageable) {
        Specification<Cliente> especificacao = Specification.allOf();

        if (nome != null && !nome.isBlank()) {
            especificacao = especificacao.and(
                    (raiz, consulta, cb) -> cb.like(cb.lower(raiz.get("nome")), "%" + nome.toLowerCase() + "%"));
        }
        if (documento != null && !documento.isBlank()) {
            especificacao = especificacao.and((raiz, consulta, cb) -> cb.equal(raiz.get("documento"), documento));
        }
        if (status != null) {
            especificacao = especificacao.and((raiz, consulta, cb) -> cb.equal(raiz.get("status"), status));
        }

        return clienteRepository.findAll(especificacao, pageable);
    }

    public Cliente atualizar(Long id, String nome, String telefone, String email, String endereco) {
        Cliente cliente = buscarPorId(id);
        cliente.atualizarDados(nome, telefone, email, endereco);
        return cliente;
    }

    public Cliente ativar(Long id) {
        Cliente cliente = buscarPorId(id);
        cliente.ativar();
        return cliente;
    }

    public Cliente inativar(Long id) {
        Cliente cliente = buscarPorId(id);
        cliente.inativar();
        return cliente;
    }
}
