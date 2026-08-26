package com.curso.restaurante.domain.cozinha;

import com.curso.restaurante.domain.cardapio.SecaoPreparo;
import com.curso.restaurante.domain.comum.TransicaoDeStatusInvalidaException;
import com.curso.restaurante.domain.pedido.ItemPedido;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Set;

import static com.curso.restaurante.domain.comum.Validacoes.exigirNaoNulo;

@Entity
@Table(
        name = "preparo_item",
        uniqueConstraints = @UniqueConstraint(name = "uk_preparo_item_item_pedido", columnNames = "item_pedido_id"))
public class PreparoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "item_pedido_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_preparo_item_item_pedido"))
    private ItemPedido itemPedido;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SecaoPreparo secao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusPreparo status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PrioridadePreparo prioridade;

    @Column(name = "tempo_estimado_minutos", nullable = false)
    private int tempoEstimadoMinutos;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "usuario_responsavel_id",
            foreignKey = @ForeignKey(name = "fk_preparo_item_usuario_responsavel"))
    private Usuario responsavel;

    @Column(name = "enfileirado_em", nullable = false)
    private LocalDateTime enfileiradoEm;

    @Column(name = "iniciado_em")
    private LocalDateTime iniciadoEm;

    @Column(name = "concluido_em")
    private LocalDateTime concluidoEm;

    @Column(name = "cancelado_em")
    private LocalDateTime canceladoEm;

    protected PreparoItem() {
    }

    public PreparoItem(ItemPedido itemPedido, SecaoPreparo secao, int tempoEstimadoMinutos) {
        this.itemPedido = exigirNaoNulo(itemPedido, "Item de pedido é obrigatório");
        this.secao = exigirNaoNulo(secao, "Seção de preparo é obrigatória");
        this.tempoEstimadoMinutos = tempoEstimadoMinutos;
        this.status = StatusPreparo.AGUARDANDO;
        this.prioridade = PrioridadePreparo.NORMAL;
        this.enfileiradoEm = LocalDateTime.now();
    }

    public void iniciar(Usuario responsavel) {
        exigirStatusEntre("iniciar", EnumSet.of(StatusPreparo.AGUARDANDO));
        this.responsavel = exigirNaoNulo(responsavel, "Responsável é obrigatório");
        this.status = StatusPreparo.EM_PREPARO;
        this.iniciadoEm = LocalDateTime.now();
    }

    public void concluir() {
        exigirStatusEntre("concluir", EnumSet.of(StatusPreparo.EM_PREPARO));
        this.status = StatusPreparo.CONCLUIDO;
        this.concluidoEm = LocalDateTime.now();
    }

    public void cancelar() {
        exigirStatusEntre("cancelar", EnumSet.of(StatusPreparo.AGUARDANDO, StatusPreparo.EM_PREPARO));
        this.status = StatusPreparo.CANCELADO;
        this.canceladoEm = LocalDateTime.now();
    }

    public void alterarPrioridade(PrioridadePreparo novaPrioridade) {
        this.prioridade = exigirNaoNulo(novaPrioridade, "Prioridade é obrigatória");
    }

    public long calcularTempoDecorridoMinutos(LocalDateTime referencia) {
        return Duration.between(enfileiradoEm, referencia).toMinutes();
    }

    public boolean estaAtrasado(LocalDateTime referencia) {
        if (status == StatusPreparo.CONCLUIDO || status == StatusPreparo.CANCELADO) {
            return false;
        }
        return calcularTempoDecorridoMinutos(referencia) > tempoEstimadoMinutos;
    }

    private void exigirStatusEntre(String verbo, Set<StatusPreparo> statusPermitidos) {
        if (!statusPermitidos.contains(this.status)) {
            throw new TransicaoDeStatusInvalidaException(
                    "Não é possível " + verbo + " um item de preparo no status " + this.status);
        }
    }

    public Long getId() {
        return id;
    }

    public ItemPedido getItemPedido() {
        return itemPedido;
    }

    public SecaoPreparo getSecao() {
        return secao;
    }

    public StatusPreparo getStatus() {
        return status;
    }

    public PrioridadePreparo getPrioridade() {
        return prioridade;
    }

    public int getTempoEstimadoMinutos() {
        return tempoEstimadoMinutos;
    }

    public Usuario getResponsavel() {
        return responsavel;
    }

    public LocalDateTime getEnfileiradoEm() {
        return enfileiradoEm;
    }

    public LocalDateTime getIniciadoEm() {
        return iniciadoEm;
    }

    public LocalDateTime getConcluidoEm() {
        return concluidoEm;
    }

    public LocalDateTime getCanceladoEm() {
        return canceladoEm;
    }
}
