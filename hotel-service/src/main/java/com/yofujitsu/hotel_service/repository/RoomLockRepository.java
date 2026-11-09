package com.yofujitsu.hotel_service.repository;

import com.yofujitsu.hotel_service.entity.RoomLock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface RoomLockRepository extends JpaRepository<RoomLock, UUID> {

    Optional<RoomLock> findByBookingId(UUID bookingId);

    boolean existsByRoomIdInAndReleasedFalse(Collection<UUID> roomIds);

    @Query("""
            select count(l) > 0
            from RoomLock l
            where l.roomId = :roomId
              and l.released = false
              and l.startDate < :endDate
              and l.endDate > :startDate
            """)
    boolean existsOverlap(
            @Param("roomId") UUID roomId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Modifying
    @Query("delete from RoomLock l where l.roomId = :roomId")
    int deleteByRoomId(@Param("roomId") UUID roomId);

    @Modifying
    @Query("delete from RoomLock l where l.roomId in :roomIds")
    int deleteByRoomIdIn(@Param("roomIds") Collection<UUID> roomIds);
}
