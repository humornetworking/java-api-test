package com.example.usersapi.application.service;

import com.example.usersapi.domain.exception.EmailAlreadyRegisteredException;
import com.example.usersapi.domain.exception.InvalidEmailFormatException;
import com.example.usersapi.domain.exception.InvalidPasswordFormatException;
import com.example.usersapi.domain.model.User;
import com.example.usersapi.domain.port.in.PhoneData;
import com.example.usersapi.domain.port.in.RegisterUserCommand;
import com.example.usersapi.domain.port.out.PasswordEncoderPort;
import com.example.usersapi.domain.port.out.TokenGeneratorPort;
import com.example.usersapi.domain.port.out.UserRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterUserApplicationServiceTest {

    @Mock
    private UserRepositoryPort userRepository;
    @Mock
    private TokenGeneratorPort tokenGenerator;
    @Mock
    private PasswordEncoderPort passwordEncoder;

    @InjectMocks
    private RegisterUserApplicationService service;

    private static final String VALID_EMAIL = "juan@rodriguez.org";
    private static final String VALID_PASSWORD = "Hunter12";
    private static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$";
    private static final String PASSWORD_REGEX = "^(?=(?:.*[0-9]){2})(?=.*[A-Z])(?=.*[a-z]).{6,}$";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "emailRegex", EMAIL_REGEX);
        ReflectionTestUtils.setField(service, "passwordRegex", PASSWORD_REGEX);
    }

    @Test
    void registerUser_success() {
        RegisterUserCommand command = new RegisterUserCommand(
                "Juan Rodriguez", VALID_EMAIL, VALID_PASSWORD,
                List.of(new PhoneData("1234567", "1", "57")));

        when(userRepository.existsByEmail(VALID_EMAIL)).thenReturn(false);
        when(tokenGenerator.generateToken(VALID_EMAIL)).thenReturn("jwt-token-value");
        when(passwordEncoder.encode(VALID_PASSWORD)).thenReturn("$2a$bcrypt");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = service.registerUser(command);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getEmail()).isEqualTo(VALID_EMAIL);
        assertThat(result.getToken()).isEqualTo("jwt-token-value");
        assertThat(result.isActive()).isTrue();
        assertThat(result.getCreated()).isNotNull();
        assertThat(result.getLastLogin()).isEqualTo(result.getCreated());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_throwsWhenEmailAlreadyRegistered() {
        RegisterUserCommand command = new RegisterUserCommand(
                "Juan", VALID_EMAIL, VALID_PASSWORD, List.of());

        when(userRepository.existsByEmail(VALID_EMAIL)).thenReturn(true);

        assertThatThrownBy(() -> service.registerUser(command))
                .isInstanceOf(EmailAlreadyRegisteredException.class)
                .hasMessage("El correo ya registrado");

        verify(userRepository, never()).save(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"juan@rodriguez", "juanrodriguez.com", "@rodriguez.com", "juan@.com", ""})
    void registerUser_throwsForInvalidEmail(String invalidEmail) {
        RegisterUserCommand command = new RegisterUserCommand("Juan", invalidEmail, VALID_PASSWORD, List.of());

        assertThatThrownBy(() -> service.registerUser(command))
                .isInstanceOf(InvalidEmailFormatException.class);

        verifyNoInteractions(userRepository);
    }

    @Test
    void registerUser_validEmailWithDotCl_passes() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(tokenGenerator.generateToken(anyString())).thenReturn("token");
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RegisterUserCommand command = new RegisterUserCommand(
                "Juan", "juan.perez@empresa.com.ar", VALID_PASSWORD, List.of());

        User result = service.registerUser(command);
        assertThat(result).isNotNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"hunter2", "HUNTER12", "Hunter1", "H1", "hunter"})
    void registerUser_throwsForInvalidPassword(String invalidPassword) {
        RegisterUserCommand command = new RegisterUserCommand("Juan", VALID_EMAIL, invalidPassword, List.of());

        assertThatThrownBy(() -> service.registerUser(command))
                .isInstanceOf(InvalidPasswordFormatException.class);
    }

    @Test
    void registerUser_withNullPhones_succeeds() {
        RegisterUserCommand command = new RegisterUserCommand("Juan", VALID_EMAIL, VALID_PASSWORD, null);

        when(userRepository.existsByEmail(VALID_EMAIL)).thenReturn(false);
        when(tokenGenerator.generateToken(any())).thenReturn("token");
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        User result = service.registerUser(command);
        assertThat(result.getPhones()).isEmpty();
    }

    @Test
    void registerUser_passwordIsEncoded() {
        RegisterUserCommand command = new RegisterUserCommand(
                "Juan", VALID_EMAIL, VALID_PASSWORD, List.of());

        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(tokenGenerator.generateToken(any())).thenReturn("token");
        when(passwordEncoder.encode(VALID_PASSWORD)).thenReturn("$2a$hashed");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        User result = service.registerUser(command);
        assertThat(result.getPassword()).isEqualTo("$2a$hashed");
        verify(passwordEncoder).encode(VALID_PASSWORD);
    }
}
