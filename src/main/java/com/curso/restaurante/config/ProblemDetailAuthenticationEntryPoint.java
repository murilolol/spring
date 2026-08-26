package com.curso.restaurante.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

public class ProblemDetailAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public ProblemDetailAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException {
        ProblemDetail problema = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        problema.setTitle("Não autenticado");
        problema.setDetail("Credenciais ausentes ou inválidas");

        response.setStatus(problema.getStatus());
        response.setContentType("application/problem+json");
        response.getWriter().write(objectMapper.writeValueAsString(problema));
    }
}
