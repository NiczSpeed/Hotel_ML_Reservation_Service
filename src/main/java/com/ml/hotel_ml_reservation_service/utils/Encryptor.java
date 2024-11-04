package com.ml.hotel_ml_reservation_service.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;


@RequiredArgsConstructor
public class Encryptor<T> implements AttributeConverter<T, String> {


    private final EncryptorUtil encryptorUtil;
    private final ObjectMapper objectMapper;
    private Class<T> clazz;



    @SneakyThrows
    @Override
    public String convertToDatabaseColumn(T t) {
        String json = objectMapper.writeValueAsString(t);
        return encryptorUtil.encrypt(json);
    }

    @SneakyThrows
    @Override
    public T convertToEntityAttribute(String s) {
        String json = encryptorUtil.decrypt(s);
        return objectMapper.readValue(json, clazz);
    }

}
