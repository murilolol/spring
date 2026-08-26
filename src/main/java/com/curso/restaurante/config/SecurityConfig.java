package com.curso.restaurante.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import tools.jackson.databind.ObjectMapper;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, ObjectMapper objectMapper) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(basic -> basic.authenticationEntryPoint(new ProblemDetailAuthenticationEntryPoint(objectMapper)))
                .exceptionHandling(excecoes -> excecoes
                        .authenticationEntryPoint(new ProblemDetailAuthenticationEntryPoint(objectMapper))
                        .accessDeniedHandler(new ProblemDetailAccessDeniedHandler(objectMapper)))
                .authorizeHttpRequests(requisicoes -> requisicoes
                        .requestMatchers("/api/health").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/categorias-cardapio/**", "/api/itens-cardapio/**")
                                .authenticated()
                        .requestMatchers("/api/categorias-cardapio/**", "/api/itens-cardapio/**").hasRole("ADMIN")
                        .requestMatchers("/api/usuarios/*/senha").authenticated()
                        .requestMatchers("/api/usuarios/**").hasRole("ADMIN")
                        .requestMatchers("/api/clientes/*/ativar", "/api/clientes/*/inativar").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/clientes").hasAnyRole("GARCOM", "CAIXA", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/clientes/*").hasAnyRole("GARCOM", "CAIXA", "ADMIN")
                        .requestMatchers("/api/clientes/**").authenticated()
                        .requestMatchers(
                                "/api/mesas/*/interditar",
                                "/api/mesas/*/liberar-interdicao").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/mesas").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/mesas/*").hasRole("ADMIN")
                        .requestMatchers(
                                "/api/mesas/*/reservar",
                                "/api/mesas/*/cancelar-reserva").hasAnyRole("GARCOM", "ADMIN")
                        .requestMatchers("/api/mesas/**").authenticated()
                        .requestMatchers("/api/comandas/*/cancelar").hasRole("ADMIN")
                        .requestMatchers("/api/comandas/*/reabrir").hasAnyRole("CAIXA", "ADMIN")
                        .requestMatchers(
                                "/api/comandas/*/transferir-mesa").hasAnyRole("GARCOM", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/comandas/*/pedidos").hasAnyRole("GARCOM", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/comandas", "/api/comandas/*/fechar")
                                .hasAnyRole("GARCOM", "CAIXA", "ADMIN")
                        .requestMatchers("/api/comandas/*/pagamentos").hasAnyRole("CAIXA", "ADMIN")
                        .requestMatchers("/api/comandas/**").authenticated()
                        .requestMatchers("/api/pedidos/*/marcar-pronto").hasAnyRole("COZINHA", "ADMIN")
                        .requestMatchers("/api/pedidos/*/cancelar").hasAnyRole("GARCOM", "CAIXA", "ADMIN")
                        .requestMatchers(
                                "/api/pedidos/*/itens",
                                "/api/pedidos/*/itens/*",
                                "/api/pedidos/*/enviar-para-preparo",
                                "/api/pedidos/*/marcar-entregue").hasAnyRole("GARCOM", "ADMIN")
                        .requestMatchers("/api/pedidos/**").authenticated()
                        .requestMatchers("/api/cozinha/**").hasAnyRole("COZINHA", "ADMIN")
                        .requestMatchers("/api/caixa/**").hasAnyRole("CAIXA", "ADMIN")
                        .anyRequest().authenticated());

        return http.build();
    }
}
