package com.example.usersapi.domain.valueobject;

import com.example.usersapi.domain.exception.InvalidEmailFormatException;

import java.util.Locale;
import java.util.Objects;

public record Email(String value) {

    private static final String REGEX = "^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$";

    public Email {
        Objects.requireNonNull(value, "Email no puede ser nulo");
        if (!value.matches(REGEX)) {
            throw new InvalidEmailFormatException();
        }
        value = value.toLowerCase(Locale.ROOT);
    }

    public static boolean isValid(String value) {
        return value != null && value.matches(REGEX);
    }
}
