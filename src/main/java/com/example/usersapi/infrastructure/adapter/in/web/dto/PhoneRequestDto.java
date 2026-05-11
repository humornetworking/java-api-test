package com.example.usersapi.infrastructure.adapter.in.web.dto;

public record PhoneRequestDto(
        String number,
        String citycode,
        String contrycode
) {}
