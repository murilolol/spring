package com.curso.restaurante.service.cardapio;

import com.curso.restaurante.domain.Status;
import com.curso.restaurante.domain.cardapio.CategoriaCardapio;
import com.curso.restaurante.domain.comum.ConflitoDeEstadoException;
import com.curso.restaurante.domain.comum.RecursoNaoEncontradoException;
import com.curso.restaurante.repository.cardapio.CategoriaCardapioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class CategoriaCardapioService {

    private final CategoriaCardapioRepository categoriaCardapioRepository;

    public CategoriaCardapioService(CategoriaCardapioRepository categoriaCardapioRepository) {
        this.categoriaCardapioRepository = categoriaCardapioRepository;
    }

    public CategoriaCardapio criar(String nome, String descricao, int ordemExibicao) {
        if (categoriaCardapioRepository.existsByNome(nome)) {
            throw new ConflitoDeEstadoException("Já existe uma categoria com o nome " + nome);
        }

        return categoriaCardapioRepository.save(new CategoriaCardapio(nome, descricao, ordemExibicao));
    }

    public CategoriaCardapio buscarPorId(Long id) {
        return categoriaCardapioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria de cardápio não encontrada"));
    }

    public Page<CategoriaCardapio> listar(Status status, Pageable pageable) {
        Specification<CategoriaCardapio> especificacao = Specification.allOf();

        if (status != null) {
            especificacao = especificacao.and((raiz, consulta, cb) -> cb.equal(raiz.get("status"), status));
        }

        return categoriaCardapioRepository.findAll(especificacao, pageable);
    }

    public CategoriaCardapio atualizar(Long id, String nome, String descricao, int ordemExibicao) {
        CategoriaCardapio categoria = buscarPorId(id);
        categoria.renomear(nome);
        categoria.alterarDescricao(descricao);
        categoria.alterarOrdemExibicao(ordemExibicao);
        return categoria;
    }

    public CategoriaCardapio ativar(Long id) {
        CategoriaCardapio categoria = buscarPorId(id);
        categoria.ativar();
        return categoria;
    }

    public CategoriaCardapio inativar(Long id) {
        CategoriaCardapio categoria = buscarPorId(id);
        categoria.inativar();
        return categoria;
    }
}
