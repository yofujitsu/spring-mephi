package com.yofujitsu.booking_service.service;

import com.yofujitsu.booking_service.client.HotelClient;
import com.yofujitsu.booking_service.dto.booking.BookingDto;
import com.yofujitsu.booking_service.dto.booking.CreateBookingRequest;
import com.yofujitsu.booking_service.dto.common.PageResponse;
import com.yofujitsu.booking_service.dto.hotel.HotelRoomDto;
import com.yofujitsu.booking_service.entity.Booking;
import com.yofujitsu.booking_service.entity.BookingStatus;
import com.yofujitsu.booking_service.exception.ConflictException;
import com.yofujitsu.booking_service.exception.ResourceNotFoundException;
import com.yofujitsu.booking_service.mapper.BookingMapper;
import com.yofujitsu.booking_service.repository.BookingRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private static final String AVAILABLE = "AVAILABLE";

    private final BookingRepository bookingRepository;
    private final RoomGuardService roomGuardService;
    private final BookingMapper bookingMapper;
    private final HotelClient hotelClient;

    @Transactional
    public BookingDto create(UUID userId, CreateBookingRequest request) {
        validatePeriod(request.startDate(), request.endDate());

        Optional<Booking> existing = bookingRepository.findByRequestId(request.requestId());
        if (existing.isPresent()) {
            if (!existing.get().getUserId().equals(userId)) {
                throw new ConflictException(
                        "requestId is already used by another user: " + request.requestId());
            }
            log.info("Booking creation is idempotent: requestId={}, bookingId={}",
                    request.requestId(), existing.get().getId());
            return bookingMapper.toDto(existing.get());
        }

        HotelRoomDto room = request.autoSelect()
                ? selectRoom(request)
                : resolveExplicitRoom(request);

        Booking booking = Booking.builder()
                .userId(userId)
                .roomId(room.id())
                .hotelId(room.hotelId())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .status(BookingStatus.PENDING)
                .requestId(request.requestId())
                .createdAt(Instant.now())
                .build();
        bookingRepository.save(booking);

        log.info("Created booking {}: userId={}, roomId={}, period={}..{}",
                booking.getId(), userId, room.id(),
                request.startDate(), request.endDate());
        return bookingMapper.toDto(booking);
    }

    @Transactional(readOnly = true)
    public PageResponse<BookingDto> getBookings(UUID userId, int page, int size) {
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                normalizeSize(size),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        return PageResponse.from(
                bookingRepository.findByUserId(userId, pageable)
                        .map(bookingMapper::toDto)
        );
    }

    @Transactional(readOnly = true)
    public BookingDto getBooking(UUID id, UUID userId, boolean admin) {
        Booking booking = getBookingOrThrow(id);
        ensureOwnerOrAdmin(booking, userId, admin);
        return bookingMapper.toDto(booking);
    }

    @Transactional
    public void cancel(UUID id, UUID userId, boolean admin) {
        Booking booking = getBookingOrThrow(id);
        ensureOwnerOrAdmin(booking, userId, admin);

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            log.info("Booking {} is already cancelled", id);
            return;
        }

        booking.setStatus(BookingStatus.CANCELLED);
        log.info("Cancelled booking {}: userId={}", id, userId);
    }

    private HotelRoomDto resolveExplicitRoom(CreateBookingRequest request) {
        if (request.roomId() == null) {
            throw new IllegalArgumentException(
                    "roomId is required when autoSelect is false");
        }

        HotelRoomDto room = fetchRoom(request.roomId());
        if (request.hotelId() != null && !request.hotelId().equals(room.hotelId())) {
            throw new IllegalArgumentException(
                    "roomId does not belong to the requested hotel");
        }
        if (!AVAILABLE.equals(room.available())) {
            throw new ConflictException("Room is not available: " + room.id());
        }

        roomGuardService.ensureGuard(room.id());
        roomGuardService.lockGuard(room.id());
        ensureNoOverlap(room.id(), request.startDate(), request.endDate());

        return room;
    }

    private HotelRoomDto selectRoom(CreateBookingRequest request) {
        List<HotelRoomDto> candidates;
        try {
            candidates = hotelClient.recommend();
        } catch (FeignException exception) {
            throw new IllegalStateException("Failed to load recommended rooms", exception);
        }

        List<HotelRoomDto> filtered = candidates.stream()
                .filter(room -> request.hotelId() == null
                        || request.hotelId().equals(room.hotelId()))
                .toList();

        for (HotelRoomDto candidate : filtered) {
            roomGuardService.ensureGuard(candidate.id());
            roomGuardService.lockGuard(candidate.id());

            if (bookingRepository.countOverlap(
                    candidate.id(),
                    request.startDate(),
                    request.endDate(),
                    BookingStatus.CANCELLED) == 0) {
                return candidate;
            }
        }

        throw new ConflictException(
                "No available room for the requested period");
    }

    private HotelRoomDto fetchRoom(UUID roomId) {
        try {
            return hotelClient.getRoom(roomId);
        } catch (FeignException.NotFound exception) {
            throw new ResourceNotFoundException("Room not found: " + roomId);
        } catch (FeignException.Conflict exception) {
            throw new ConflictException("Room is not available: " + roomId);
        } catch (FeignException exception) {
            throw new IllegalStateException("Failed to load room " + roomId, exception);
        }
    }

    private void ensureNoOverlap(UUID roomId, LocalDate startDate, LocalDate endDate) {
        if (bookingRepository.countOverlap(
                roomId, startDate, endDate, BookingStatus.CANCELLED) > 0) {
            throw new ConflictException(
                    "Room %s is already booked for the requested period".formatted(roomId));
        }
    }

    private void validatePeriod(LocalDate startDate, LocalDate endDate) {
        if (!startDate.isBefore(endDate)) {
            throw new IllegalArgumentException("startDate must be before endDate");
        }
        if (startDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("startDate must not be in the past");
        }
    }

    private Booking getBookingOrThrow(UUID id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + id));
    }

    private void ensureOwnerOrAdmin(Booking booking, UUID userId, boolean admin) {
        if (!admin && !booking.getUserId().equals(userId)) {
            throw new AccessDeniedException("Access is denied");
        }
    }

    private int normalizeSize(int size) {
        if (size < 1) {
            return 10;
        }
        return Math.min(size, 100);
    }
}
