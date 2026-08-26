package com.curso.restaurante.domain.mesa;

import com.curso.restaurante.domain.comum.TransicaoDeStatusInvalidaException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MesaTest {

    @Test
    void deveCriarMesaLivreComDadosValidos() {
        Mesa mesa = new Mesa(10, 4, "Salão Principal");

        assertEquals(10, mesa.getNumero());
        assertEquals(4, mesa.getCapacidade());
        assertEquals("Salão Principal", mesa.getSetor());
        assertEquals(StatusMesa.LIVRE, mesa.getStatus());
    }

    @Test
    void deveRejeitarCapacidadeMenorOuIgualAZero() {
        assertThrows(IllegalArgumentException.class, () -> new Mesa(10, 0, "Salão Principal"));
        assertThrows(IllegalArgumentException.class, () -> new Mesa(10, -1, "Salão Principal"));
    }

    @Test
    void deveRejeitarSetorEmBranco() {
        assertThrows(IllegalArgumentException.class, () -> new Mesa(10, 4, "   "));
    }

    @Test
    void deveReservarMesaLivre() {
        Mesa mesa = new Mesa(10, 4, "Salão Principal");

        mesa.reservar();

        assertEquals(StatusMesa.RESERVADA, mesa.getStatus());
    }

    @Test
    void naoDeveReservarMesaJaOcupada() {
        Mesa mesa = new Mesa(10, 4, "Salão Principal");
        mesa.ocupar();

        assertThrows(TransicaoDeStatusInvalidaException.class, mesa::reservar);
    }

    @Test
    void deveCancelarReservaDeMesaReservada() {
        Mesa mesa = new Mesa(10, 4, "Salão Principal");
        mesa.reservar();

        mesa.cancelarReserva();

        assertEquals(StatusMesa.LIVRE, mesa.getStatus());
    }

    @Test
    void naoDeveCancelarReservaDeMesaLivre() {
        Mesa mesa = new Mesa(10, 4, "Salão Principal");

        assertThrows(TransicaoDeStatusInvalidaException.class, mesa::cancelarReserva);
    }

    @Test
    void deveOcuparMesaLivreOuReservada() {
        Mesa livre = new Mesa(10, 4, "Salão Principal");
        livre.ocupar();
        assertEquals(StatusMesa.OCUPADA, livre.getStatus());

        Mesa reservada = new Mesa(11, 4, "Salão Principal");
        reservada.reservar();
        reservada.ocupar();
        assertEquals(StatusMesa.OCUPADA, reservada.getStatus());
    }

    @Test
    void naoDeveOcuparMesaJaOcupada() {
        Mesa mesa = new Mesa(10, 4, "Salão Principal");
        mesa.ocupar();

        assertThrows(TransicaoDeStatusInvalidaException.class, mesa::ocupar);
    }

    @Test
    void naoDeveOcuparMesaInterditada() {
        Mesa mesa = new Mesa(10, 4, "Salão Principal");
        mesa.interditar();

        assertThrows(TransicaoDeStatusInvalidaException.class, mesa::ocupar);
    }

    @Test
    void deveLiberarMesaOcupada() {
        Mesa mesa = new Mesa(10, 4, "Salão Principal");
        mesa.ocupar();

        mesa.liberar();

        assertEquals(StatusMesa.LIVRE, mesa.getStatus());
    }

    @Test
    void naoDeveLiberarMesaQueNaoEstaOcupada() {
        Mesa mesa = new Mesa(10, 4, "Salão Principal");

        assertThrows(TransicaoDeStatusInvalidaException.class, mesa::liberar);
    }

    @Test
    void deveInterditarMesaLivreOuReservada() {
        Mesa mesa = new Mesa(10, 4, "Salão Principal");

        mesa.interditar();

        assertEquals(StatusMesa.INTERDITADA, mesa.getStatus());
    }

    @Test
    void naoDeveInterditarMesaOcupada() {
        Mesa mesa = new Mesa(10, 4, "Salão Principal");
        mesa.ocupar();

        assertThrows(TransicaoDeStatusInvalidaException.class, mesa::interditar);
    }

    @Test
    void deveLiberarInterdicaoDeMesaInterditada() {
        Mesa mesa = new Mesa(10, 4, "Salão Principal");
        mesa.interditar();

        mesa.liberarInterdicao();

        assertEquals(StatusMesa.LIVRE, mesa.getStatus());
    }

    @Test
    void naoDeveLiberarInterdicaoDeMesaQueNaoEstaInterditada() {
        Mesa mesa = new Mesa(10, 4, "Salão Principal");

        assertThrows(TransicaoDeStatusInvalidaException.class, mesa::liberarInterdicao);
    }

    @Test
    void deveAlterarCapacidadeESetor() {
        Mesa mesa = new Mesa(10, 4, "Salão Principal");

        mesa.alterarCapacidadeESetor(6, "Varanda");

        assertEquals(6, mesa.getCapacidade());
        assertEquals("Varanda", mesa.getSetor());
    }
}
