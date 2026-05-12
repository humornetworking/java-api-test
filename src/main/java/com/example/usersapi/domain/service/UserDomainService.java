package com.example.usersapi.domain.service;

import com.example.usersapi.domain.model.Phone;
import com.example.usersapi.domain.model.User;
import com.example.usersapi.domain.valueobject.Email;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Servicio de dominio puro: sin dependencias de Spring, JPA ni ningún framework.
 * Centraliza la creación de User garantizando todos los invariantes del dominio.
 */
public class UserDomainService {

    public User createNewUser(String name, Email email, String encodedPassword,
                              List<Phone> phones, String token) {
        LocalDateTime now = LocalDateTime.now();
        return User.builder()
                .id(UUID.randomUUID())
                .name(name)
                .email(email.value())
                .password(encodedPassword)
                .phones(phones != null ? phones : List.of())
                .created(now)
                .modified(now)
                .lastLogin(now)
                .token(token)
                .isActive(true)
                .build();
    }
}
