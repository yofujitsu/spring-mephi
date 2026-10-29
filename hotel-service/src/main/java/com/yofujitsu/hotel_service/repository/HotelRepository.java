package com.yofujitsu.hotel_service.repository;

import com.yofujitsu.hotel_service.entity.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface HotelRepository extends JpaRepository<Hotel, UUID> {
}