package com.example.usersapi.domain.port.in;

import com.example.usersapi.domain.model.User;

public interface RegisterUserUseCase {
    User registerUser(RegisterUserCommand command);
}
