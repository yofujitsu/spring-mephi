package com.yofujitsu.booking_service.exception;

import java.time.Instant;

public record ErrorDto(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        String requestId
) {
}
