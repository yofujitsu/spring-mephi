package com.yofujitsu.hotel_service.repository;

import com.yofujitsu.hotel_service.entity.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RoomRepository extends JpaRepository<Room, UUID> {
    List<Room> findAllByHotelId(UUID hotelId);

    List<Room> findAllByAvailable(Room.AvailableStatus available);
}