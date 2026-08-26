package com.curso.restaurante.domain.caixa;

import com.curso.restaurante.domain.comanda.Comanda;
import com.curso.restaurante.domain.comum.ConflitoDeEstadoException;
import com.curso.restaurante.domain.usuario.Usuario;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import static com.curso.restaurante.domain.comum.Validacoes.exigirNaoNulo;
import static com.curso.restaurante.domain.comum.Validacoes.exigirPositivo;

@Entity
@Table(name = "pagamento")
public class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comanda_id", nullable = false, foreignKey = @ForeignKey(name = "fk_pagamento_comanda"))
    private Comanda comanda;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "sessao_caixa_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_pagamento_sessao_caixa"))
    private SessaoCaixa sessaoCaixa;

    @Enumerated(EnumType.STRING)
    @Column(name = "forma_pagamento", nullable = false, length = 20)
    private FormaPagamento formaPagamento;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal valor;

    @Column(name = "valor_recebido", precision = 18, scale = 2)
    private BigDecimal valorRecebido;

    @Column(precision = 18, scale = 2)
    private BigDecimal troco;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, foreignKey = @ForeignKey(name = "fk_pagamento_usuario"))
    private Usuario usuario;

    @Column(name = "registrado_em", nullable = false)
    private LocalDateTime registradoEm;

    protected Pagamento() {
    }

    public Pagamento(
            Comanda comanda,
            SessaoCaixa sessaoCaixa,
            FormaPagamento formaPagamento,
            BigDecimal valor,
            BigDecimal valorRecebido,
            Usuario usuario) {
        this.comanda = exigirNaoNulo(comanda, "Comanda é obrigatória");
        this.sessaoCaixa = exigirNaoNulo(sessaoCaixa, "Sessão de caixa é obrigatória");

        if (sessaoCaixa.getStatus() != StatusSessaoCaixa.ABERTA) {
            throw new ConflitoDeEstadoException("Não é possível registrar pagamento em uma sessão de caixa fechada");
        }

        this.formaPagamento = exigirNaoNulo(formaPagamento, "Forma de pagamento é obrigatória");
        this.valor = exigirPositivo(valor, "Valor do pagamento deve ser maior que zero");
        this.usuario = exigirNaoNulo(usuario, "Usuário é obrigatório");
        this.registradoEm = LocalDateTime.now();

        if (formaPagamento == FormaPagamento.DINHEIRO) {
            exigirNaoNulo(valorRecebido, "Valor recebido é obrigatório para pagamento em dinheiro");
            if (valorRecebido.compareTo(valor) < 0) {
                throw new IllegalArgumentException("Valor recebido é insuficiente para o pagamento em dinheiro");
            }
            this.valorRecebido = valorRecebido;
            this.troco = valorRecebido.subtract(valor).setScale(2, RoundingMode.HALF_UP);
        } else {
            if (valorRecebido != null) {
                throw new IllegalArgumentException(
                        "Valor recebido só é aplicável ao pagamento em dinheiro");
            }
            this.valorRecebido = null;
            this.troco = null;
        }
    }

    public Long getId() {
        return id;
    }

    public Comanda getComanda() {
        return comanda;
    }

    public SessaoCaixa getSessaoCaixa() {
        return sessaoCaixa;
    }

    public FormaPagamento getFormaPagamento() {
        return formaPagamento;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public BigDecimal getValorRecebido() {
        return valorRecebido;
    }

    public BigDecimal getTroco() {
        return troco;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public LocalDateTime getRegistradoEm() {
        return registradoEm;
    }
}
