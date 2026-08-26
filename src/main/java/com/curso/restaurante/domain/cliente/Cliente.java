package com.curso.restaurante.domain.cliente;

import com.curso.restaurante.domain.Status;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

import static com.curso.restaurante.domain.comum.Validacoes.exigirTexto;

@Entity
@Table(name = "cliente")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(length = 14)
    private String documento;

    @Column(nullable = false, length = 20)
    private String telefone;

    @Column(length = 150)
    private String email;

    @Column(length = 200)
    private String endereco;

    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    @Column(name = "data_cadastro", nullable = false)
    private LocalDate dataCadastro;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    protected Cliente() {
    }

    public Cliente(
            String nome,
            String documento,
            String telefone,
            String email,
            String endereco,
            LocalDate dataNascimento) {
        this.nome = exigirTexto(nome, "Nome é obrigatório");
        this.documento = documento == null ? null : documento.trim();
        this.telefone = exigirTexto(telefone, "Telefone é obrigatório");
        this.email = email == null ? null : email.trim();
        this.endereco = endereco == null ? null : endereco.trim();
        this.dataNascimento = dataNascimento;
        this.dataCadastro = LocalDate.now();
        this.status = Status.ATIVO;
    }

    public void atualizarDados(String nome, String telefone, String email, String endereco) {
        this.nome = exigirTexto(nome, "Nome é obrigatório");
        this.telefone = exigirTexto(telefone, "Telefone é obrigatório");
        this.email = email == null ? null : email.trim();
        this.endereco = endereco == null ? null : endereco.trim();
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

    public String getDocumento() {
        return documento;
    }

    public String getTelefone() {
        return telefone;
    }

    public String getEmail() {
        return email;
    }

    public String getEndereco() {
        return endereco;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public LocalDate getDataCadastro() {
        return dataCadastro;
    }

    public Status getStatus() {
        return status;
    }
}
