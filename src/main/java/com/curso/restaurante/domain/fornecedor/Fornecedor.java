package com.curso.restaurante.domain.fornecedor;

import com.curso.restaurante.domain.Status;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import static com.curso.restaurante.domain.comum.Validacoes.exigirCnpj;
import static com.curso.restaurante.domain.comum.Validacoes.exigirTexto;

@Entity
@Table(
        name = "fornecedor",
        uniqueConstraints = @UniqueConstraint(name = "uk_fornecedor_cnpj", columnNames = "cnpj"))
public class Fornecedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "razao_social", nullable = false, length = 150)
    private String razaoSocial;

    @Column(nullable = false, length = 14)
    private String cnpj;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    protected Fornecedor() {
    }

    public Fornecedor(String razaoSocial, String cnpj) {
        this.razaoSocial = exigirTexto(razaoSocial, "Razão social é obrigatória");
        this.cnpj = exigirCnpj(cnpj, "CNPJ deve conter 14 dígitos numéricos");
        this.status = Status.ATIVO;
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

    public String getRazaoSocial() {
        return razaoSocial;
    }

    public String getCnpj() {
        return cnpj;
    }

    public Status getStatus() {
        return status;
    }
}
