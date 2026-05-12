package com.example.usersapi.application.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record RegisterUserRequestDto(
        @NotBlank(message = "El nombre es requerido")
        String name,

        @NotBlank(message = "El correo es requerido")
        String email,

        @NotBlank(message = "La contraseña es requerida")
        String password,

        List<PhoneRequestDto> phones
) {}
