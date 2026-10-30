package com.yofujitsu.booking_service.dto.user;

import com.yofujitsu.booking_service.entity.Role;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UserUpdateDto(
        @NotNull UUID id,
        @Size(min = 3, max = 50) String username,
        @Size(min = 6, max = 100) String password,
        Role role
) {
}
