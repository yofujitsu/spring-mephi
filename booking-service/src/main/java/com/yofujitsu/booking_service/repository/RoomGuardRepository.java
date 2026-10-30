package com.yofujitsu.booking_service.repository;

import com.yofujitsu.booking_service.entity.RoomGuard;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface RoomGuardRepository extends JpaRepository<RoomGuard, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select g from RoomGuard g where g.roomId = :roomId")
    Optional<RoomGuard> lockById(@Param("roomId") UUID roomId);

    @Modifying
    @Query(
            value = "MERGE INTO room_guards (room_id) KEY (room_id) VALUES (:roomId)",
            nativeQuery = true
    )
    void mergeGuard(@Param("roomId") UUID roomId);
}
