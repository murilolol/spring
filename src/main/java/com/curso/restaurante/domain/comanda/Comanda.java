package com.curso.restaurante.domain.comanda;

import com.curso.restaurante.domain.cliente.Cliente;
import com.curso.restaurante.domain.comum.RegraDeNegocioException;
import com.curso.restaurante.domain.comum.TransicaoDeStatusInvalidaException;
import com.curso.restaurante.domain.mesa.Mesa;
import com.curso.restaurante.domain.pedido.Pedido;
import com.curso.restaurante.domain.pedido.StatusPedido;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.curso.restaurante.domain.comum.Validacoes.exigirEntre;
import static com.curso.restaurante.domain.comum.Validacoes.exigirNaoNulo;
import static com.curso.restaurante.domain.comum.Validacoes.exigirPositivo;
import static com.curso.restaurante.domain.comum.Validacoes.exigirTexto;

@Entity
@Table(
        name = "comanda",
        uniqueConstraints = @UniqueConstraint(name = "uk_comanda_codigo", columnNames = "codigo"))
public class Comanda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String codigo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_atendimento", nullable = false, length = 20)
    private TipoAtendimento tipoAtendimento;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "mesa_id", foreignKey = @ForeignKey(name = "fk_comanda_mesa"))
    private Mesa mesa;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cliente_id", foreignKey = @ForeignKey(name = "fk_comanda_cliente"))
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(
            name = "usuario_responsavel_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_comanda_usuario_responsavel"))
    private Usuario responsavel;

    @Column(name = "numero_pessoas", nullable = false)
    private int numeroPessoas;

    @Column(name = "percentual_taxa_servico", nullable = false, precision = 5, scale = 2)
    private BigDecimal percentualTaxaServico;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusComanda status;

    @Column(name = "aberta_em", nullable = false)
    private LocalDateTime abertaEm;

    @Column(name = "fechada_em")
    private LocalDateTime fechadaEm;

    @Column(name = "cancelada_em")
    private LocalDateTime canceladaEm;

    @Column(length = 300)
    private String observacao;

    @OneToMany(mappedBy = "comanda", fetch = FetchType.LAZY)
    private List<Pedido> pedidos = new ArrayList<>();

    protected Comanda() {
    }

    public Comanda(
            String codigo,
            TipoAtendimento tipoAtendimento,
            Mesa mesa,
            Cliente cliente,
            Usuario responsavel,
            int numeroPessoas,
            BigDecimal percentualTaxaServico,
            String observacao) {
        this.codigo = exigirTexto(codigo, "Código é obrigatório");
        this.tipoAtendimento = exigirNaoNulo(tipoAtendimento, "Tipo de atendimento é obrigatório");
        validarMesaEClientePorTipo(tipoAtendimento, mesa, cliente);
        this.mesa = mesa;
        this.cliente = cliente;
        this.responsavel = exigirNaoNulo(responsavel, "Responsável é obrigatório");
        this.numeroPessoas = exigirPositivo(numeroPessoas, "Número de pessoas deve ser maior que zero");
        this.percentualTaxaServico = exigirEntre(
                percentualTaxaServico,
                BigDecimal.ZERO,
                new BigDecimal("100"),
                "Percentual de taxa de serviço deve estar entre 0 e 100");
        this.observacao = observacao == null ? null : observacao.trim();
        this.status = StatusComanda.ABERTA;
        this.abertaEm = LocalDateTime.now();
    }

    private static void validarMesaEClientePorTipo(TipoAtendimento tipo, Mesa mesa, Cliente cliente) {
        if (mesa == null && cliente == null) {
            throw new IllegalArgumentException("Comanda precisa estar vinculada a uma mesa ou a um cliente");
        }

        switch (tipo) {
            case SALAO -> {
                if (mesa == null) {
                    throw new IllegalArgumentException("Comanda de salão exige uma mesa");
                }
            }
            case DELIVERY, RETIRADA -> {
                if (cliente == null) {
                    throw new IllegalArgumentException("Comanda de " + tipo + " exige um cliente");
                }
                if (mesa != null) {
                    throw new IllegalArgumentException("Comanda de " + tipo + " não pode estar vinculada a uma mesa");
                }
            }
            case BALCAO -> {
            }
        }
    }

    public void fechar() {
        exigirStatusAtual(StatusComanda.ABERTA, "fechar");

        boolean temPedidoEntregue = pedidos.stream().anyMatch(p -> p.getStatus() == StatusPedido.ENTREGUE);
        boolean todosPedidosFinalizados = pedidos.stream()
                .allMatch(p -> p.getStatus() == StatusPedido.ENTREGUE || p.getStatus() == StatusPedido.CANCELADO);

        if (!temPedidoEntregue || !todosPedidosFinalizados) {
            throw new RegraDeNegocioException(
                    "Comanda só pode ser fechada quando todos os pedidos estiverem entregues ou cancelados, "
                            + "com ao menos um pedido entregue");
        }

        this.status = StatusComanda.FECHADA;
        this.fechadaEm = LocalDateTime.now();
    }

    public void reabrir() {
        exigirStatusAtual(StatusComanda.FECHADA, "reabrir");
        this.status = StatusComanda.ABERTA;
        this.fechadaEm = null;
    }

    public void cancelar(String motivo) {
        exigirStatusAtual(StatusComanda.ABERTA, "cancelar");
        exigirTexto(motivo, "Motivo do cancelamento é obrigatório");

        boolean temPedidoJaEntregueOuPago = pedidos.stream()
                .anyMatch(p -> p.getStatus() == StatusPedido.ENTREGUE || p.getStatus() == StatusPedido.PAGO);

        if (temPedidoJaEntregueOuPago) {
            throw new RegraDeNegocioException("Comanda com pedido já entregue não pode ser cancelada");
        }

        this.status = StatusComanda.CANCELADA;
        this.canceladaEm = LocalDateTime.now();
    }

    public void transferirParaMesa(Mesa novaMesa) {
        exigirStatusAtual(StatusComanda.ABERTA, "transferir a mesa de");
        exigirNaoNulo(novaMesa, "Nova mesa é obrigatória");

        if (tipoAtendimento == TipoAtendimento.DELIVERY || tipoAtendimento == TipoAtendimento.RETIRADA) {
            throw new IllegalArgumentException(
                    "Comanda de " + tipoAtendimento + " não pode ser vinculada a uma mesa");
        }

        this.mesa = novaMesa;
    }

    public void marcarComoPaga() {
        exigirStatusAtual(StatusComanda.FECHADA, "marcar como paga");
        this.status = StatusComanda.PAGA;

        for (Pedido pedido : pedidos) {
            if (pedido.getStatus() == StatusPedido.ENTREGUE) {
                pedido.marcarComoPago();
            }
        }
    }

    public BigDecimal calcularSubtotal() {
        return pedidos.stream()
                .filter(pedido -> pedido.getStatus() != StatusPedido.CANCELADO)
                .map(Pedido::calcularTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, java.math.RoundingMode.HALF_UP);
    }

    public BigDecimal calcularTaxaServico() {
        return calcularSubtotal()
                .multiply(percentualTaxaServico)
                .divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
    }

    public BigDecimal calcularTotal() {
        return calcularSubtotal().add(calcularTaxaServico());
    }

    public BigDecimal calcularSaldoDevedor(BigDecimal jaPago) {
        return calcularTotal().subtract(jaPago).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private void exigirStatusAtual(StatusComanda esperado, String verbo) {
        if (this.status != esperado) {
            throw new TransicaoDeStatusInvalidaException(
                    "Não é possível " + verbo + " a comanda no status " + this.status);
        }
    }

    public Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public TipoAtendimento getTipoAtendimento() {
        return tipoAtendimento;
    }

    public Mesa getMesa() {
        return mesa;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public Usuario getResponsavel() {
        return responsavel;
    }

    public int getNumeroPessoas() {
        return numeroPessoas;
    }

    public BigDecimal getPercentualTaxaServico() {
        return percentualTaxaServico;
    }

    public StatusComanda getStatus() {
        return status;
    }

    public LocalDateTime getAbertaEm() {
        return abertaEm;
    }

    public LocalDateTime getFechadaEm() {
        return fechadaEm;
    }

    public LocalDateTime getCanceladaEm() {
        return canceladaEm;
    }

    public String getObservacao() {
        return observacao;
    }

    public void registrarPedido(Pedido pedido) {
        pedidos.add(exigirNaoNulo(pedido, "Pedido é obrigatório"));
    }

    public List<Pedido> getPedidos() {
        return List.copyOf(pedidos);
    }
}
