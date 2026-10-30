package com.yofujitsu.hotel_service.controller;

import com.yofujitsu.hotel_service.dto.room.ConfirmAvailabilityRequest;
import com.yofujitsu.hotel_service.dto.room.ReleaseRequest;
import com.yofujitsu.hotel_service.dto.room.RoomCreateDto;
import com.yofujitsu.hotel_service.dto.room.RoomDto;
import com.yofujitsu.hotel_service.dto.room.RoomStatsDto;
import com.yofujitsu.hotel_service.dto.room.RoomUpdateDto;
import com.yofujitsu.hotel_service.service.RoomLockService;
import com.yofujitsu.hotel_service.service.RoomService;
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
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;
    private final RoomLockService roomLockService;

    @GetMapping
    public List<RoomDto> getAvailableRooms(
            @RequestParam(required = false) UUID hotelId,
            Sort sort) {
        return roomService.getAvailableRooms(hotelId, sort);
    }

    @GetMapping("/recommend")
    public List<RoomDto> getRecommendedRooms() {
        return roomService.getRecommendedRooms();
    }

    @GetMapping("/stats")
    public List<RoomStatsDto> getStats(Sort sort) {
        return roomService.getStats(sort);
    }

    @GetMapping("/{id}")
    public RoomDto getRoom(@PathVariable UUID id) {
        return roomService.getRoom(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoomDto createRoom(@Valid @RequestBody RoomCreateDto roomCreateDto) {
        return roomService.createRoom(roomCreateDto);
    }

    @PutMapping("/{id}")
    public RoomDto updateRoom(
            @PathVariable UUID id,
            @Valid @RequestBody RoomUpdateDto roomUpdateDto) {
        return roomService.updateRoom(id, roomUpdateDto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRoom(@PathVariable UUID id) {
        roomService.deleteRoom(id);
    }

    @PostMapping("/{id}/confirm-availability")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void confirmAvailability(
            @PathVariable UUID id,
            @Valid @RequestBody ConfirmAvailabilityRequest request) {
        roomLockService.confirmAvailability(id, request);
    }

    @PostMapping("/{id}/release")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void release(
            @PathVariable UUID id,
            @Valid @RequestBody ReleaseRequest request) {
        roomLockService.release(id, request);
    }
}
