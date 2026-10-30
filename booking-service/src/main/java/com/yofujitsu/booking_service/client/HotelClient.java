package com.yofujitsu.booking_service.client;

import com.yofujitsu.booking_service.dto.hotel.ConfirmAvailabilityRequest;
import com.yofujitsu.booking_service.dto.hotel.HotelRoomDto;
import com.yofujitsu.booking_service.dto.hotel.ReleaseRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "hotel-service")
public interface HotelClient {

    @GetMapping("/api/rooms/recommend")
    List<HotelRoomDto> recommend();

    @GetMapping("/api/rooms/{id}")
    HotelRoomDto getRoom(@PathVariable("id") UUID id);

    @PostMapping("/api/rooms/{id}/confirm-availability")
    void confirmAvailability(
            @PathVariable("id") UUID id,
            @RequestBody ConfirmAvailabilityRequest request);

    @PostMapping("/api/rooms/{id}/release")
    void release(
            @PathVariable("id") UUID id,
            @RequestBody ReleaseRequest request);
}
