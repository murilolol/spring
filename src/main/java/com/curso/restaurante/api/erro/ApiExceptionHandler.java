package com.curso.restaurante.api.erro;

import com.curso.restaurante.domain.comum.ConflitoDeEstadoException;
import com.curso.restaurante.domain.comum.RecursoNaoEncontradoException;
import com.curso.restaurante.domain.comum.RegraDeNegocioException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;

@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ProblemDetail tratarRecursoNaoEncontrado(RecursoNaoEncontradoException excecao) {
        return construir(HttpStatus.NOT_FOUND, "Recurso não encontrado", excecao.getMessage());
    }

    @ExceptionHandler(ConflitoDeEstadoException.class)
    public ProblemDetail tratarConflitoDeEstado(ConflitoDeEstadoException excecao) {
        return construir(HttpStatus.CONFLICT, "Conflito de estado", excecao.getMessage());
    }

    @ExceptionHandler(RegraDeNegocioException.class)
    public ProblemDetail tratarRegraDeNegocio(RegraDeNegocioException excecao) {
        return construir(HttpStatus.UNPROCESSABLE_ENTITY, "Regra de negócio violada", excecao.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail tratarArgumentoInvalido(IllegalArgumentException excecao) {
        return construir(HttpStatus.UNPROCESSABLE_ENTITY, "Dados inválidos", excecao.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail tratarViolacaoDeIntegridade(DataIntegrityViolationException excecao) {
        return construir(
                HttpStatus.CONFLICT,
                "Violação de integridade",
                "A operação viola uma restrição do banco de dados");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail tratarAcessoNegado(AccessDeniedException excecao) {
        return construir(HttpStatus.FORBIDDEN, "Acesso negado", excecao.getMessage());
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException excecao,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        List<CampoInvalido> campos = excecao.getBindingResult().getFieldErrors().stream()
                .map(erro -> new CampoInvalido(erro.getField(), erro.getDefaultMessage()))
                .toList();

        ProblemDetail problema = construir(HttpStatus.BAD_REQUEST, "Requisição inválida", "Um ou mais campos são inválidos");
        problema.setProperty("campos", campos);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problema);
    }

    private ProblemDetail construir(HttpStatus status, String titulo, String detalhe) {
        ProblemDetail problema = ProblemDetail.forStatus(status);
        problema.setTitle(titulo);
        problema.setDetail(detalhe);
        return problema;
    }
}
