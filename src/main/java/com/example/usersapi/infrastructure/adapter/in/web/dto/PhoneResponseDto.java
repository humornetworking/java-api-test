package com.example.usersapi.infrastructure.adapter.in.web.dto;

public record PhoneResponseDto(
        String number,
        String citycode,
        String contrycode
) {}
