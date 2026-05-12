package com.example.usersapi.application.mapper;

import com.example.usersapi.application.dto.request.RegisterUserRequestDto;
import com.example.usersapi.application.dto.response.PhoneResponseDto;
import com.example.usersapi.application.dto.response.RegisterUserResponseDto;
import com.example.usersapi.domain.model.User;
import com.example.usersapi.domain.port.in.PhoneData;
import com.example.usersapi.domain.port.in.RegisterUserCommand;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class UserMapper {

    public RegisterUserCommand toCommand(RegisterUserRequestDto dto) {
        List<PhoneData> phones = dto.phones() == null
                ? Collections.emptyList()
                : dto.phones().stream()
                        .map(p -> new PhoneData(p.number(), p.citycode(), p.contrycode()))
                        .toList();
        return new RegisterUserCommand(dto.name(), dto.email(), dto.password(), phones);
    }

    public RegisterUserResponseDto toResponse(User user) {
        List<PhoneResponseDto> phones = user.getPhones() == null
                ? Collections.emptyList()
                : user.getPhones().stream()
                        .map(p -> new PhoneResponseDto(p.number(), p.citycode(), p.contrycode()))
                        .toList();
        return RegisterUserResponseDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phones(phones)
                .created(user.getCreated())
                .modified(user.getModified())
                .lastLogin(user.getLastLogin())
                .token(user.getToken())
                .isActive(user.isActive())
                .build();
    }
}
