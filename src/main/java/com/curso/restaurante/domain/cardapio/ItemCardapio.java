package com.curso.restaurante.domain.cardapio;

import com.curso.restaurante.domain.Status;
import com.curso.restaurante.domain.fornecedor.Fornecedor;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

import static com.curso.restaurante.domain.comum.Validacoes.exigirNaoNegativo;
import static com.curso.restaurante.domain.comum.Validacoes.exigirNaoNulo;
import static com.curso.restaurante.domain.comum.Validacoes.exigirPositivo;
import static com.curso.restaurante.domain.comum.Validacoes.exigirTexto;

@Entity
@Table(
        name = "item_cardapio",
        uniqueConstraints = @UniqueConstraint(name = "uk_item_cardapio_codigo", columnNames = "codigo"))
public class ItemCardapio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String codigo;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(length = 300)
    private String descricao;

    @Column(name = "preco_venda", nullable = false, precision = 18, scale = 2)
    private BigDecimal precoVenda;

    @Column(name = "tempo_preparo_minutos", nullable = false)
    private int tempoPreparoMinutos;

    @Enumerated(EnumType.STRING)
    @Column(name = "secao_preparo", nullable = false, length = 20)
    private SecaoPreparo secaoPreparo;

    @Column(name = "exige_preparo", nullable = false)
    private boolean exigePreparo;

    @Column(name = "controla_estoque", nullable = false)
    private boolean controlaEstoque;

    @Column(name = "saldo_estoque", nullable = false, precision = 18, scale = 3)
    private BigDecimal saldoEstoque;

    @Column(name = "data_cadastro", nullable = false)
    private LocalDate dataCadastro;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(
            name = "categoria_cardapio_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_item_cardapio_categoria_cardapio"))
    private CategoriaCardapio categoria;

    @Column(name = "estoque_minimo", nullable = false, precision = 18, scale = 3)
    private BigDecimal estoqueMinimo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "fornecedor_id", foreignKey = @ForeignKey(name = "fk_item_cardapio_fornecedor"))
    private Fornecedor fornecedor;

    protected ItemCardapio() {
    }

    public ItemCardapio(
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
        this(
                codigo, nome, descricao, precoVenda, tempoPreparoMinutos, secaoPreparo, exigePreparo, controlaEstoque,
                saldoEstoque, dataCadastro, BigDecimal.ZERO);
    }

    public ItemCardapio(
            String codigo,
            String nome,
            String descricao,
            BigDecimal precoVenda,
            int tempoPreparoMinutos,
            SecaoPreparo secaoPreparo,
            boolean exigePreparo,
            boolean controlaEstoque,
            BigDecimal saldoEstoque,
            LocalDate dataCadastro,
            BigDecimal estoqueMinimo) {
        this.codigo = exigirTexto(codigo, "Código é obrigatório");
        this.nome = exigirTexto(nome, "Nome é obrigatório");
        this.descricao = descricao == null ? null : descricao.trim();
        this.precoVenda = exigirNaoNegativo(precoVenda, "Preço de venda não pode ser negativo");
        this.tempoPreparoMinutos = exigirNaoNegativo(tempoPreparoMinutos, "Tempo de preparo não pode ser negativo");
        this.secaoPreparo = exigirNaoNulo(secaoPreparo, "Seção de preparo é obrigatória");
        this.exigePreparo = exigePreparo;
        this.controlaEstoque = controlaEstoque;
        this.saldoEstoque = exigirNaoNegativo(saldoEstoque, "Saldo de estoque não pode ser negativo");
        this.dataCadastro = exigirNaoNulo(dataCadastro, "Data de cadastro é obrigatória");
        this.estoqueMinimo = exigirNaoNegativo(estoqueMinimo, "Estoque mínimo não pode ser negativo");
        this.status = Status.ATIVO;
    }

    public BigDecimal calcularValorEmEstoque() {
        return saldoEstoque
                .multiply(precoVenda)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public void receberEstoque(BigDecimal quantidade) {
        exigirPositivo(quantidade, "Quantidade recebida deve ser maior que zero");
        this.saldoEstoque = saldoEstoque.add(quantidade);
    }

    public void baixarEstoque(BigDecimal quantidade) {
        exigirPositivo(quantidade, "Quantidade retirada deve ser maior que zero");

        if (saldoEstoque.compareTo(quantidade) < 0) {
            throw new IllegalArgumentException("Saldo de estoque insuficiente");
        }

        this.saldoEstoque = saldoEstoque.subtract(quantidade);
    }

    public void atualizarDados(
            String nome,
            String descricao,
            BigDecimal precoVenda,
            int tempoPreparoMinutos,
            SecaoPreparo secaoPreparo,
            boolean exigePreparo,
            boolean controlaEstoque) {
        this.nome = exigirTexto(nome, "Nome é obrigatório");
        this.descricao = descricao == null ? null : descricao.trim();
        this.precoVenda = exigirNaoNegativo(precoVenda, "Preço de venda não pode ser negativo");
        this.tempoPreparoMinutos = exigirNaoNegativo(tempoPreparoMinutos, "Tempo de preparo não pode ser negativo");
        this.secaoPreparo = exigirNaoNulo(secaoPreparo, "Seção de preparo é obrigatória");
        this.exigePreparo = exigePreparo;
        this.controlaEstoque = controlaEstoque;
    }

    public void alterarPreco(BigDecimal novoPreco) {
        this.precoVenda = exigirNaoNegativo(novoPreco, "Preço de venda não pode ser negativo");
    }

    public void ativar() {
        this.status = Status.ATIVO;
    }

    public void inativar() {
        this.status = Status.INATIVO;
    }

    public boolean estaDisponivelPara(BigDecimal quantidade) {
        if (!controlaEstoque) {
            return true;
        }
        return saldoEstoque.compareTo(quantidade) >= 0;
    }

    public boolean exigeEntradaNaFilaDeCozinha() {
        return exigePreparo;
    }

    public void alterarEstoqueMinimo(BigDecimal novoEstoqueMinimo) {
        this.estoqueMinimo = exigirNaoNegativo(novoEstoqueMinimo, "Estoque mínimo não pode ser negativo");
    }

    public void associarFornecedor(Fornecedor fornecedor) {
        this.fornecedor = exigirNaoNulo(fornecedor, "Fornecedor é obrigatório");
    }

    public void removerFornecedor() {
        this.fornecedor = null;
    }

    void associarA(CategoriaCardapio categoria) {
        Objects.requireNonNull(categoria, "Categoria do cardápio é obrigatória");

        if (this.categoria != null && this.categoria != categoria) {
            throw new IllegalStateException("Item já pertence a outra categoria");
        }

        this.categoria = categoria;
    }

    public Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public BigDecimal getPrecoVenda() {
        return precoVenda;
    }

    public int getTempoPreparoMinutos() {
        return tempoPreparoMinutos;
    }

    public SecaoPreparo getSecaoPreparo() {
        return secaoPreparo;
    }

    public boolean isExigePreparo() {
        return exigePreparo;
    }

    public boolean isControlaEstoque() {
        return controlaEstoque;
    }

    public BigDecimal getSaldoEstoque() {
        return saldoEstoque;
    }

    public LocalDate getDataCadastro() {
        return dataCadastro;
    }

    public Status getStatus() {
        return status;
    }

    public CategoriaCardapio getCategoria() {
        return categoria;
    }

    public BigDecimal getEstoqueMinimo() {
        return estoqueMinimo;
    }

    public Fornecedor getFornecedor() {
        return fornecedor;
    }
}
