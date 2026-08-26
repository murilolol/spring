package com.curso.restaurante.domain.mesa;

import com.curso.restaurante.domain.comum.TransicaoDeStatusInvalidaException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.EnumSet;
import java.util.Set;

import static com.curso.restaurante.domain.comum.Validacoes.exigirPositivo;
import static com.curso.restaurante.domain.comum.Validacoes.exigirTexto;

@Entity
@Table(
        name = "mesa",
        uniqueConstraints = @UniqueConstraint(name = "uk_mesa_numero", columnNames = "numero"))
public class Mesa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int numero;

    @Column(nullable = false)
    private int capacidade;

    @Column(nullable = false, length = 60)
    private String setor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusMesa status;

    protected Mesa() {
    }

    public Mesa(int numero, int capacidade, String setor) {
        this.numero = numero;
        this.capacidade = exigirPositivo(capacidade, "Capacidade deve ser maior que zero");
        this.setor = exigirTexto(setor, "Setor é obrigatório");
        this.status = StatusMesa.LIVRE;
    }

    public void reservar() {
        transicionarPara(StatusMesa.RESERVADA, "reservar", EnumSet.of(StatusMesa.LIVRE));
    }

    public void cancelarReserva() {
        transicionarPara(StatusMesa.LIVRE, "cancelar a reserva de", EnumSet.of(StatusMesa.RESERVADA));
    }

    public void ocupar() {
        transicionarPara(StatusMesa.OCUPADA, "ocupar", EnumSet.of(StatusMesa.LIVRE, StatusMesa.RESERVADA));
    }

    public void liberar() {
        transicionarPara(StatusMesa.LIVRE, "liberar", EnumSet.of(StatusMesa.OCUPADA));
    }

    public void interditar() {
        transicionarPara(StatusMesa.INTERDITADA, "interditar", EnumSet.of(StatusMesa.LIVRE, StatusMesa.RESERVADA));
    }

    public void liberarInterdicao() {
        transicionarPara(StatusMesa.LIVRE, "liberar a interdição de", EnumSet.of(StatusMesa.INTERDITADA));
    }

    public void alterarCapacidadeESetor(int novaCapacidade, String novoSetor) {
        this.capacidade = exigirPositivo(novaCapacidade, "Capacidade deve ser maior que zero");
        this.setor = exigirTexto(novoSetor, "Setor é obrigatório");
    }

    private void transicionarPara(StatusMesa destino, String verbo, Set<StatusMesa> origensPermitidas) {
        if (!origensPermitidas.contains(this.status)) {
            throw new TransicaoDeStatusInvalidaException(
                    "Não é possível " + verbo + " a mesa no status " + this.status);
        }
        this.status = destino;
    }

    public Long getId() {
        return id;
    }

    public int getNumero() {
        return numero;
    }

    public int getCapacidade() {
        return capacidade;
    }

    public String getSetor() {
        return setor;
    }

    public StatusMesa getStatus() {
        return status;
    }
}
