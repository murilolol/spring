package com.curso.restaurante.domain.fornecedor;

import com.curso.restaurante.domain.Status;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FornecedorTest {

    @Test
    void deveCriarFornecedorAtivoComDadosValidos() {
        Fornecedor fornecedor = new Fornecedor("Distribuidora Bom Sabor Ltda", "12345678901234");

        assertEquals("Distribuidora Bom Sabor Ltda", fornecedor.getRazaoSocial());
        assertEquals("12345678901234", fornecedor.getCnpj());
        assertEquals(Status.ATIVO, fornecedor.getStatus());
    }

    @Test
    void deveRejeitarRazaoSocialEmBranco() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Fornecedor("   ", "12345678901234"));
    }

    @Test
    void deveRejeitarCnpjComFormatoInvalido() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Fornecedor("Distribuidora Bom Sabor Ltda", "123"));
    }

    @Test
    void inativarDeveMudarStatusParaInativo() {
        Fornecedor fornecedor = new Fornecedor("Distribuidora Bom Sabor Ltda", "12345678901234");

        fornecedor.inativar();

        assertEquals(Status.INATIVO, fornecedor.getStatus());
    }

    @Test
    void ativarDeveMudarStatusParaAtivo() {
        Fornecedor fornecedor = new Fornecedor("Distribuidora Bom Sabor Ltda", "12345678901234");
        fornecedor.inativar();

        fornecedor.ativar();

        assertEquals(Status.ATIVO, fornecedor.getStatus());
    }
}
