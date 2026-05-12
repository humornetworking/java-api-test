package com.example.usersapi.application.dto.request;

public record PhoneRequestDto(
        String number,
        String citycode,
        String contrycode
) {}
