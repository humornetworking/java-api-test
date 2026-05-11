package com.example.usersapi.domain.port.in;

import java.util.List;

public record RegisterUserCommand(
        String name,
        String email,
        String password,
        List<PhoneData> phones
) {}
