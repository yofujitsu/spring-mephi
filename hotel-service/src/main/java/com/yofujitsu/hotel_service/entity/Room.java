package com.yofujitsu.hotel_service.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "hotel_rooms")
public class Room {
    @Id
    @UuidGenerator
    private UUID id;
    private UUID hotelId;
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer number;
    @Enumerated(EnumType.STRING)
    private AvailableStatus available = AvailableStatus.AVAILABLE;
    private Integer timesBooked = 0;

    public enum AvailableStatus {
        AVAILABLE,
        MAINTENANCE,
        DECOMMISSIONING
    }
}
