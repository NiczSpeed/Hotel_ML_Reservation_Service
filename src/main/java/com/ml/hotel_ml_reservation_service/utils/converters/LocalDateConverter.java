package com.ml.hotel_ml_reservation_service.utils.converters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ml.hotel_ml_reservation_service.utils.Encryptor;
import com.ml.hotel_ml_reservation_service.utils.EncryptorUtil;

import java.time.LocalDate;

public class LocalDateConverter extends Encryptor<LocalDate> {

    public LocalDateConverter(EncryptorUtil encryptorUtil, ObjectMapper objectMapper) {
        super(encryptorUtil, objectMapper);
    }

    @Override
    protected Class<LocalDate> getTargetClass() {
        return LocalDate.class;
    }

}
