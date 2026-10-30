package com.yofujitsu.hotel_service.dto.room;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record ConfirmAvailabilityRequest(
        @NotNull UUID bookingId,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        @NotNull UUID requestId
) {
}
