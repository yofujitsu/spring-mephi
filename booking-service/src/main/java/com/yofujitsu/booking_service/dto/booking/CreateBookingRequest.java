package com.yofujitsu.booking_service.dto.booking;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record CreateBookingRequest(
        UUID hotelId,
        UUID roomId,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        boolean autoSelect,
        @NotNull UUID requestId
) {
}
