package com.curso.restaurante.domain.pedido;

import com.curso.restaurante.domain.cardapio.ItemCardapio;
import com.curso.restaurante.domain.comanda.Comanda;
import com.curso.restaurante.domain.comum.RegraDeNegocioException;
import com.curso.restaurante.domain.comum.TransicaoDeStatusInvalidaException;
import com.curso.restaurante.domain.usuario.Usuario;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.curso.restaurante.domain.comum.Validacoes.exigirNaoNulo;
import static com.curso.restaurante.domain.comum.Validacoes.exigirTexto;

@Entity
@Table(
        name = "pedido",
        uniqueConstraints = @UniqueConstraint(name = "uk_pedido_codigo", columnNames = "codigo"))
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String codigo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comanda_id", nullable = false, foreignKey = @ForeignKey(name = "fk_pedido_comanda"))
    private Comanda comanda;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "usuario_solicitante_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_pedido_usuario_solicitante"))
    private Usuario solicitante;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusPedido status;

    @Column(length = 300)
    private String observacao;

    @Column(name = "motivo_cancelamento", length = 200)
    private String motivoCancelamento;

    @Column(name = "aberto_em", nullable = false)
    private LocalDateTime abertoEm;

    @Column(name = "enviado_preparo_em")
    private LocalDateTime enviadoPreparoEm;

    @Column(name = "pronto_em")
    private LocalDateTime prontoEm;

    @Column(name = "entregue_em")
    private LocalDateTime entregueEm;

    @Column(name = "pago_em")
    private LocalDateTime pagoEm;

    @Column(name = "cancelado_em")
    private LocalDateTime canceladoEm;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ItemPedido> itens = new ArrayList<>();

    protected Pedido() {
    }

    public Pedido(String codigo, Comanda comanda, Usuario solicitante, String observacao) {
        this.codigo = exigirTexto(codigo, "Código é obrigatório");
        this.comanda = exigirNaoNulo(comanda, "Comanda é obrigatória");
        this.solicitante = exigirNaoNulo(solicitante, "Solicitante é obrigatório");
        this.observacao = observacao == null ? null : observacao.trim();
        this.status = StatusPedido.ABERTO;
        this.abertoEm = LocalDateTime.now();
        comanda.registrarPedido(this);
    }

    public ItemPedido adicionarItem(ItemCardapio itemCardapio, int quantidade, String observacaoItem) {
        exigirStatusAberto("adicionar itens a");
        ItemPedido item = new ItemPedido(itemCardapio, quantidade, observacaoItem);
        item.associarA(this);
        itens.add(item);
        return item;
    }

    public void removerItem(ItemPedido item) {
        exigirStatusAberto("remover itens de");
        itens.remove(item);
    }

    public BigDecimal calcularTotal() {
        return itens.stream()
                .map(ItemPedido::calcularSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public void enviarParaPreparo() {
        if (itens.isEmpty()) {
            throw new RegraDeNegocioException("Pedido não pode ser enviado para preparo sem itens");
        }
        transicionarPara(StatusPedido.EM_PREPARO);
    }

    public void marcarComoPronto() {
        transicionarPara(StatusPedido.PRONTO);
    }

    public void marcarComoEntregue() {
        transicionarPara(StatusPedido.ENTREGUE);
    }

    public void marcarComoPago() {
        transicionarPara(StatusPedido.PAGO);
    }

    public void cancelar(String motivo) {
        this.motivoCancelamento = exigirTexto(motivo, "Motivo do cancelamento é obrigatório");
        transicionarPara(StatusPedido.CANCELADO);
    }

    private void exigirStatusAberto(String verbo) {
        if (this.status != StatusPedido.ABERTO) {
            throw new TransicaoDeStatusInvalidaException(
                    "Não é possível " + verbo + " um pedido no status " + this.status);
        }
    }

    private void transicionarPara(StatusPedido destino) {
        if (!status.podeTransicionarPara(destino)) {
            throw new TransicaoDeStatusInvalidaException(
                    "Não é possível mover o pedido de " + status + " para " + destino);
        }

        this.status = destino;
        LocalDateTime agora = LocalDateTime.now();

        switch (destino) {
            case EM_PREPARO -> enviadoPreparoEm = agora;
            case PRONTO -> prontoEm = agora;
            case ENTREGUE -> entregueEm = agora;
            case PAGO -> pagoEm = agora;
            case CANCELADO -> canceladoEm = agora;
            case ABERTO -> {
            }
        }
    }

    public Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public Comanda getComanda() {
        return comanda;
    }

    public Usuario getSolicitante() {
        return solicitante;
    }

    public StatusPedido getStatus() {
        return status;
    }

    public String getObservacao() {
        return observacao;
    }

    public String getMotivoCancelamento() {
        return motivoCancelamento;
    }

    public LocalDateTime getAbertoEm() {
        return abertoEm;
    }

    public LocalDateTime getEnviadoPreparoEm() {
        return enviadoPreparoEm;
    }

    public LocalDateTime getProntoEm() {
        return prontoEm;
    }

    public LocalDateTime getEntregueEm() {
        return entregueEm;
    }

    public LocalDateTime getPagoEm() {
        return pagoEm;
    }

    public LocalDateTime getCanceladoEm() {
        return canceladoEm;
    }

    public List<ItemPedido> getItens() {
        return List.copyOf(itens);
    }
}
