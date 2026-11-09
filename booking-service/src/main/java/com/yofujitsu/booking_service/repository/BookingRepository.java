package com.yofujitsu.booking_service.repository;

import com.yofujitsu.booking_service.entity.Booking;
import com.yofujitsu.booking_service.entity.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Booking b where b.id = :id")
    Optional<Booking> findByIdForUpdate(@Param("id") UUID id);

    List<Booking> findByReleasePendingTrue();

    Optional<Booking> findByRequestId(UUID requestId);

    Page<Booking> findByUserId(UUID userId, Pageable pageable);

    @Query("""
            select count(b)
            from Booking b
            where b.roomId = :roomId
              and b.status <> :cancelled
              and b.startDate < :endDate
              and b.endDate > :startDate
            """)
    long countOverlap(
            @Param("roomId") UUID roomId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("cancelled") BookingStatus cancelled);
}
