package com.curso.restaurante.api.erro;

import com.curso.restaurante.domain.comum.ConflitoDeEstadoException;
import com.curso.restaurante.domain.comum.RecursoNaoEncontradoException;
import com.curso.restaurante.domain.comum.RegraDeNegocioException;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.ServletWebRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiExceptionHandlerTest {

    private final ApiExceptionHandler tratador = new ApiExceptionHandler();

    @Test
    void deveTratarRecursoNaoEncontradoComo404() {
        ProblemDetail problema = tratador.tratarRecursoNaoEncontrado(
                new RecursoNaoEncontradoException("Cliente não encontrado"));

        assertEquals(404, problema.getStatus());
        assertEquals("Recurso não encontrado", problema.getTitle());
        assertEquals("Cliente não encontrado", problema.getDetail());
    }

    @Test
    void deveTratarConflitoDeEstadoComo409() {
        ProblemDetail problema = tratador.tratarConflitoDeEstado(
                new ConflitoDeEstadoException("Já existe uma sessão de caixa aberta"));

        assertEquals(409, problema.getStatus());
        assertEquals("Conflito de estado", problema.getTitle());
    }

    @Test
    void deveTratarRegraDeNegocioComo422() {
        ProblemDetail problema = tratador.tratarRegraDeNegocio(
                new RegraDeNegocioException("Saldo em estoque insuficiente"));

        assertEquals(422, problema.getStatus());
        assertEquals("Regra de negócio violada", problema.getTitle());
    }

    @Test
    void deveTratarArgumentoInvalidoComo422() {
        ProblemDetail problema = tratador.tratarArgumentoInvalido(
                new IllegalArgumentException("Nome é obrigatório"));

        assertEquals(422, problema.getStatus());
        assertEquals("Dados inválidos", problema.getTitle());
        assertEquals("Nome é obrigatório", problema.getDetail());
    }

    @Test
    void deveTratarViolacaoDeIntegridadeComo409() {
        ProblemDetail problema = tratador.tratarViolacaoDeIntegridade(
                new DataIntegrityViolationException("constraint uk_item_cardapio_codigo"));

        assertEquals(409, problema.getStatus());
        assertEquals("Violação de integridade", problema.getTitle());
    }

    @Test
    void deveTratarAcessoNegadoComo403() {
        ProblemDetail problema = tratador.tratarAcessoNegado(
                new AccessDeniedException("Acesso restrito ao perfil ADMIN"));

        assertEquals(403, problema.getStatus());
        assertEquals("Acesso negado", problema.getTitle());
    }

    @Test
    void deveTratarErrosDeValidacaoComo400ComListaDeCampos() throws NoSuchMethodException {
        BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "categoriaCardapioRequest");
        bindingResult.addError(new FieldError("categoriaCardapioRequest", "nome", "não pode ficar em branco"));
        bindingResult.addError(new FieldError("categoriaCardapioRequest", "ordemExibicao", "deve ser positivo ou zero"));

        MethodParameter parametro = new MethodParameter(
                ApiExceptionHandlerTest.class.getDeclaredMethod("metodoFake", String.class), 0);
        MethodArgumentNotValidException excecao = new MethodArgumentNotValidException(parametro, bindingResult);

        ResponseEntity<Object> resposta = tratador.handleMethodArgumentNotValid(
                excecao,
                new HttpHeaders(),
                HttpStatus.BAD_REQUEST,
                new ServletWebRequest(new MockHttpServletRequest()));

        assertEquals(HttpStatus.BAD_REQUEST, resposta.getStatusCode());
        ProblemDetail problema = (ProblemDetail) resposta.getBody();
        assertEquals("Requisição inválida", problema.getTitle());

        @SuppressWarnings("unchecked")
        List<CampoInvalido> campos = (List<CampoInvalido>) problema.getProperties().get("campos");
        assertEquals(2, campos.size());
        assertTrue(campos.contains(new CampoInvalido("nome", "não pode ficar em branco")));
        assertTrue(campos.contains(new CampoInvalido("ordemExibicao", "deve ser positivo ou zero")));
    }

    @SuppressWarnings("unused")
    private void metodoFake(String parametro) {
    }
}
