package com.yofujitsu.hotel_service.service;

import com.yofujitsu.hotel_service.dto.hotel.HotelCreateDto;
import com.yofujitsu.hotel_service.dto.hotel.HotelDto;
import com.yofujitsu.hotel_service.mapper.HotelMapper;
import com.yofujitsu.hotel_service.repository.HotelRepository;
import com.yofujitsu.hotel_service.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class HotelService {

    private final HotelRepository hotelRepository;
    private final HotelMapper hotelMapper;

    public void createHotel(HotelCreateDto hotelCreateDto) {
        var hotel = hotelMapper.toEntity(hotelCreateDto);
        hotelRepository.save(hotel);
        log.debug("created new hotel: {}", hotel);
    }

    public List<HotelDto> getHotels() {
        return hotelRepository.findAll().stream().map(hotelMapper::toDto).toList();
    }
}
