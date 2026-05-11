package com.example.usersapi.application.service;

import com.example.usersapi.domain.exception.EmailAlreadyRegisteredException;
import com.example.usersapi.domain.exception.InvalidEmailFormatException;
import com.example.usersapi.domain.exception.InvalidPasswordFormatException;
import com.example.usersapi.domain.model.Phone;
import com.example.usersapi.domain.model.User;
import com.example.usersapi.domain.port.in.RegisterUserCommand;
import com.example.usersapi.domain.port.in.RegisterUserUseCase;
import com.example.usersapi.domain.port.out.PasswordEncoderPort;
import com.example.usersapi.domain.port.out.TokenGeneratorPort;
import com.example.usersapi.domain.port.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class RegisterUserApplicationService implements RegisterUserUseCase {

    private final UserRepositoryPort userRepository;
    private final TokenGeneratorPort tokenGenerator;
    private final PasswordEncoderPort passwordEncoder;

    @Value("${app.validation.email-regex}")
    private String emailRegex;

    @Value("${app.validation.password-regex}")
    private String passwordRegex;

    @Override
    public User registerUser(RegisterUserCommand command) {
        validateEmail(command.email());
        validatePassword(command.password());

        if (userRepository.existsByEmail(command.email())) {
            throw new EmailAlreadyRegisteredException();
        }

        LocalDateTime now = LocalDateTime.now();
        String token = tokenGenerator.generateToken(command.email());
        String encodedPassword = passwordEncoder.encode(command.password());

        List<Phone> phones = command.phones() == null
                ? Collections.emptyList()
                : command.phones().stream()
                        .map(p -> new Phone(p.number(), p.citycode(), p.contrycode()))
                        .toList();

        User user = User.builder()
                .id(UUID.randomUUID())
                .name(command.name())
                .email(command.email())
                .password(encodedPassword)
                .phones(phones)
                .created(now)
                .modified(now)
                .lastLogin(now)
                .token(token)
                .isActive(true)
                .build();

        return userRepository.save(user);
    }

    private void validateEmail(String email) {
        if (email == null || !Pattern.matches(emailRegex, email)) {
            throw new InvalidEmailFormatException();
        }
    }

    private void validatePassword(String password) {
        if (password == null || !Pattern.matches(passwordRegex, password)) {
            throw new InvalidPasswordFormatException();
        }
    }
}
