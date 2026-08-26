package com.curso.restaurante.domain.caixa;

import com.curso.restaurante.domain.comum.TransicaoDeStatusInvalidaException;
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

import static com.curso.restaurante.domain.comum.Validacoes.exigirNaoNegativo;
import static com.curso.restaurante.domain.comum.Validacoes.exigirNaoNulo;

@Entity
@Table(name = "sessao_caixa")
public class SessaoCaixa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "usuario_abertura_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_sessao_caixa_usuario_abertura"))
    private Usuario usuarioAbertura;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "usuario_fechamento_id",
            foreignKey = @ForeignKey(name = "fk_sessao_caixa_usuario_fechamento"))
    private Usuario usuarioFechamento;

    @Column(name = "valor_abertura", nullable = false, precision = 18, scale = 2)
    private BigDecimal valorAbertura;

    @Column(name = "valor_informado_fechamento", precision = 18, scale = 2)
    private BigDecimal valorInformadoFechamento;

    @Column(name = "valor_apurado_fechamento", precision = 18, scale = 2)
    private BigDecimal valorApuradoFechamento;

    @Column(name = "diferenca_fechamento", precision = 18, scale = 2)
    private BigDecimal diferencaFechamento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusSessaoCaixa status;

    @Column(name = "observacao_fechamento", length = 300)
    private String observacaoFechamento;

    @Column(name = "aberta_em", nullable = false)
    private LocalDateTime abertaEm;

    @Column(name = "fechada_em")
    private LocalDateTime fechadaEm;

    protected SessaoCaixa() {
    }

    public SessaoCaixa(Usuario usuarioAbertura, BigDecimal valorAbertura) {
        this.usuarioAbertura = exigirNaoNulo(usuarioAbertura, "Usuário de abertura é obrigatório");
        this.valorAbertura = exigirNaoNegativo(valorAbertura, "Valor de abertura não pode ser negativo");
        this.status = StatusSessaoCaixa.ABERTA;
        this.abertaEm = LocalDateTime.now();
    }

    public void fechar(Usuario usuarioFechamento, BigDecimal valorContado, BigDecimal valorApurado, String observacao) {
        if (this.status != StatusSessaoCaixa.ABERTA) {
            throw new TransicaoDeStatusInvalidaException("Não é possível fechar uma sessão de caixa já fechada");
        }

        this.usuarioFechamento = exigirNaoNulo(usuarioFechamento, "Usuário de fechamento é obrigatório");
        this.valorInformadoFechamento = exigirNaoNegativo(valorContado, "Valor contado não pode ser negativo");
        this.valorApuradoFechamento = exigirNaoNulo(valorApurado, "Valor apurado é obrigatório");
        this.diferencaFechamento = valorContado.subtract(valorApurado).setScale(2, RoundingMode.HALF_UP);
        this.observacaoFechamento = observacao == null ? null : observacao.trim();
        this.status = StatusSessaoCaixa.FECHADA;
        this.fechadaEm = LocalDateTime.now();
    }

    public BigDecimal calcularSaldoEsperadoEmDinheiro(BigDecimal recebidoEmDinheiro, BigDecimal totalSangrias) {
        return valorAbertura
                .add(recebidoEmDinheiro)
                .subtract(totalSangrias)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuarioAbertura() {
        return usuarioAbertura;
    }

    public Usuario getUsuarioFechamento() {
        return usuarioFechamento;
    }

    public BigDecimal getValorAbertura() {
        return valorAbertura;
    }

    public BigDecimal getValorInformadoFechamento() {
        return valorInformadoFechamento;
    }

    public BigDecimal getValorApuradoFechamento() {
        return valorApuradoFechamento;
    }

    public BigDecimal getDiferencaFechamento() {
        return diferencaFechamento;
    }

    public StatusSessaoCaixa getStatus() {
        return status;
    }

    public String getObservacaoFechamento() {
        return observacaoFechamento;
    }

    public LocalDateTime getAbertaEm() {
        return abertaEm;
    }

    public LocalDateTime getFechadaEm() {
        return fechadaEm;
    }
}
