package com.example.usersapi.domain.exception;

public class EmailAlreadyRegisteredException extends RuntimeException {
    public EmailAlreadyRegisteredException() {
        super("El correo ya registrado");
    }
}
