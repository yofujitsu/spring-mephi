package com.yofujitsu.hotel_service.config;

import com.yofujitsu.hotel_service.entity.Room;
import com.yofujitsu.hotel_service.repository.HotelRepository;
import com.yofujitsu.hotel_service.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class DemoDataInitializer implements ApplicationRunner {

    private static final UUID GRAND_HOTEL_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID RIVERSIDE_HOTEL_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID AIRPORT_HOTEL_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (hotelRepository.count() > 0) {
            return;
        }

        jdbcTemplate.batchUpdate(
                "insert into hotels (id, name, address) values (?, ?, ?)",
                List.of(
                        new Object[]{GRAND_HOTEL_ID, "Mephi Grand",
                                "Moscow, Kashirskoe highway 31"},
                        new Object[]{RIVERSIDE_HOTEL_ID, "Mephi Riverside",
                                "Moscow, Leninskie Gory 1"},
                        new Object[]{AIRPORT_HOTEL_ID, "Mephi Airport",
                                "Moscow, Sheremetyevo airport"}
                ));

        jdbcTemplate.batchUpdate(
                "insert into rooms (id, hotel_id, number, available, times_booked) "
                        + "values (?, ?, ?, ?, ?)",
                List.of(
                        roomArgs(GRAND_HOTEL_ID, "aaaaaaaa-0000-0000-0000-000000000101", 101,
                                Room.AvailableStatus.AVAILABLE, 0),
                        roomArgs(GRAND_HOTEL_ID, "aaaaaaaa-0000-0000-0000-000000000102", 102,
                                Room.AvailableStatus.AVAILABLE, 3),
                        roomArgs(RIVERSIDE_HOTEL_ID, "bbbbbbbb-0000-0000-0000-000000000201", 201,
                                Room.AvailableStatus.AVAILABLE, 7),
                        roomArgs(RIVERSIDE_HOTEL_ID, "bbbbbbbb-0000-0000-0000-000000000202", 202,
                                Room.AvailableStatus.MAINTENANCE, 1),
                        roomArgs(AIRPORT_HOTEL_ID, "cccccccc-0000-0000-0000-000000000301", 301,
                                Room.AvailableStatus.AVAILABLE, 5)
                ));

        log.info("Demo hotels and rooms created: hotels={}, rooms={}",
                hotelRepository.count(), roomRepository.count());
    }

    private Object[] roomArgs(
            UUID hotelId,
            String id,
            int number,
            Room.AvailableStatus status,
            int timesBooked) {
        return new Object[]{
                UUID.fromString(id), hotelId, number, status.name(), timesBooked
        };
    }
}
