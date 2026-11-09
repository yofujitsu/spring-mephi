package com.yofujitsu.hotel_service.repository;

import com.yofujitsu.hotel_service.entity.Room;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoomRepository extends JpaRepository<Room, UUID> {

    List<Room> findAllByHotelId(UUID hotelId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Room r where r.hotelId = :hotelId order by r.id")
    List<Room> findAllByHotelIdForUpdate(@Param("hotelId") UUID hotelId);

    List<Room> findAllByHotelId(UUID hotelId, Sort sort);

    List<Room> findAllByHotelIdIn(Collection<UUID> hotelIds);

    List<Room> findAllByAvailable(Room.AvailableStatus available, Sort sort);

    List<Room> findAllByAvailableAndHotelId(
            Room.AvailableStatus available, UUID hotelId, Sort sort);

    List<Room> findAllByAvailableOrderByTimesBookedAscIdAsc(Room.AvailableStatus available);

    boolean existsByHotelIdAndNumber(UUID hotelId, Integer number);

    long countByHotelId(UUID hotelId);

    long countByHotelIdAndAvailable(UUID hotelId, Room.AvailableStatus available);

    @Query("select coalesce(sum(r.timesBooked), 0) from Room r where r.hotelId = :hotelId")
    Long sumTimesBookedByHotelId(@Param("hotelId") UUID hotelId);

    @Modifying
    @Query("delete from Room r where r.hotelId = :hotelId")
    void deleteAllByHotelId(@Param("hotelId") UUID hotelId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Room r where r.id = :id")
    Optional<Room> findByIdForUpdate(@Param("id") UUID id);
}
