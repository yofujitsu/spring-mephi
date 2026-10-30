package com.yofujitsu.hotel_service.service;

import com.yofujitsu.hotel_service.dto.room.ConfirmAvailabilityRequest;
import com.yofujitsu.hotel_service.dto.room.ReleaseRequest;
import com.yofujitsu.hotel_service.entity.Room;
import com.yofujitsu.hotel_service.entity.RoomLock;
import com.yofujitsu.hotel_service.exception.ConflictException;
import com.yofujitsu.hotel_service.exception.ResourceNotFoundException;
import com.yofujitsu.hotel_service.repository.RoomLockRepository;
import com.yofujitsu.hotel_service.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomLockService {

    private final RoomRepository roomRepository;
    private final RoomLockRepository roomLockRepository;

    @Transactional
    public void confirmAvailability(UUID roomId, ConfirmAvailabilityRequest request) {
        validatePeriod(request.startDate(), request.endDate());

        Room room = roomRepository.findByIdForUpdate(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found: " + roomId));

        Optional<RoomLock> existingLock = roomLockRepository.findByBookingId(request.bookingId());
        if (existingLock.isPresent()) {
            if (!existingLock.get().getRoomId().equals(roomId)) {
                throw new ConflictException("Booking %s is already locked for another room"
                        .formatted(request.bookingId()));
            }
            log.info("confirm-availability is idempotent: roomId={}, bookingId={}, requestId={}",
                    roomId, request.bookingId(), request.requestId());
            return;
        }

        if (room.getAvailable() != Room.AvailableStatus.AVAILABLE) {
            throw new ConflictException("Room %s is not operationally available".formatted(roomId));
        }
        if (roomLockRepository.existsOverlap(
                roomId, request.startDate(), request.endDate())) {
            throw new ConflictException("Room %s is already locked for the requested period"
                    .formatted(roomId));
        }

        RoomLock lock = RoomLock.builder()
                .roomId(roomId)
                .bookingId(request.bookingId())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .requestId(request.requestId())
                .createdAt(Instant.now())
                .build();
        roomLockRepository.save(lock);

        int timesBooked = room.getTimesBooked() == null ? 0 : room.getTimesBooked();
        room.setTimesBooked(timesBooked + 1);

        log.info("Confirmed availability: roomId={}, bookingId={}, requestId={}, period={}..{}",
                roomId, request.bookingId(), request.requestId(),
                request.startDate(), request.endDate());
    }

    @Transactional
    public void release(UUID roomId, ReleaseRequest request) {
        Optional<RoomLock> lock = roomLockRepository.findByBookingId(request.bookingId());
        if (lock.isEmpty()) {
            log.info("release is idempotent, no lock found: roomId={}, bookingId={}, requestId={}",
                    roomId, request.bookingId(), request.requestId());
            return;
        }
        if (!lock.get().getRoomId().equals(roomId)) {
            throw new ConflictException("Booking %s is locked for another room"
                    .formatted(request.bookingId()));
        }

        roomLockRepository.deleteByBookingId(request.bookingId());
        log.info("Released room lock: roomId={}, bookingId={}, requestId={}",
                roomId, request.bookingId(), request.requestId());
    }

    private void validatePeriod(LocalDate startDate, LocalDate endDate) {
        if (!startDate.isBefore(endDate)) {
            throw new IllegalArgumentException("startDate must be before endDate");
        }
    }
}
