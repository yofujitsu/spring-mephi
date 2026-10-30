package com.yofujitsu.hotel_service.dto.room;

import com.yofujitsu.hotel_service.entity.Room;
import jakarta.validation.constraints.Positive;

public record RoomUpdateDto(
        @Positive Integer number,
        Room.AvailableStatus available
) {
}
