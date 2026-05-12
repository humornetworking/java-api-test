package com.example.usersapi.infrastructure.adapter.in.web;

import com.example.usersapi.application.dto.response.RegisterUserResponseDto;
import com.example.usersapi.application.mapper.UserMapper;
import com.example.usersapi.application.validator.UserValidator;
import com.example.usersapi.domain.exception.EmailAlreadyRegisteredException;
import com.example.usersapi.domain.exception.InvalidEmailFormatException;
import com.example.usersapi.domain.exception.InvalidPasswordFormatException;
import com.example.usersapi.domain.model.Phone;
import com.example.usersapi.domain.model.User;
import com.example.usersapi.domain.port.in.RegisterUserUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import({UserMapper.class, UserValidator.class})
class UserControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private RegisterUserUseCase registerUserUseCase;

    private static final String API_URL = "/api/users";

    private User buildSampleUser() {
        return User.builder()
                .id(UUID.randomUUID())
                .name("Juan Rodriguez")
                .email("juan@rodriguez.org")
                .password("$2a$encoded")
                .phones(List.of(new Phone("1234567", "1", "57")))
                .created(LocalDateTime.now())
                .modified(LocalDateTime.now())
                .lastLogin(LocalDateTime.now())
                .token("jwt-token-value")
                .isActive(true)
                .build();
    }

    @Test
    @WithMockUser
    void registerUser_returns201_whenSuccess() throws Exception {
        when(registerUserUseCase.registerUser(any())).thenReturn(buildSampleUser());

        mockMvc.perform(post(API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Juan Rodriguez",
                                  "email": "juan@rodriguez.org",
                                  "password": "Hunter12",
                                  "phones": [{ "number": "1234567", "citycode": "1", "contrycode": "57" }]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.token").value("jwt-token-value"))
                .andExpect(jsonPath("$.isactive").value(true))
                .andExpect(jsonPath("$.last_login").isNotEmpty())
                .andExpect(jsonPath("$.phones", hasSize(1)));
    }

    @Test
    @WithMockUser
    void registerUser_returns409_whenEmailDuplicated() throws Exception {
        when(registerUserUseCase.registerUser(any())).thenThrow(new EmailAlreadyRegisteredException());

        mockMvc.perform(post(API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Juan","email":"juan@rodriguez.org","password":"Hunter12"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("El correo ya registrado"));
    }

    @Test
    @WithMockUser
    void registerUser_returns400_whenEmailInvalid() throws Exception {
        when(registerUserUseCase.registerUser(any())).thenThrow(new InvalidEmailFormatException());

        mockMvc.perform(post(API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Juan","email":"bad-email","password":"Hunter12"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").exists());
    }

    @Test
    @WithMockUser
    void registerUser_returns400_whenPasswordInvalid() throws Exception {
        when(registerUserUseCase.registerUser(any())).thenThrow(new InvalidPasswordFormatException());

        mockMvc.perform(post(API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Juan","email":"juan@rodriguez.org","password":"weak"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").exists());
    }

    @Test
    @WithMockUser
    void registerUser_returns400_whenNameBlank() throws Exception {
        mockMvc.perform(post(API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"","email":"juan@rodriguez.org","password":"Hunter12"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").exists());
    }

    @Test
    @WithMockUser
    void registerUser_returns400_whenBodyNotJson() throws Exception {
        mockMvc.perform(post(API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("not-valid-json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").exists());
    }
}
