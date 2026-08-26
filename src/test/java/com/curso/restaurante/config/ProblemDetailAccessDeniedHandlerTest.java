package com.curso.restaurante.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProblemDetailAccessDeniedHandlerTest {

    private final ProblemDetailAccessDeniedHandler handler =
            new ProblemDetailAccessDeniedHandler(new ObjectMapper());

    @Test
    void deveResponderComProblemDetail403() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.handle(
                new MockHttpServletRequest(),
                response,
                new AccessDeniedException("Acesso restrito ao perfil ADMIN"));

        assertEquals(403, response.getStatus());
        assertEquals("application/problem+json", response.getContentType());
        assertTrue(response.getContentAsString().contains("Acesso negado"));
    }
}
