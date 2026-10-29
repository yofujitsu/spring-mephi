package com.yofujitsu.hotel_service.dto.hotel;

import com.yofujitsu.hotel_service.entity.Hotel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for {@link Hotel}
 */
public record HotelCreateDto(@NotNull @NotEmpty @NotBlank String name, @NotNull @NotEmpty @NotBlank String address) {
}