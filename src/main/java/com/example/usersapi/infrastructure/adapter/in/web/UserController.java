package com.example.usersapi.infrastructure.adapter.in.web;

import com.example.usersapi.application.dto.request.RegisterUserRequestDto;
import com.example.usersapi.application.dto.response.ErrorResponseDto;
import com.example.usersapi.application.dto.response.RegisterUserResponseDto;
import com.example.usersapi.application.mapper.UserMapper;
import com.example.usersapi.application.validator.UserValidator;
import com.example.usersapi.domain.model.User;
import com.example.usersapi.domain.port.in.RegisterUserUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/users", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Users", description = "Gestión de usuarios")
public class UserController {

    private final RegisterUserUseCase registerUserUseCase;
    private final UserMapper userMapper;
    private final UserValidator userValidator;

    @Operation(summary = "Registrar nuevo usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Error de validación",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "409", description = "Correo ya registrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RegisterUserResponseDto> registerUser(
            @Valid @RequestBody RegisterUserRequestDto request) {

        userValidator.validate(request);

        User user = registerUserUseCase.registerUser(userMapper.toCommand(request));

        return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toResponse(user));
    }
}
