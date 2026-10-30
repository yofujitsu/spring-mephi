package com.yofujitsu.hotel_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
        name = "room_locks",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_room_locks_booking",
                columnNames = "booking_id"
        ),
        indexes = @Index(
                name = "idx_room_locks_room_dates",
                columnList = "room_id, start_date, end_date"
        )
)
public class RoomLock {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "room_id", nullable = false)
    private UUID roomId;

    @Column(name = "booking_id", nullable = false)
    private UUID bookingId;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "request_id")
    private UUID requestId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
