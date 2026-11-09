package com.yofujitsu.hotel_service.service;

import com.yofujitsu.hotel_service.dto.hotel.HotelCreateDto;
import com.yofujitsu.hotel_service.dto.hotel.HotelDto;
import com.yofujitsu.hotel_service.dto.hotel.HotelStatsDto;
import com.yofujitsu.hotel_service.dto.hotel.HotelUpdateDto;
import com.yofujitsu.hotel_service.dto.room.RoomDto;
import com.yofujitsu.hotel_service.entity.Hotel;
import com.yofujitsu.hotel_service.entity.Room;
import com.yofujitsu.hotel_service.exception.ResourceNotFoundException;
import com.yofujitsu.hotel_service.exception.ConflictException;
import com.yofujitsu.hotel_service.mapper.HotelMapper;
import com.yofujitsu.hotel_service.mapper.RoomMapper;
import com.yofujitsu.hotel_service.repository.HotelRepository;
import com.yofujitsu.hotel_service.repository.RoomLockRepository;
import com.yofujitsu.hotel_service.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HotelService {

    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final RoomLockRepository roomLockRepository;
    private final HotelMapper hotelMapper;
    private final RoomMapper roomMapper;

    @Transactional
    public HotelDto createHotel(HotelCreateDto hotelCreateDto) {
        Hotel hotel = hotelMapper.toEntity(hotelCreateDto);
        hotelRepository.save(hotel);
        log.info("Created hotel {}", hotel.getId());
        return hotelMapper.toDto(hotel, List.of());
    }

    @Transactional(readOnly = true)
    public List<HotelDto> getHotels(String name, String address, Sort sort) {
        List<Hotel> hotels = findHotels(name, address, sort);
        if (hotels.isEmpty()) {
            return List.of();
        }

        Map<UUID, List<RoomDto>> roomsByHotel = roomRepository
                .findAllByHotelIdIn(hotels.stream().map(Hotel::getId).toList())
                .stream()
                .collect(Collectors.groupingBy(
                        Room::getHotelId,
                        Collectors.mapping(roomMapper::toDto, Collectors.toList())
                ));

        return hotels.stream()
                .map(hotel -> hotelMapper.toDto(
                        hotel,
                        roomsByHotel.getOrDefault(hotel.getId(), List.of())))
                .toList();
    }

    @Transactional(readOnly = true)
    public HotelDto getHotel(UUID id) {
        Hotel hotel = getHotelOrThrow(id);
        List<RoomDto> rooms = roomRepository.findAllByHotelId(id, Sort.by("number")).stream()
                .map(roomMapper::toDto)
                .toList();
        return hotelMapper.toDto(hotel, rooms);
    }

    @Transactional
    public HotelDto updateHotel(UUID id, HotelUpdateDto hotelUpdateDto) {
        Hotel hotel = getHotelOrThrow(id);
        hotel.setName(hotelUpdateDto.name());
        hotel.setAddress(hotelUpdateDto.address());
        log.info("Updated hotel {}", id);
        return getHotel(id);
    }

    @Transactional
    public void deleteHotel(UUID id) {
        Hotel hotel = getHotelOrThrow(id);
        List<UUID> roomIds = roomRepository.findAllByHotelIdForUpdate(id).stream()
                .map(Room::getId)
                .toList();

        if (!roomIds.isEmpty()) {
            if (roomLockRepository.existsByRoomIdInAndReleasedFalse(roomIds)) {
                throw new ConflictException("Hotel has active bookings: " + id);
            }
            roomLockRepository.deleteByRoomIdIn(roomIds);
            roomRepository.deleteAllByHotelId(id);
        }

        hotelRepository.delete(hotel);
        log.info("Deleted hotel {}", id);
    }

    @Transactional(readOnly = true)
    public HotelStatsDto getStats(UUID id) {
        getHotelOrThrow(id);
        long totalRooms = roomRepository.countByHotelId(id);
        long availableRooms = roomRepository.countByHotelIdAndAvailable(
                id, Room.AvailableStatus.AVAILABLE);
        long totalTimesBooked = Optional
                .ofNullable(roomRepository.sumTimesBookedByHotelId(id))
                .orElse(0L);

        return new HotelStatsDto(id, totalRooms, availableRooms, totalTimesBooked);
    }

    private List<Hotel> findHotels(String name, String address, Sort sort) {
        boolean hasName = StringUtils.hasText(name);
        boolean hasAddress = StringUtils.hasText(address);

        if (hasName && hasAddress) {
            return hotelRepository.findAllByNameContainingIgnoreCaseAndAddressContainingIgnoreCase(
                    name, address, sort);
        }
        if (hasName) {
            return hotelRepository.findAllByNameContainingIgnoreCase(name, sort);
        }
        if (hasAddress) {
            return hotelRepository.findAllByAddressContainingIgnoreCase(address, sort);
        }
        return hotelRepository.findAll(sort);
    }

    private Hotel getHotelOrThrow(UUID id) {
        return hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found: " + id));
    }
}
