package com.curso.restaurante.domain.pedido;

import com.curso.restaurante.domain.cardapio.ItemCardapio;
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
import java.math.RoundingMode;

import static com.curso.restaurante.domain.comum.Validacoes.exigirNaoNulo;
import static com.curso.restaurante.domain.comum.Validacoes.exigirPositivo;

@Entity
@Table(name = "item_pedido")
public class ItemPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false, foreignKey = @ForeignKey(name = "fk_item_pedido_pedido"))
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "item_cardapio_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_item_pedido_item_cardapio"))
    private ItemCardapio itemCardapio;

    @Column(nullable = false)
    private int quantidade;

    @Column(name = "preco_unitario", nullable = false, precision = 18, scale = 2)
    private BigDecimal precoUnitario;

    @Column(length = 200)
    private String observacao;

    protected ItemPedido() {
    }

    public ItemPedido(ItemCardapio itemCardapio, int quantidade, String observacao) {
        this.itemCardapio = exigirNaoNulo(itemCardapio, "Item de cardápio é obrigatório");
        this.quantidade = exigirPositivo(quantidade, "Quantidade deve ser maior que zero");
        this.precoUnitario = itemCardapio.getPrecoVenda();
        this.observacao = observacao == null ? null : observacao.trim();
    }

    public BigDecimal calcularSubtotal() {
        return precoUnitario
                .multiply(BigDecimal.valueOf(quantidade))
                .setScale(2, RoundingMode.HALF_UP);
    }

    void associarA(Pedido pedido) {
        this.pedido = exigirNaoNulo(pedido, "Pedido é obrigatório");
    }

    public Long getId() {
        return id;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public ItemCardapio getItemCardapio() {
        return itemCardapio;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public BigDecimal getPrecoUnitario() {
        return precoUnitario;
    }

    public String getObservacao() {
        return observacao;
    }
}
