package com.example.usersapi.application.dto.response;

public record PhoneResponseDto(
        String number,
        String citycode,
        String contrycode
) {}
