package com.ml.hotel_ml_reservation_service.utils;

import jakarta.persistence.AttributeConverter;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

@AllArgsConstructor
public class Encryptor implements AttributeConverter<String, String> {


    private final EncryptorUtil encryptorUtil;

    @SneakyThrows
    @Override
    public String convertToDatabaseColumn(String s) {
        return encryptorUtil.encrypt(s);
    }

    @SneakyThrows
    @Override
    public String convertToEntityAttribute(String s) {
        return encryptorUtil.decrypt(s);
    }
}
