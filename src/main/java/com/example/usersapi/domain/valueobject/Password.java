package com.example.usersapi.domain.valueobject;

import com.example.usersapi.domain.exception.InvalidPasswordFormatException;

import java.util.Objects;

public record Password(String value) {

    // Al menos 1 mayúscula, al menos 1 minúscula, al menos 2 dígitos, mínimo 6 caracteres
    private static final String REGEX = "^(?=(?:.*[0-9]){2})(?=.*[A-Z])(?=.*[a-z]).{6,}$";

    public Password {
        Objects.requireNonNull(value, "Contraseña no puede ser nula");
        if (!value.matches(REGEX)) {
            throw new InvalidPasswordFormatException();
        }
    }
}
