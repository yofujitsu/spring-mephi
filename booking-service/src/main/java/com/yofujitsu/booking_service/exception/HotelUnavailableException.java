package com.yofujitsu.booking_service.exception;

public class HotelUnavailableException extends RuntimeException {

    public HotelUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
