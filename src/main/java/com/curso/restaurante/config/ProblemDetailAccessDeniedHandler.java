package com.curso.restaurante.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

public class ProblemDetailAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public ProblemDetailAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException {
        ProblemDetail problema = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
        problema.setTitle("Acesso negado");
        problema.setDetail("Você não tem permissão para executar esta operação");

        response.setStatus(problema.getStatus());
        response.setContentType("application/problem+json");
        response.getWriter().write(objectMapper.writeValueAsString(problema));
    }
}
