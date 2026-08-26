package com.curso.restaurante.api.comum;

import jakarta.validation.constraints.NotBlank;

public record CancelarRequest(@NotBlank(message = "não pode ficar em branco") String motivo) {
}
