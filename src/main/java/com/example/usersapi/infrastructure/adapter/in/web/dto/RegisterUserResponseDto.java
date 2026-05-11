package com.example.usersapi.infrastructure.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class RegisterUserResponseDto {
    private UUID id;
    private String name;
    private String email;
    private List<PhoneResponseDto> phones;
    private LocalDateTime created;
    private LocalDateTime modified;

    @JsonProperty("last_login")
    private LocalDateTime lastLogin;

    private String token;

    @JsonProperty("isactive")
    private boolean isActive;
}
