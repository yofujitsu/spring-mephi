package com.yofujitsu.booking_service.dto.user;

import com.yofujitsu.booking_service.entity.Role;

public record TokenResponse(
        String token,
        String tokenType,
        long expiresInSeconds,
        String username,
        Role role
) {
}
