package com.yofujitsu.booking_service.client;

import com.yofujitsu.booking_service.dto.hotel.HotelRoomDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "hotel-service")
public interface HotelClient {

    @GetMapping("/api/rooms/recommend")
    List<HotelRoomDto> recommend();

    @GetMapping("/api/rooms/{id}")
    HotelRoomDto getRoom(@PathVariable("id") UUID id);
}
