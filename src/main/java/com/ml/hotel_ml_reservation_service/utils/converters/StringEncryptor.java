package com.ml.hotel_ml_reservation_service.utils.converters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ml.hotel_ml_reservation_service.utils.Encryptor;
import com.ml.hotel_ml_reservation_service.utils.EncryptorUtil;

public class StringEncryptor extends Encryptor {


    public StringEncryptor(EncryptorUtil encryptorUtil, ObjectMapper objectMapper) {
        super(encryptorUtil, objectMapper);
    }

    @Override
    protected Class<String> getTargetClass() {
        return String.class;
    }
}
