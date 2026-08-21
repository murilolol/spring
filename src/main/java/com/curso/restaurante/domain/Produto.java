package com.curso.restaurante.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

public class Produto {

    private final String codigo;
    private String descricao;
    private BigDecimal saldoEstoque;
    private BigDecimal valorUnitario;
    private final LocalDate dataCadastro;
    private Status status;
    private CategoriaProduto categoria;

    public Produto(
            String codigo,
            String descricao,
            BigDecimal saldoEstoque,
            BigDecimal valorUnitario,
            LocalDate dataCadastro) {
        this.codigo = validarTextoObrigatorio(
                codigo,
                "Código é obrigatório");
        this.descricao = validarTextoObrigatorio(
                descricao,
                "Descrição é obrigatória");
        this.saldoEstoque = validarNaoNegativo(
                saldoEstoque,
                "Saldo de estoque não pode ser negativo");
        this.valorUnitario = validarNaoNegativo(
                valorUnitario,
                "Valor unitário não pode ser negativo");
        this.dataCadastro = Objects.requireNonNull(
                dataCadastro,
                "Data de cadastro é obrigatória");
        this.status = Status.ATIVO;
    }

    public BigDecimal calcularValorEstoque() {
        return saldoEstoque
                .multiply(valorUnitario)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public void receberEstoque(BigDecimal quantidade) {
        validarPositivo(quantidade, "Quantidade recebida deve ser maior que zero");
        this.saldoEstoque = saldoEstoque.add(quantidade);
    }

    public void retirarEstoque(BigDecimal quantidade) {
        validarPositivo(quantidade, "Quantidade retirada deve ser maior que zero");

        if (saldoEstoque.compareTo(quantidade) < 0) {
            throw new IllegalArgumentException("Saldo de estoque insuficiente");
        }

        this.saldoEstoque = saldoEstoque.subtract(quantidade);
    }

    public void alterarDescricao(String novaDescricao) {
        this.descricao = validarTextoObrigatorio(
                novaDescricao,
                "Descrição é obrigatória");
    }

    public void alterarValorUnitario(BigDecimal novoValor) {
        this.valorUnitario = validarNaoNegativo(
                novoValor,
                "Valor unitário não pode ser negativo");
    }

    public void ativar() {
        this.status = Status.ATIVO;
    }

    public void inativar() {
        this.status = Status.INATIVO;
    }

    void associarA(CategoriaProduto categoria) {
        Objects.requireNonNull(categoria, "Categoria de produto é obrigatória");

        if (this.categoria != null && this.categoria != categoria) {
            throw new IllegalStateException("Produto já pertence a outra categoria");
        }

        this.categoria = categoria;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getDescricao() {
        return descricao;
    }

    public BigDecimal getSaldoEstoque() {
        return saldoEstoque;
    }

    public BigDecimal getValorUnitario() {
        return valorUnitario;
    }

    public LocalDate getDataCadastro() {
        return dataCadastro;
    }

    public Status getStatus() {
        return status;
    }

    public CategoriaProduto getCategoria() {
        return categoria;
    }

    private static String validarTextoObrigatorio(String texto, String mensagem) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException(mensagem);
        }
        return texto.trim();
    }

    private static BigDecimal validarNaoNegativo(BigDecimal valor, String mensagem) {
        Objects.requireNonNull(valor, mensagem);
        if (valor.signum() < 0) {
            throw new IllegalArgumentException(mensagem);
        }
        return valor;
    }

    private static void validarPositivo(BigDecimal valor, String mensagem) {
        Objects.requireNonNull(valor, mensagem);
        if (valor.signum() <= 0) {
            throw new IllegalArgumentException(mensagem);
        }
    }
}
