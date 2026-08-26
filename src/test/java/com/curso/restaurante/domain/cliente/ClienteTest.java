package com.curso.restaurante.domain.cliente;

import com.curso.restaurante.domain.Status;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ClienteTest {

    @Test
    void deveCriarClienteAtivoComDadosCompletos() {
        Cliente cliente = new Cliente(
                "Maria Souza",
                "12345678901",
                "(45) 99999-0000",
                "maria@example.com",
                "Rua das Flores, 123",
                LocalDate.of(1990, 5, 20));

        assertEquals("Maria Souza", cliente.getNome());
        assertEquals("12345678901", cliente.getDocumento());
        assertEquals("(45) 99999-0000", cliente.getTelefone());
        assertEquals("maria@example.com", cliente.getEmail());
        assertEquals("Rua das Flores, 123", cliente.getEndereco());
        assertEquals(LocalDate.of(1990, 5, 20), cliente.getDataNascimento());
        assertEquals(Status.ATIVO, cliente.getStatus());
        assertNotNull(cliente.getDataCadastro());
    }

    @Test
    void deveCriarClienteApenasComCamposObrigatorios() {
        Cliente cliente = new Cliente("João Pereira", null, "(45) 98888-1111", null, null, null);

        assertEquals("João Pereira", cliente.getNome());
        assertNull(cliente.getDocumento());
        assertNull(cliente.getEmail());
        assertNull(cliente.getEndereco());
        assertNull(cliente.getDataNascimento());
    }

    @Test
    void deveRejeitarNomeEmBranco() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Cliente("   ", null, "(45) 98888-1111", null, null, null));
    }

    @Test
    void deveRejeitarTelefoneEmBranco() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Cliente("João Pereira", null, "   ", null, null, null));
    }

    @Test
    void deveAtualizarDados() {
        Cliente cliente = new Cliente("João Pereira", null, "(45) 98888-1111", null, null, null);

        cliente.atualizarDados("João P. Silva", "(45) 97777-2222", "joao@example.com", "Avenida Brasil, 500");

        assertEquals("João P. Silva", cliente.getNome());
        assertEquals("(45) 97777-2222", cliente.getTelefone());
        assertEquals("joao@example.com", cliente.getEmail());
        assertEquals("Avenida Brasil, 500", cliente.getEndereco());
    }

    @Test
    void deveInativarEAtivar() {
        Cliente cliente = new Cliente("João Pereira", null, "(45) 98888-1111", null, null, null);

        cliente.inativar();
        assertEquals(Status.INATIVO, cliente.getStatus());

        cliente.ativar();
        assertEquals(Status.ATIVO, cliente.getStatus());
    }
}
