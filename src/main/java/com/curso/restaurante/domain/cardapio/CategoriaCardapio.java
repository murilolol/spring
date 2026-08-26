package com.curso.restaurante.domain.cardapio;

import com.curso.restaurante.domain.Status;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.curso.restaurante.domain.comum.Validacoes.exigirNaoNegativo;
import static com.curso.restaurante.domain.comum.Validacoes.exigirTexto;

@Entity
@Table(
        name = "categoria_cardapio",
        uniqueConstraints = @UniqueConstraint(name = "uk_categoria_cardapio_nome", columnNames = "nome"))
public class CategoriaCardapio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(length = 200)
    private String descricao;

    @Column(name = "ordem_exibicao", nullable = false)
    private int ordemExibicao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    @OneToMany(mappedBy = "categoria", fetch = FetchType.LAZY)
    private List<ItemCardapio> itens = new ArrayList<>();

    protected CategoriaCardapio() {
    }

    public CategoriaCardapio(String nome, String descricao, int ordemExibicao) {
        this.nome = exigirTexto(nome, "Nome da categoria é obrigatório");
        this.descricao = descricao == null ? null : descricao.trim();
        this.ordemExibicao = exigirNaoNegativo(ordemExibicao, "Ordem de exibição não pode ser negativa");
        this.status = Status.ATIVO;
    }

    public void adicionarItem(ItemCardapio item) {
        Objects.requireNonNull(item, "Item é obrigatório");

        boolean codigoJaUtilizado = itens.stream()
                .anyMatch(existente -> existente != item
                        && existente.getCodigo().equals(item.getCodigo()));

        if (codigoJaUtilizado) {
            throw new IllegalArgumentException("Código já utilizado na categoria");
        }

        item.associarA(this);

        if (!itens.contains(item)) {
            itens.add(item);
        }
    }

    public void renomear(String novoNome) {
        this.nome = exigirTexto(novoNome, "Nome da categoria é obrigatório");
    }

    public void alterarDescricao(String novaDescricao) {
        this.descricao = novaDescricao == null ? null : novaDescricao.trim();
    }

    public void alterarOrdemExibicao(int novaOrdem) {
        this.ordemExibicao = exigirNaoNegativo(novaOrdem, "Ordem de exibição não pode ser negativa");
    }

    public void ativar() {
        this.status = Status.ATIVO;
    }

    public void inativar() {
        this.status = Status.INATIVO;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public int getOrdemExibicao() {
        return ordemExibicao;
    }

    public Status getStatus() {
        return status;
    }

    public List<ItemCardapio> getItens() {
        return List.copyOf(itens);
    }
}
