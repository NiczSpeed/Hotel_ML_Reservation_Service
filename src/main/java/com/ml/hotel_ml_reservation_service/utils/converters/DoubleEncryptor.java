package com.ml.hotel_ml_reservation_service.utils.converters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ml.hotel_ml_reservation_service.utils.Encryptor;
import com.ml.hotel_ml_reservation_service.utils.EncryptorUtil;

import java.time.LocalDate;

public class DoubleEncryptor extends Encryptor {

    public DoubleEncryptor(EncryptorUtil encryptorUtil, ObjectMapper objectMapper) {
        super(encryptorUtil, objectMapper);
    }

    @Override
    protected Class<Double> getTargetClass() {
        return Double.class;
    }

}
