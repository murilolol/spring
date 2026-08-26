package com.curso.restaurante.domain.caixa;

import com.curso.restaurante.domain.comum.ConflitoDeEstadoException;
import com.curso.restaurante.domain.usuario.PerfilUsuario;
import com.curso.restaurante.domain.usuario.Usuario;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SangriaTest {

    @Test
    void deveRegistrarSangriaEmSessaoAberta() {
        SessaoCaixa sessao = new SessaoCaixa(umUsuario(), new BigDecimal("100.00"));

        Sangria sangria = new Sangria(sessao, new BigDecimal("50.00"), "Reforço de troco no bar", umUsuario());

        assertEquals(0, new BigDecimal("50.00").compareTo(sangria.getValor()));
        assertEquals("Reforço de troco no bar", sangria.getMotivo());
        assertNotNull(sangria.getRegistradaEm());
    }

    @Test
    void deveRejeitarValorMenorOuIgualAZero() {
        SessaoCaixa sessao = new SessaoCaixa(umUsuario(), new BigDecimal("100.00"));

        assertThrows(IllegalArgumentException.class, () -> new Sangria(sessao, BigDecimal.ZERO, "motivo", umUsuario()));
    }

    @Test
    void deveRejeitarMotivoEmBranco() {
        SessaoCaixa sessao = new SessaoCaixa(umUsuario(), new BigDecimal("100.00"));

        assertThrows(
                IllegalArgumentException.class,
                () -> new Sangria(sessao, new BigDecimal("10.00"), "   ", umUsuario()));
    }

    @Test
    void naoDeveRegistrarSangriaEmSessaoFechada() {
        SessaoCaixa sessao = new SessaoCaixa(umUsuario(), new BigDecimal("100.00"));
        sessao.fechar(umUsuario(), BigDecimal.ZERO, BigDecimal.ZERO, null);

        assertThrows(
                ConflitoDeEstadoException.class,
                () -> new Sangria(sessao, new BigDecimal("10.00"), "motivo", umUsuario()));
    }

    private Usuario umUsuario() {
        return new Usuario("Caixa Sangria Teste", "caixa.sangria.teste." + Math.random(), "hash", PerfilUsuario.CAIXA);
    }
}
