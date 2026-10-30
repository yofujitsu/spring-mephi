package com.yofujitsu.hotel_service.controller;

import com.yofujitsu.hotel_service.dto.hotel.HotelCreateDto;
import com.yofujitsu.hotel_service.dto.hotel.HotelDto;
import com.yofujitsu.hotel_service.dto.hotel.HotelStatsDto;
import com.yofujitsu.hotel_service.dto.hotel.HotelUpdateDto;
import com.yofujitsu.hotel_service.service.HotelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/hotels")
@RequiredArgsConstructor
public class HotelController {

    private final HotelService hotelService;

    @GetMapping
    public List<HotelDto> getHotels(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String address,
            Sort sort) {
        return hotelService.getHotels(name, address, sort);
    }

    @GetMapping("/{id}")
    public HotelDto getHotel(@PathVariable UUID id) {
        return hotelService.getHotel(id);
    }

    @GetMapping("/{id}/stats")
    public HotelStatsDto getHotelStats(@PathVariable UUID id) {
        return hotelService.getStats(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HotelDto createHotel(@Valid @RequestBody HotelCreateDto hotelCreateDto) {
        return hotelService.createHotel(hotelCreateDto);
    }

    @PutMapping("/{id}")
    public HotelDto updateHotel(
            @PathVariable UUID id,
            @Valid @RequestBody HotelUpdateDto hotelUpdateDto) {
        return hotelService.updateHotel(id, hotelUpdateDto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteHotel(@PathVariable UUID id) {
        hotelService.deleteHotel(id);
    }
}
