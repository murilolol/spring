package com.curso.restaurante.domain.caixa;

import com.curso.restaurante.domain.comum.TransicaoDeStatusInvalidaException;
import com.curso.restaurante.domain.usuario.PerfilUsuario;
import com.curso.restaurante.domain.usuario.Usuario;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SessaoCaixaTest {

    @Test
    void deveAbrirSessaoComStatusAberta() {
        SessaoCaixa sessao = new SessaoCaixa(umUsuario(), new BigDecimal("100.00"));

        assertEquals(StatusSessaoCaixa.ABERTA, sessao.getStatus());
        assertEquals(0, new BigDecimal("100.00").compareTo(sessao.getValorAbertura()));
        assertNotNull(sessao.getAbertaEm());
        assertNull(sessao.getFechadaEm());
    }

    @Test
    void deveRejeitarValorDeAberturaNegativo() {
        assertThrows(IllegalArgumentException.class, () -> new SessaoCaixa(umUsuario(), new BigDecimal("-1.00")));
    }

    @Test
    void deveFecharSessaoRegistrandoConferencia() {
        SessaoCaixa sessao = new SessaoCaixa(umUsuario(), new BigDecimal("100.00"));
        Usuario fechamento = umUsuario();

        sessao.fechar(fechamento, new BigDecimal("450.00"), new BigDecimal("460.00"), "Faltou troco");

        assertEquals(StatusSessaoCaixa.FECHADA, sessao.getStatus());
        assertEquals(fechamento, sessao.getUsuarioFechamento());
        assertEquals(0, new BigDecimal("450.00").compareTo(sessao.getValorInformadoFechamento()));
        assertEquals(0, new BigDecimal("460.00").compareTo(sessao.getValorApuradoFechamento()));
        assertEquals(0, new BigDecimal("-10.00").compareTo(sessao.getDiferencaFechamento()));
        assertEquals("Faltou troco", sessao.getObservacaoFechamento());
        assertNotNull(sessao.getFechadaEm());
    }

    @Test
    void naoDeveFecharSessaoJaFechada() {
        SessaoCaixa sessao = new SessaoCaixa(umUsuario(), new BigDecimal("100.00"));
        sessao.fechar(umUsuario(), new BigDecimal("100.00"), new BigDecimal("100.00"), null);

        assertThrows(
                TransicaoDeStatusInvalidaException.class,
                () -> sessao.fechar(umUsuario(), BigDecimal.ZERO, BigDecimal.ZERO, null));
    }

    @Test
    void deveCalcularSaldoEsperadoEmDinheiro() {
        SessaoCaixa sessao = new SessaoCaixa(umUsuario(), new BigDecimal("100.00"));

        BigDecimal saldo = sessao.calcularSaldoEsperadoEmDinheiro(new BigDecimal("300.00"), new BigDecimal("50.00"));

        assertEquals(0, new BigDecimal("350.00").compareTo(saldo));
    }

    private Usuario umUsuario() {
        return new Usuario("Caixa Teste", "caixa.teste." + Math.random(), "hash", PerfilUsuario.CAIXA);
    }
}
