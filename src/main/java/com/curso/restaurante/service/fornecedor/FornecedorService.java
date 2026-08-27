package com.curso.restaurante.service.fornecedor;

import com.curso.restaurante.domain.Status;
import com.curso.restaurante.domain.comum.ConflitoDeEstadoException;
import com.curso.restaurante.domain.comum.RecursoNaoEncontradoException;
import com.curso.restaurante.domain.fornecedor.Fornecedor;
import com.curso.restaurante.repository.fornecedor.FornecedorRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class FornecedorService {

    private final FornecedorRepository fornecedorRepository;

    public FornecedorService(FornecedorRepository fornecedorRepository) {
        this.fornecedorRepository = fornecedorRepository;
    }

    public Fornecedor cadastrar(String razaoSocial, String cnpj) {
        if (fornecedorRepository.existsByCnpj(cnpj)) {
            throw new ConflitoDeEstadoException("CNPJ já cadastrado para outro fornecedor");
        }

        Fornecedor fornecedor = new Fornecedor(razaoSocial, cnpj);
        return fornecedorRepository.save(fornecedor);
    }

    public Fornecedor buscarPorId(Long id) {
        return fornecedorRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Fornecedor não encontrado"));
    }

    public Page<Fornecedor> listar(Status status, Pageable pageable) {
        Specification<Fornecedor> especificacao = Specification.allOf();

        if (status != null) {
            especificacao = especificacao.and((raiz, consulta, cb) -> cb.equal(raiz.get("status"), status));
        }

        return fornecedorRepository.findAll(especificacao, pageable);
    }

    public Fornecedor ativar(Long id) {
        Fornecedor fornecedor = buscarPorId(id);
        fornecedor.ativar();
        return fornecedor;
    }

    public Fornecedor inativar(Long id) {
        Fornecedor fornecedor = buscarPorId(id);
        fornecedor.inativar();
        return fornecedor;
    }
}
