package com.ml.hotel_ml_reservation_service.service;

import com.ml.hotel_ml_reservation_service.dto.ReservationDto;
import com.ml.hotel_ml_reservation_service.mapper.ReservationMapper;
import com.ml.hotel_ml_reservation_service.model.Reservation;
import com.ml.hotel_ml_reservation_service.repository.ReservationRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;

@Service
public class ReservationService {

    Logger logger = Logger.getLogger(getClass().getName());

    private final Map<String, CompletableFuture<String>> responseFutures = new ConcurrentHashMap<>();

    private final ReservationRepository reservationRepository;
    private final KafkaTemplate kafkaTemplate;

    @Autowired
    public ReservationService(ReservationRepository reservationRepository, KafkaTemplate kafkaTemplate) {
        this.reservationRepository = reservationRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "create_reservation_topic", groupId = "hotel_ml_reservation_service")
    private void createReservation(String message) throws Exception {
        JSONObject json = new JSONObject(decodeMessage(message));
        String messageId = json.optString("messageId");
        String priceMessageId = UUID.randomUUID().toString();

        CompletableFuture<String> responseFuture = new CompletableFuture<>();
        responseFutures.put(priceMessageId, responseFuture);
        String messageWithId = attachMessageId(json.toString(), priceMessageId);
        kafkaTemplate.send("check_room_reservation_price_topic", Base64.getEncoder().encodeToString(messageWithId.getBytes()));
        try {
            String response = responseFuture.get(5, TimeUnit.SECONDS);
            responseFutures.remove(priceMessageId);
            JSONObject priceJson = new JSONObject(response);
            if (response.contains("Error")) {
                sendRequestMessage(response, messageId, "error_request_topic");
            } else {
                ReservationDto reservationDto = new ReservationDto().builder()
                        .startDate(LocalDate.parse(json.optString("startDate")))
                        .endDate(LocalDate.parse(json.optString("endDate")))
                        .hotelCity(json.optString("hotelCity"))
                        .hotelName(json.optString("hotelName"))
                        .roomNumber(json.optLong("roomNumber"))
                        .clientEmail(json.optString("clientEmail"))
                        .amountPayable(priceJson.optDouble("message"))
                        .build();
                Reservation reservation = ReservationMapper.Instance.mapReservationDtoToReservation(reservationDto);
                reservationRepository.save(reservation);
                sendEncodedMessage(String.valueOf(reservationDto.getAmountPayable()), messageId, "response_create_reservation_topic");
            }


        } catch (Exception e) {
            logger.severe("Error while creating reservation: " + e.getMessage());
        }
    }

    @KafkaListener(topics = "check_reservation_topic", groupId = "hotel_ml_reservation_service")
    private void checkFreeRoom(String message) throws Exception {
        try {
            JSONObject json = new JSONObject(decodeMessage(message));
            String messageId = json.optString("messageId");
            json.remove("messageId");
            Map<String, Object> messageMap = json.getJSONObject("message").toMap();
            json.clear();
            json = new JSONObject(messageMap);
            LocalDate startDate = LocalDate.parse(json.optString("startDate"));
            LocalDate endDate = LocalDate.parse(json.optString("endDate"));
            Set<Reservation> checkReservations = reservationRepository.findReservationByHotelNameAndHotelCityAndRoomNumber(json.optString("hotel"), json.optString("city"), json.optLong("room"));

            if (checkReservations.isEmpty() || isDateRangeAvailable(checkReservations, startDate, endDate)) {
                sendRequestMessage("True", messageId, "boolean_reservation_topic");
            } else {
                sendRequestMessage("False", messageId, "boolean_reservation_topic");
            }
        } catch (Exception e) {
            logger.severe("Error while creating hotel: " + e.getMessage());
        }
    }

    @KafkaListener(topics = "all_user_reservation_request_topic", groupId = "hotel_ml_reservation_service")
    private void getAllUserReservations(String message) throws Exception {
        try {
            JSONObject json = new JSONObject(decodeMessage(message));
            String messageId = json.optString("messageId");
            String userEmail = json.optString("email");
            List<Reservation> reservations = reservationRepository.findByclientEmail(userEmail);
            List<ReservationDto> reservationDtos = ReservationMapper.Instance.mapReservationListToReservationDtoList(reservations);
            if (reservationDtos.isEmpty()) {
                sendRequestMessage("Error:There is no reservations on your account!", messageId, "error_request_topic");
            } else {
                JSONArray jsonArray = new JSONArray(reservationDtos);
                sendEncodedMessage(jsonArray.toString(), messageId, "all_user_reservation_response_topic");
            }
        } catch (Exception e) {
            logger.severe("Error while creating hotel: " + e.getMessage());
        }
    }

//    private JSONObject decodeMessage(String message) {
//        byte[] decodedBytes = Base64.getDecoder().decode(message);
//        message = new String(decodedBytes);
//        return new JSONObject(message);
//    }

    private String sendRequestMessage(String message, String messageId, String topic) {
        JSONObject json = new JSONObject();
        json.put("messageId", messageId);
        json.put("message", message);
        logger.severe(json.toString());
        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, json.toString());
        future.whenComplete((result, exception) -> {
            if (exception != null) logger.severe(exception.getMessage());
            else logger.info("Message send successfully!");
        });
        return message;
    }

    private static boolean isDateRangeAvailable(Set<Reservation> reservationse, LocalDate newStart, LocalDate newEnd) {
        for (Reservation reservation : reservationse) {
            LocalDate existingStart = reservation.getStartDate();
            LocalDate existingEnd = reservation.getEndDate();

            if (newEnd.isBefore(existingStart) || newEnd.equals(existingStart) || newStart.isAfter(existingEnd) || newStart.equals(existingEnd)) {
            } else {
                return false;
            }
        }
        return true;
    }

    String attachMessageId(String message, String messageId) {
        JSONObject json = new JSONObject(message);
        json.put("messageId", messageId);
        return json.toString();
    }

    private String sendEncodedMessage(String message, String messageId, String topic) {
        JSONObject json = new JSONObject();
        json.put("messageId", messageId);
        if(message.contains("[")) json.put("message", new JSONArray(message));
        else json.put("message", message);
        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, Base64.getEncoder().encodeToString(json.toString().getBytes()));
        future.whenComplete((result, exception) -> {
            if (exception != null) logger.severe(exception.getMessage());
            else logger.info("Message send successfully!");
        });
        return message;
    }

    @KafkaListener(topics = "room_price_topic", groupId = "hotel_ml_apigateway_service")
    public void earnUserDetails(String message) {
        getRequestMessage(decodeMessage(message));
    }

    void getRequestMessage(String message) {
        String messageId = extractMessageId(message);
        CompletableFuture<String> responseFuture = responseFutures.get(messageId);
        if (responseFuture != null) {
            responseFuture.complete(message);
        }
    }

    String extractMessageId(String message) {
        JSONObject json = new JSONObject(message);
        return json.optString("messageId");
    }

    String decodeMessage(String message) {
        byte[] decodedBytes = Base64.getDecoder().decode(message);
        return new String(decodedBytes);
    }


}
