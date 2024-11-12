package com.ml.hotel_ml_reservation_service.exceptions;

public class ReservationNotFoundException extends RuntimeException {
    public ReservationNotFoundException() {
        super("Reservation Not Found!");
    }
}
