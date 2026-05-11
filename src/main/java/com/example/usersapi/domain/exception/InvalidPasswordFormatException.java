package com.example.usersapi.domain.exception;

public class InvalidPasswordFormatException extends RuntimeException {
    public InvalidPasswordFormatException() {
        super("La contraseña no tiene un formato válido. Debe contener al menos una mayúscula, letras minúsculas y dos dígitos");
    }
}
