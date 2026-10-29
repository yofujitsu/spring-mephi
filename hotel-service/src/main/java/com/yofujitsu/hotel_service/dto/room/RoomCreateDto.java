package com.yofujitsu.hotel_service.dto.room;

import com.yofujitsu.hotel_service.entity.Room;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

/**
 * DTO for {@link Room}
 */
public record RoomCreateDto(@NotNull UUID hotelId, Room.AvailableStatus available) {
}