package com.curso.restaurante.service.caixa;

import com.curso.restaurante.domain.caixa.FormaPagamento;
import com.curso.restaurante.domain.caixa.Sangria;
import com.curso.restaurante.domain.caixa.SessaoCaixa;
import com.curso.restaurante.domain.caixa.StatusSessaoCaixa;
import com.curso.restaurante.domain.comum.ConflitoDeEstadoException;
import com.curso.restaurante.domain.comum.RecursoNaoEncontradoException;
import com.curso.restaurante.domain.comum.RegraDeNegocioException;
import com.curso.restaurante.domain.usuario.Usuario;
import com.curso.restaurante.repository.caixa.PagamentoRepository;
import com.curso.restaurante.repository.caixa.SangriaRepository;
import com.curso.restaurante.repository.caixa.SessaoCaixaRepository;
import com.curso.restaurante.repository.usuario.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class SessaoCaixaService {

    private final SessaoCaixaRepository sessaoCaixaRepository;
    private final SangriaRepository sangriaRepository;
    private final PagamentoRepository pagamentoRepository;
    private final UsuarioRepository usuarioRepository;

    public SessaoCaixaService(
            SessaoCaixaRepository sessaoCaixaRepository,
            SangriaRepository sangriaRepository,
            PagamentoRepository pagamentoRepository,
            UsuarioRepository usuarioRepository) {
        this.sessaoCaixaRepository = sessaoCaixaRepository;
        this.sangriaRepository = sangriaRepository;
        this.pagamentoRepository = pagamentoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public SessaoCaixa abrir(String usernameAbertura, BigDecimal valorAbertura) {
        if (sessaoCaixaRepository.findByStatus(StatusSessaoCaixa.ABERTA).isPresent()) {
            throw new ConflitoDeEstadoException("Já existe uma sessão de caixa aberta");
        }

        Usuario operador = buscarUsuario(usernameAbertura);
        return sessaoCaixaRepository.save(new SessaoCaixa(operador, valorAbertura));
    }

    public SessaoCaixa buscarPorId(Long id) {
        return sessaoCaixaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Sessão de caixa não encontrada"));
    }

    public SessaoCaixa buscarSessaoAberta() {
        return sessaoCaixaRepository.findByStatus(StatusSessaoCaixa.ABERTA)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Nenhuma sessão de caixa está aberta"));
    }

    public Page<SessaoCaixa> listar(StatusSessaoCaixa status, LocalDateTime de, LocalDateTime ate, Pageable pageable) {
        Specification<SessaoCaixa> especificacao = Specification.allOf();

        if (status != null) {
            especificacao = especificacao.and((raiz, consulta, cb) -> cb.equal(raiz.get("status"), status));
        }
        if (de != null) {
            especificacao = especificacao.and((raiz, consulta, cb) -> cb.greaterThanOrEqualTo(raiz.get("abertaEm"), de));
        }
        if (ate != null) {
            especificacao = especificacao.and((raiz, consulta, cb) -> cb.lessThanOrEqualTo(raiz.get("abertaEm"), ate));
        }

        return sessaoCaixaRepository.findAll(especificacao, pageable);
    }

    public Sangria registrarSangria(Long sessaoId, BigDecimal valor, String motivo, String username) {
        SessaoCaixa sessao = buscarPorId(sessaoId);
        Usuario operador = buscarUsuario(username);

        BigDecimal recebidoEmDinheiro = totalPagamentosEmDinheiro(sessaoId);
        BigDecimal totalSangrias = totalSangrias(sessaoId);
        BigDecimal saldoDisponivel = sessao.calcularSaldoEsperadoEmDinheiro(recebidoEmDinheiro, totalSangrias);

        if (valor.compareTo(saldoDisponivel) > 0) {
            throw new RegraDeNegocioException("Valor da sangria excede o saldo disponível em dinheiro");
        }

        return sangriaRepository.save(new Sangria(sessao, valor, motivo, operador));
    }

    public Page<Sangria> listarSangrias(Long sessaoId, Pageable pageable) {
        buscarPorId(sessaoId);
        return sangriaRepository.findBySessaoCaixaId(sessaoId, pageable);
    }

    public SessaoCaixa fechar(Long sessaoId, BigDecimal valorContado, String observacao, String username) {
        SessaoCaixa sessao = buscarPorId(sessaoId);
        Usuario operador = buscarUsuario(username);

        BigDecimal valorApurado = sessao.calcularSaldoEsperadoEmDinheiro(
                totalPagamentosEmDinheiro(sessaoId), totalSangrias(sessaoId));

        sessao.fechar(operador, valorContado, valorApurado, observacao);
        return sessao;
    }

    private BigDecimal totalPagamentosEmDinheiro(Long sessaoId) {
        return pagamentoRepository.findBySessaoCaixaId(sessaoId).stream()
                .filter(pagamento -> pagamento.getFormaPagamento() == FormaPagamento.DINHEIRO)
                .map(pagamento -> pagamento.getValor())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal totalSangrias(Long sessaoId) {
        return sangriaRepository.findBySessaoCaixaId(sessaoId).stream()
                .map(Sangria::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Usuario buscarUsuario(String username) {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));
    }
}
