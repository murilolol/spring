package com.curso.restaurante.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProblemDetailAuthenticationEntryPointTest {

    private final ProblemDetailAuthenticationEntryPoint entryPoint =
            new ProblemDetailAuthenticationEntryPoint(new ObjectMapper());

    @Test
    void deveResponderComProblemDetail401() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(
                new MockHttpServletRequest(),
                response,
                new BadCredentialsException("Credenciais inválidas"));

        assertEquals(401, response.getStatus());
        assertEquals("application/problem+json", response.getContentType());
        assertTrue(response.getContentAsString().contains("Não autenticado"));
    }
}
