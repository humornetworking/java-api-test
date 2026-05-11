package com.example.usersapi.domain.exception;

public class InvalidEmailFormatException extends RuntimeException {
    public InvalidEmailFormatException() {
        super("El correo no tiene un formato válido");
    }
}
