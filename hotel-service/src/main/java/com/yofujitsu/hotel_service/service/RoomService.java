package com.yofujitsu.hotel_service.service;

import com.yofujitsu.hotel_service.dto.room.RoomCreateDto;
import com.yofujitsu.hotel_service.dto.room.RoomDto;
import com.yofujitsu.hotel_service.dto.room.RoomStatsDto;
import com.yofujitsu.hotel_service.dto.room.RoomUpdateDto;
import com.yofujitsu.hotel_service.entity.Room;
import com.yofujitsu.hotel_service.exception.ConflictException;
import com.yofujitsu.hotel_service.exception.ResourceNotFoundException;
import com.yofujitsu.hotel_service.mapper.RoomMapper;
import com.yofujitsu.hotel_service.repository.HotelRepository;
import com.yofujitsu.hotel_service.repository.RoomLockRepository;
import com.yofujitsu.hotel_service.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomLockRepository roomLockRepository;
    private final HotelRepository hotelRepository;
    private final RoomMapper roomMapper;

    @Transactional
    public RoomDto createRoom(RoomCreateDto roomCreateDto) {
        if (!hotelRepository.existsById(roomCreateDto.hotelId())) {
            throw new ResourceNotFoundException("Hotel not found: " + roomCreateDto.hotelId());
        }
        if (roomRepository.existsByHotelIdAndNumber(
                roomCreateDto.hotelId(), roomCreateDto.number())) {
            throw new ConflictException("Room number %d already exists in hotel %s"
                    .formatted(roomCreateDto.number(), roomCreateDto.hotelId()));
        }

        Room room = roomMapper.toEntity(roomCreateDto);
        if (room.getAvailable() == null) {
            room.setAvailable(Room.AvailableStatus.AVAILABLE);
        }
        room.setTimesBooked(0);
        roomRepository.save(room);
        log.info("Created room {} in hotel {}", room.getId(), room.getHotelId());

        return roomMapper.toDto(room);
    }

    @Transactional(readOnly = true)
    public List<RoomDto> getAvailableRooms(UUID hotelId, Sort sort) {
        Sort effectiveSort = sort == null ? Sort.unsorted() : sort;
        List<Room> rooms = hotelId == null
                ? roomRepository.findAllByAvailable(Room.AvailableStatus.AVAILABLE, effectiveSort)
                : roomRepository.findAllByAvailableAndHotelId(
                        Room.AvailableStatus.AVAILABLE, hotelId, effectiveSort);

        return rooms.stream().map(roomMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<RoomDto> getRecommendedRooms() {
        return roomRepository
                .findAllByAvailableOrderByTimesBookedAscIdAsc(Room.AvailableStatus.AVAILABLE)
                .stream()
                .map(roomMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public RoomDto getRoom(UUID id) {
        return roomMapper.toDto(getRoomOrThrow(id));
    }

    @Transactional
    public RoomDto updateRoom(UUID id, RoomUpdateDto roomUpdateDto) {
        Room room = getRoomOrThrow(id);

        if (roomUpdateDto.number() != null
                && !roomUpdateDto.number().equals(room.getNumber())) {
            if (roomRepository.existsByHotelIdAndNumber(
                    room.getHotelId(), roomUpdateDto.number())) {
                throw new ConflictException("Room number %d already exists in hotel %s"
                        .formatted(roomUpdateDto.number(), room.getHotelId()));
            }
            room.setNumber(roomUpdateDto.number());
        }
        if (roomUpdateDto.available() != null) {
            room.setAvailable(roomUpdateDto.available());
        }

        log.info("Updated room {}", id);
        return roomMapper.toDto(room);
    }

    @Transactional
    public void deleteRoom(UUID id) {
        Room room = getRoomOrThrow(id);
        roomLockRepository.deleteByRoomId(id);
        roomRepository.delete(room);
        log.info("Deleted room {}", id);
    }

    @Transactional(readOnly = true)
    public List<RoomStatsDto> getStats(Sort sort) {
        Sort effectiveSort = sort == null ? Sort.unsorted() : sort;
        return roomRepository.findAll(effectiveSort).stream()
                .map(room -> new RoomStatsDto(
                        room.getId(),
                        room.getHotelId(),
                        room.getNumber(),
                        room.getAvailable(),
                        room.getTimesBooked()))
                .toList();
    }

    private Room getRoomOrThrow(UUID id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found: " + id));
    }
}
