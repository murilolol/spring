package com.curso.restaurante.service.caixa;

import com.curso.restaurante.domain.caixa.FormaPagamento;
import com.curso.restaurante.domain.caixa.Pagamento;
import com.curso.restaurante.domain.caixa.SessaoCaixa;
import com.curso.restaurante.domain.comanda.Comanda;
import com.curso.restaurante.domain.comum.ConflitoDeEstadoException;
import com.curso.restaurante.domain.comum.RecursoNaoEncontradoException;
import com.curso.restaurante.domain.comum.RegraDeNegocioException;
import com.curso.restaurante.domain.usuario.Usuario;
import com.curso.restaurante.repository.caixa.PagamentoRepository;
import com.curso.restaurante.repository.caixa.SessaoCaixaRepository;
import com.curso.restaurante.repository.comanda.ComandaRepository;
import com.curso.restaurante.repository.usuario.UsuarioRepository;
import com.curso.restaurante.domain.caixa.StatusSessaoCaixa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class PagamentoService {

    private final PagamentoRepository pagamentoRepository;
    private final ComandaRepository comandaRepository;
    private final SessaoCaixaRepository sessaoCaixaRepository;
    private final UsuarioRepository usuarioRepository;

    public PagamentoService(
            PagamentoRepository pagamentoRepository,
            ComandaRepository comandaRepository,
            SessaoCaixaRepository sessaoCaixaRepository,
            UsuarioRepository usuarioRepository) {
        this.pagamentoRepository = pagamentoRepository;
        this.comandaRepository = comandaRepository;
        this.sessaoCaixaRepository = sessaoCaixaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public Pagamento registrarPagamento(
            Long comandaId, FormaPagamento forma, BigDecimal valor, BigDecimal valorRecebido, String username) {
        Comanda comanda = comandaRepository.findById(comandaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Comanda não encontrada"));
        SessaoCaixa sessao = sessaoCaixaRepository.findByStatus(StatusSessaoCaixa.ABERTA)
                .orElseThrow(() -> new ConflitoDeEstadoException("Nenhuma sessão de caixa está aberta"));
        Usuario operador = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));

        BigDecimal totalJaPago = listarPagamentosDaComanda(comandaId).stream()
                .map(Pagamento::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal saldoDevedor = comanda.calcularSaldoDevedor(totalJaPago);

        if (valor.compareTo(saldoDevedor) > 0) {
            throw new RegraDeNegocioException("Valor do pagamento excede o saldo devedor da comanda");
        }

        Pagamento pagamento = pagamentoRepository.save(
                new Pagamento(comanda, sessao, forma, valor, valorRecebido, operador));

        BigDecimal totalPagoComEsse = totalJaPago.add(valor);
        if (totalPagoComEsse.compareTo(comanda.calcularTotal()) >= 0) {
            comanda.marcarComoPaga();
        }

        return pagamento;
    }

    public List<Pagamento> listarPagamentosDaComanda(Long comandaId) {
        return pagamentoRepository.findByComandaId(comandaId);
    }

    public Page<Pagamento> listarPagamentosPorSessao(Long sessaoId, FormaPagamento forma, Pageable pageable) {
        if (forma == null) {
            return pagamentoRepository.findBySessaoCaixaId(sessaoId, pageable);
        }
        return pagamentoRepository.findBySessaoCaixaIdAndFormaPagamento(sessaoId, forma, pageable);
    }
}
