package com.curso.restaurante.domain.usuario;

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

import java.time.LocalDateTime;

import static com.curso.restaurante.domain.comum.Validacoes.exigirNaoNulo;
import static com.curso.restaurante.domain.comum.Validacoes.exigirTexto;

@Entity
@Table(
        name = "usuario",
        uniqueConstraints = @UniqueConstraint(name = "uk_usuario_username", columnNames = "username"))
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(name = "senha_hash", nullable = false, length = 100)
    private String senhaHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PerfilUsuario perfil;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    protected Usuario() {
    }

    public Usuario(String nome, String username, String senhaHash, PerfilUsuario perfil) {
        this.nome = exigirTexto(nome, "Nome é obrigatório");
        this.username = exigirTexto(username, "Username é obrigatório");
        this.senhaHash = exigirNaoNulo(senhaHash, "Senha é obrigatória");
        this.perfil = exigirNaoNulo(perfil, "Perfil é obrigatório");
        this.status = Status.ATIVO;
        this.criadoEm = LocalDateTime.now();
    }

    public void alterarNome(String novoNome) {
        this.nome = exigirTexto(novoNome, "Nome é obrigatório");
    }

    public void alterarSenha(String novaSenhaHash) {
        this.senhaHash = exigirNaoNulo(novaSenhaHash, "Senha é obrigatória");
    }

    public void alterarPerfil(PerfilUsuario novoPerfil) {
        this.perfil = exigirNaoNulo(novoPerfil, "Perfil é obrigatório");
    }

    public void ativar() {
        this.status = Status.ATIVO;
    }

    public void inativar() {
        this.status = Status.INATIVO;
    }

    public boolean estaAtivo() {
        return this.status == Status.ATIVO;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getUsername() {
        return username;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public PerfilUsuario getPerfil() {
        return perfil;
    }

    public Status getStatus() {
        return status;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }
}
