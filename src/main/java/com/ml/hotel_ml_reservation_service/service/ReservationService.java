package com.ml.hotel_ml_reservation_service.service;

import com.ml.hotel_ml_reservation_service.dto.ReservationDto;
import com.ml.hotel_ml_reservation_service.mapper.ReservationMapper;
import com.ml.hotel_ml_reservation_service.model.Reservation;
import com.ml.hotel_ml_reservation_service.repository.ReservationRepository;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

@Service
public class ReservationService {

    Logger logger = Logger.getLogger(getClass().getName());

    private final ReservationRepository reservationRepository;
    private final KafkaTemplate kafkaTemplate;

    @Autowired
    public ReservationService(ReservationRepository reservationRepository, KafkaTemplate kafkaTemplate) {
        this.reservationRepository = reservationRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "create_reservation_topic", groupId = "hotel_ml_reservation_service")
    private void createReservation(String message) throws Exception {
        try {
            JSONObject json = decodeMessage(message);
            String messageId = json.optString("messageId");
            ReservationDto reservationDto = new ReservationDto().builder()
                    .startDate(LocalDate.parse(json.optString("startDate")))
                    .endDate(LocalDate.parse(json.optString("endDate")))
                    .hotelCity(json.optString("hotelCity"))
                    .hotelName(json.optString("hotelName"))
                    .roomNumber(json.optLong("roomNumber"))
                    .clientEmail(json.optString("clientEmail"))
                    .build();
            Reservation reservation = ReservationMapper.Instance.mapReservationDtoToReservation(reservationDto);
            reservationRepository.save(reservation);
            logger.info("Reservation created: " + reservation.toString());
            sendRequestMessage("Reservation created!", messageId, "success_request_topic");
        } catch (Exception e) {
            logger.severe("Error while creating reservation: " + e.getMessage());
        }
    }

    @KafkaListener(topics = "check_reservation_topic", groupId = "hotel_ml_reservation_service")
    private void checkFreeRoom(String message) throws Exception {
        try {
            JSONObject json = decodeMessage(message);
            String messageId = json.optString("messageId");
            Set<Reservation> reservations = reservationRepository.findByHotelCity(json.optString("city"));
            if(reservations.isEmpty()){
                sendRequestMessage("False", messageId, "boolean_reservation_topic");
            }
        } catch (Exception e) {
            logger.severe("Error while creating hotel: " + e.getMessage());
        }
    }

    private JSONObject decodeMessage(String message) {
        byte[] decodedBytes = Base64.getDecoder().decode(message);
        message = new String(decodedBytes);
        return new JSONObject(message);
    }

    private String sendRequestMessage(String message, String messageId, String topic) {
        JSONObject json = new JSONObject();
        json.put("messageId", messageId);
        json.put("message", message);
        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, json.toString());
        future.whenComplete((result, exception) -> {
            if (exception != null) logger.severe(exception.getMessage());
            else logger.info("Message send successfully!");
        });
        return message;
    }


}
