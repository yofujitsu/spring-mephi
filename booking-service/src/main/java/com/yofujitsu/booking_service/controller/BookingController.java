package com.yofujitsu.booking_service.controller;

import com.yofujitsu.booking_service.dto.booking.BookingDto;
import com.yofujitsu.booking_service.dto.booking.CreateBookingRequest;
import com.yofujitsu.booking_service.dto.common.PageResponse;
import com.yofujitsu.booking_service.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping("/booking")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public BookingDto create(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateBookingRequest request) {
        return bookingService.create(currentUserId(jwt), request);
    }

    @GetMapping("/bookings")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public PageResponse<BookingDto> getBookings(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return bookingService.getBookings(currentUserId(jwt), page, size);
    }

    @GetMapping("/booking/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public BookingDto getBooking(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id) {
        return bookingService.getBooking(id, currentUserId(jwt), isAdmin(jwt));
    }

    @DeleteMapping("/booking/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id) {
        bookingService.cancel(id, currentUserId(jwt), isAdmin(jwt));
    }

    private UUID currentUserId(Jwt jwt) {
        String userId = jwt.getClaimAsString("userId");
        if (userId == null) {
            throw new IllegalStateException("JWT does not contain userId claim");
        }
        return UUID.fromString(userId);
    }

    private boolean isAdmin(Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList("roles");
        return roles != null && roles.contains("ADMIN");
    }
}
