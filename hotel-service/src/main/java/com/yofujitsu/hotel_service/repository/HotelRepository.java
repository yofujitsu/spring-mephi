package com.yofujitsu.hotel_service.repository;

import com.yofujitsu.hotel_service.entity.Hotel;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface HotelRepository extends JpaRepository<Hotel, UUID> {

    List<Hotel> findAllByNameContainingIgnoreCase(String name, Sort sort);

    List<Hotel> findAllByAddressContainingIgnoreCase(String address, Sort sort);

    List<Hotel> findAllByNameContainingIgnoreCaseAndAddressContainingIgnoreCase(
            String name, String address, Sort sort);
}
