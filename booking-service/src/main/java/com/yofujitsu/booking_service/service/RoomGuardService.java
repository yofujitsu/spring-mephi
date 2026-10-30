package com.yofujitsu.booking_service.service;

import com.yofujitsu.booking_service.repository.RoomGuardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoomGuardService {

    private final RoomGuardRepository roomGuardRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void ensureGuard(UUID roomId) {
        roomGuardRepository.mergeGuard(roomId);
    }

    @Transactional
    public void lockGuard(UUID roomId) {
        roomGuardRepository.lockById(roomId)
                .orElseThrow(() -> new IllegalStateException(
                        "Room guard is missing: " + roomId));
    }
}
