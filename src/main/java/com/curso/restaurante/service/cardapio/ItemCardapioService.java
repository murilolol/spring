package com.curso.restaurante.service.cardapio;

import com.curso.restaurante.domain.Status;
import com.curso.restaurante.domain.cardapio.CategoriaCardapio;
import com.curso.restaurante.domain.cardapio.ItemCardapio;
import com.curso.restaurante.domain.cardapio.SecaoPreparo;
import com.curso.restaurante.domain.comum.ConflitoDeEstadoException;
import com.curso.restaurante.domain.comum.RecursoNaoEncontradoException;
import com.curso.restaurante.repository.cardapio.CategoriaCardapioRepository;
import com.curso.restaurante.repository.cardapio.ItemCardapioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class ItemCardapioService {

    private final ItemCardapioRepository itemCardapioRepository;
    private final CategoriaCardapioRepository categoriaCardapioRepository;

    public ItemCardapioService(
            ItemCardapioRepository itemCardapioRepository,
            CategoriaCardapioRepository categoriaCardapioRepository) {
        this.itemCardapioRepository = itemCardapioRepository;
        this.categoriaCardapioRepository = categoriaCardapioRepository;
    }

    public ItemCardapio criar(
            Long categoriaId,
            String codigo,
            String nome,
            String descricao,
            BigDecimal precoVenda,
            int tempoPreparoMinutos,
            SecaoPreparo secaoPreparo,
            boolean exigePreparo,
            boolean controlaEstoque,
            BigDecimal saldoEstoque,
            LocalDate dataCadastro) {
        if (itemCardapioRepository.existsByCodigo(codigo)) {
            throw new ConflitoDeEstadoException("Já existe um item de cardápio com o código " + codigo);
        }

        CategoriaCardapio categoria = categoriaCardapioRepository.findById(categoriaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria de cardápio não encontrada"));

        ItemCardapio item = new ItemCardapio(
                codigo, nome, descricao, precoVenda, tempoPreparoMinutos, secaoPreparo, exigePreparo,
                controlaEstoque, saldoEstoque, dataCadastro);
        categoria.adicionarItem(item);

        return itemCardapioRepository.save(item);
    }

    public ItemCardapio buscarPorId(Long id) {
        return itemCardapioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Item de cardápio não encontrado"));
    }

    public Page<ItemCardapio> listar(
            Long categoriaId, SecaoPreparo secao, Status status, String nome, Pageable pageable) {
        Specification<ItemCardapio> especificacao = Specification.allOf();

        if (categoriaId != null) {
            especificacao = especificacao.and(
                    (raiz, consulta, cb) -> cb.equal(raiz.get("categoria").get("id"), categoriaId));
        }
        if (secao != null) {
            especificacao = especificacao.and((raiz, consulta, cb) -> cb.equal(raiz.get("secaoPreparo"), secao));
        }
        if (status != null) {
            especificacao = especificacao.and((raiz, consulta, cb) -> cb.equal(raiz.get("status"), status));
        }
        if (nome != null && !nome.isBlank()) {
            especificacao = especificacao.and(
                    (raiz, consulta, cb) -> cb.like(cb.lower(raiz.get("nome")), "%" + nome.toLowerCase() + "%"));
        }

        return itemCardapioRepository.findAll(especificacao, pageable);
    }

    public ItemCardapio atualizar(
            Long id,
            String nome,
            String descricao,
            BigDecimal precoVenda,
            int tempoPreparoMinutos,
            SecaoPreparo secaoPreparo,
            boolean exigePreparo,
            boolean controlaEstoque) {
        ItemCardapio item = buscarPorId(id);
        item.atualizarDados(nome, descricao, precoVenda, tempoPreparoMinutos, secaoPreparo, exigePreparo, controlaEstoque);
        return item;
    }

    public ItemCardapio registrarEntradaEstoque(Long id, BigDecimal quantidade) {
        ItemCardapio item = buscarPorId(id);
        item.receberEstoque(quantidade);
        return item;
    }

    public ItemCardapio registrarSaidaEstoque(Long id, BigDecimal quantidade) {
        ItemCardapio item = buscarPorId(id);
        item.baixarEstoque(quantidade);
        return item;
    }

    public ItemCardapio ativar(Long id) {
        ItemCardapio item = buscarPorId(id);
        item.ativar();
        return item;
    }

    public ItemCardapio inativar(Long id) {
        ItemCardapio item = buscarPorId(id);
        item.inativar();
        return item;
    }
}
