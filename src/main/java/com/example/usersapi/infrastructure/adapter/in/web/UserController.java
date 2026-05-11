package com.example.usersapi.infrastructure.adapter.in.web;

import com.example.usersapi.domain.model.User;
import com.example.usersapi.domain.port.in.RegisterUserUseCase;
import com.example.usersapi.infrastructure.adapter.in.web.dto.RegisterUserRequestDto;
import com.example.usersapi.infrastructure.adapter.in.web.dto.RegisterUserResponseDto;
import com.example.usersapi.infrastructure.adapter.in.web.mapper.UserWebMapper;
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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/users", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Users", description = "Gestión de usuarios")
public class UserController {

    private final RegisterUserUseCase registerUserUseCase;
    private final UserWebMapper mapper;

    @Operation(summary = "Registrar nuevo usuario", description = "Crea un usuario y retorna sus datos junto con el token JWT")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Error de validación", content = @Content(schema = @Schema(implementation = com.example.usersapi.infrastructure.adapter.in.web.dto.ErrorResponseDto.class))),
            @ApiResponse(responseCode = "409", description = "Correo ya registrado", content = @Content(schema = @Schema(implementation = com.example.usersapi.infrastructure.adapter.in.web.dto.ErrorResponseDto.class)))
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RegisterUserResponseDto> registerUser(@Valid @RequestBody RegisterUserRequestDto request) {
        User user = registerUserUseCase.registerUser(mapper.toCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(user));
    }
}
