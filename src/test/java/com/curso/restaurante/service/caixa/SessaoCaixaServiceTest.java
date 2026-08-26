package com.curso.restaurante.service.caixa;

import com.curso.restaurante.domain.caixa.Sangria;
import com.curso.restaurante.domain.caixa.SessaoCaixa;
import com.curso.restaurante.domain.caixa.StatusSessaoCaixa;
import com.curso.restaurante.domain.comum.ConflitoDeEstadoException;
import com.curso.restaurante.domain.comum.RecursoNaoEncontradoException;
import com.curso.restaurante.domain.comum.RegraDeNegocioException;
import com.curso.restaurante.domain.usuario.PerfilUsuario;
import com.curso.restaurante.service.usuario.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SessaoCaixaServiceTest {

    @Autowired
    private SessaoCaixaService sessaoCaixaService;

    @Autowired
    private UsuarioService usuarioService;

    @Test
    void deveAbrirSessao() {
        var operador = usuarioService.criar(
                "Caixa Abrir Svc " + Math.random(), "caixa.abrir.svc." + Math.random(), "senha", PerfilUsuario.CAIXA);

        SessaoCaixa sessao = sessaoCaixaService.abrir(operador.getUsername(), new BigDecimal("100.00"));

        assertEquals(StatusSessaoCaixa.ABERTA, sessao.getStatus());
    }

    @Test
    void naoDeveAbrirSegundaSessaoEnquantoUmaEstaAberta() {
        var operador = usuarioService.criar(
                "Caixa Dupla Svc " + Math.random(), "caixa.dupla.svc." + Math.random(), "senha", PerfilUsuario.CAIXA);
        sessaoCaixaService.abrir(operador.getUsername(), BigDecimal.ZERO);

        assertThrows(ConflitoDeEstadoException.class, () -> sessaoCaixaService.abrir(operador.getUsername(), BigDecimal.ZERO));
    }

    @Test
    void buscarSessaoAbertaDeveLancarQuandoNaoHaNenhuma() {
        assertThrows(RecursoNaoEncontradoException.class, () -> sessaoCaixaService.buscarSessaoAberta());
    }

    @Test
    void deveRegistrarSangriaDentroDoSaldoDisponivel() {
        var operador = usuarioService.criar(
                "Caixa Sangria Svc " + Math.random(), "caixa.sangria.svc." + Math.random(), "senha", PerfilUsuario.CAIXA);
        SessaoCaixa sessao = sessaoCaixaService.abrir(operador.getUsername(), new BigDecimal("100.00"));

        Sangria sangria = sessaoCaixaService.registrarSangria(
                sessao.getId(), new BigDecimal("50.00"), "Reforço de troco", operador.getUsername());

        assertEquals(0, new BigDecimal("50.00").compareTo(sangria.getValor()));
    }

    @Test
    void deveRejeitarSangriaMaiorQueOSaldoDisponivel() {
        var operador = usuarioService.criar(
                "Caixa Sangria Excede Svc " + Math.random(), "caixa.sangria.excede.svc." + Math.random(), "senha", PerfilUsuario.CAIXA);
        SessaoCaixa sessao = sessaoCaixaService.abrir(operador.getUsername(), new BigDecimal("50.00"));

        assertThrows(
                RegraDeNegocioException.class,
                () -> sessaoCaixaService.registrarSangria(
                        sessao.getId(), new BigDecimal("100.00"), "motivo", operador.getUsername()));
    }

    @Test
    void deveFecharSessaoComConferenciaCalculada() {
        var operador = usuarioService.criar(
                "Caixa Fechar Svc " + Math.random(), "caixa.fechar.svc." + Math.random(), "senha", PerfilUsuario.CAIXA);
        SessaoCaixa sessao = sessaoCaixaService.abrir(operador.getUsername(), new BigDecimal("100.00"));
        sessaoCaixaService.registrarSangria(sessao.getId(), new BigDecimal("20.00"), "motivo", operador.getUsername());

        SessaoCaixa fechada = sessaoCaixaService.fechar(
                sessao.getId(), new BigDecimal("80.00"), "Conferência ok", operador.getUsername());

        assertEquals(StatusSessaoCaixa.FECHADA, fechada.getStatus());
        assertEquals(0, new BigDecimal("80.00").compareTo(fechada.getValorApuradoFechamento()));
        assertEquals(0, BigDecimal.ZERO.setScale(2).compareTo(fechada.getDiferencaFechamento()));
    }
}
