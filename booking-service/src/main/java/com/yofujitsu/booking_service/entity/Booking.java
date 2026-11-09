package com.yofujitsu.booking_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(
        name = "bookings",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_bookings_request",
                columnNames = "request_id"
        ),
        indexes = {
                @Index(
                        name = "idx_bookings_user_created",
                        columnList = "user_id, created_at"
                ),
                @Index(
                        name = "idx_bookings_room_dates",
                        columnList = "room_id, start_date, end_date"
                )
        }
)
public class Booking {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "room_id", nullable = false)
    private UUID roomId;

    @Column(name = "hotel_id", nullable = false)
    private UUID hotelId;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    @Column(name = "request_id", nullable = false)
    private UUID requestId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private boolean releasePending;
}
