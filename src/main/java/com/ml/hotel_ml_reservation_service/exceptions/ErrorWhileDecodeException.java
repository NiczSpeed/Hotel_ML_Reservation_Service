package com.ml.hotel_ml_reservation_service.exceptions;

public class ErrorWhileDecodeException extends RuntimeException {
    public ErrorWhileDecodeException() {
        super("Error While Decoding!");
    }
}
