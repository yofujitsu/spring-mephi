package com.yofujitsu.hotel_service.dto.room;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ReleaseRequest(
        @NotNull UUID bookingId,
        @NotNull UUID requestId
) {
}
