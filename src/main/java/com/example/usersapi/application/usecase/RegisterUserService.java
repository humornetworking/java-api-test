package com.example.usersapi.application.usecase;

import com.example.usersapi.domain.exception.EmailAlreadyRegisteredException;
import com.example.usersapi.domain.model.Phone;
import com.example.usersapi.domain.model.User;
import com.example.usersapi.domain.port.in.RegisterUserCommand;
import com.example.usersapi.domain.port.in.RegisterUserUseCase;
import com.example.usersapi.domain.port.out.PasswordEncoderPort;
import com.example.usersapi.domain.port.out.TokenGeneratorPort;
import com.example.usersapi.domain.port.out.UserRepositoryPort;
import com.example.usersapi.domain.service.UserDomainService;
import com.example.usersapi.domain.valueobject.Email;
import com.example.usersapi.domain.valueobject.Password;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RegisterUserService implements RegisterUserUseCase {

    private final UserRepositoryPort userRepository;
    private final TokenGeneratorPort tokenGenerator;
    private final PasswordEncoderPort passwordEncoder;
    private final UserDomainService userDomainService;

    @Override
    public User registerUser(RegisterUserCommand command) {
        // Los value objects validan el formato y lanzan excepciones de dominio
        Email email = new Email(command.email());
        new Password(command.password());

        if (userRepository.existsByEmail(email.value())) {
            throw new EmailAlreadyRegisteredException();
        }

        String token = tokenGenerator.generateToken(email.value());
        String encodedPassword = passwordEncoder.encode(command.password());

        List<Phone> phones = command.phones() == null
                ? Collections.emptyList()
                : command.phones().stream()
                        .map(p -> new Phone(p.number(), p.citycode(), p.contrycode()))
                        .toList();

        User user = userDomainService.createNewUser(command.name(), email, encodedPassword, phones, token);

        return userRepository.save(user);
    }
}
