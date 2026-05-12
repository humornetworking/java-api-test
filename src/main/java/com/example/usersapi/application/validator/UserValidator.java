package com.example.usersapi.application.validator;

import com.example.usersapi.application.dto.request.RegisterUserRequestDto;
import com.example.usersapi.domain.exception.BusinessValidationException;
import org.springframework.stereotype.Component;

@Component
public class UserValidator {

    private static final int MAX_NAME_LENGTH = 100;
    private static final int MAX_PHONES = 10;

    public void validate(RegisterUserRequestDto request) {
        if (request.name() != null && request.name().length() > MAX_NAME_LENGTH) {
            throw new BusinessValidationException(
                    "El nombre no puede superar los " + MAX_NAME_LENGTH + " caracteres");
        }
        if (request.phones() != null && request.phones().size() > MAX_PHONES) {
            throw new BusinessValidationException(
                    "No se pueden registrar más de " + MAX_PHONES + " teléfonos");
        }
    }
}
