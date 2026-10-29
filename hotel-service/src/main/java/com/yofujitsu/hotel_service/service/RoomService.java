package com.yofujitsu.hotel_service.service;

import com.yofujitsu.hotel_service.dto.room.RoomCreateDto;
import com.yofujitsu.hotel_service.dto.room.RoomDto;
import com.yofujitsu.hotel_service.entity.Room;
import com.yofujitsu.hotel_service.mapper.RoomMapper;
import com.yofujitsu.hotel_service.repository.HotelRepository;
import com.yofujitsu.hotel_service.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomMapper roomMapper;

    public void addRoomToHotel(RoomCreateDto roomCreateDto) {
        var room = roomMapper.toEntity(roomCreateDto);
        roomRepository.save(room);
        log.debug("created new room: {}", room);
    }

    public List<RoomDto> getAllRoomsByHotel(UUID hotelId) {
        return roomRepository.findAllByHotelId(hotelId).stream()
                .map(roomMapper::toDto).toList();
    }

    public List<RoomDto> getAllAvailableRooms() {
        return roomRepository.findAllByAvailable(Room.AvailableStatus.AVAILABLE)
                .stream().map(roomMapper::toDto).toList();
    }
}
