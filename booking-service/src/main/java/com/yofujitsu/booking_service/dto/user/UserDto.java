package com.yofujitsu.booking_service.dto.user;

import com.yofujitsu.booking_service.entity.Role;

import java.util.UUID;

public record UserDto(
        UUID id,
        String username,
        Role role
) {
}
