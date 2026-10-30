package com.yofujitsu.hotel_service.dto.hotel;

import com.yofujitsu.hotel_service.entity.Hotel;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO for {@link Hotel}
 */
public record HotelCreateDto(
        @NotBlank String name,
        @NotBlank String address
) {
}
