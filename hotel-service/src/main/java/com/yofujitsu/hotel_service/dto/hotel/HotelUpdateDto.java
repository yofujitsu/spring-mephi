package com.yofujitsu.hotel_service.dto.hotel;

import jakarta.validation.constraints.NotBlank;

public record HotelUpdateDto(
        @NotBlank String name,
        @NotBlank String address
) {
}
