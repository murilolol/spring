package com.curso.restaurante.domain.caixa;

import com.curso.restaurante.domain.comum.ConflitoDeEstadoException;
import com.curso.restaurante.domain.usuario.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static com.curso.restaurante.domain.comum.Validacoes.exigirNaoNulo;
import static com.curso.restaurante.domain.comum.Validacoes.exigirPositivo;
import static com.curso.restaurante.domain.comum.Validacoes.exigirTexto;

@Entity
@Table(name = "sangria")
public class Sangria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "sessao_caixa_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_sangria_sessao_caixa"))
    private SessaoCaixa sessaoCaixa;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal valor;

    @Column(nullable = false, length = 200)
    private String motivo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, foreignKey = @ForeignKey(name = "fk_sangria_usuario"))
    private Usuario usuario;

    @Column(name = "registrada_em", nullable = false)
    private LocalDateTime registradaEm;

    protected Sangria() {
    }

    public Sangria(SessaoCaixa sessaoCaixa, BigDecimal valor, String motivo, Usuario usuario) {
        this.sessaoCaixa = exigirNaoNulo(sessaoCaixa, "Sessão de caixa é obrigatória");

        if (sessaoCaixa.getStatus() != StatusSessaoCaixa.ABERTA) {
            throw new ConflitoDeEstadoException("Não é possível registrar sangria em uma sessão de caixa fechada");
        }

        this.valor = exigirPositivo(valor, "Valor da sangria deve ser maior que zero");
        this.motivo = exigirTexto(motivo, "Motivo da sangria é obrigatório");
        this.usuario = exigirNaoNulo(usuario, "Usuário é obrigatório");
        this.registradaEm = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public SessaoCaixa getSessaoCaixa() {
        return sessaoCaixa;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public String getMotivo() {
        return motivo;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public LocalDateTime getRegistradaEm() {
        return registradaEm;
    }
}
