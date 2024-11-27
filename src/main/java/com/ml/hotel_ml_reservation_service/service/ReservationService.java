package com.ml.hotel_ml_reservation_service.service;

import com.ml.hotel_ml_reservation_service.dto.ReservationDto;
import com.ml.hotel_ml_reservation_service.exceptions.ErrorWhileEncodeException;
import com.ml.hotel_ml_reservation_service.exceptions.ReservationNotFoundException;
import com.ml.hotel_ml_reservation_service.mapper.ReservationMapper;
import com.ml.hotel_ml_reservation_service.model.Reservation;
import com.ml.hotel_ml_reservation_service.repository.ReservationRepository;
import com.ml.hotel_ml_reservation_service.utils.EncryptorUtil;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class ReservationService {

    Logger logger = Logger.getLogger(getClass().getName());

    private final Map<String, CompletableFuture<String>> responseFutures = new ConcurrentHashMap<>();

    private final ReservationRepository reservationRepository;
    private final EncryptorUtil encryptorUtil;
    private final KafkaTemplate kafkaTemplate;


    @KafkaListener(topics = "create_reservation_topic", groupId = "hotel_ml_reservation_service")
    private void createReservation(String message) throws Exception {
        String decodedMessage = encryptorUtil.decrypt(message);
        JSONObject json = new JSONObject(decodedMessage);
        JSONObject jsonMessage = json.getJSONObject("message");
        String messageId = json.optString("messageId");
        if (LocalDate.parse(jsonMessage.optString("startDate")).isBefore(LocalDate.now()) || LocalDate.parse(jsonMessage.optString("endDate")).isBefore(LocalDate.now())) {
            sendRequestMessage("Error:You are trying to pick a date from the past!", messageId, "error_request_topic");
            logger.severe("Error:You are trying to pick a date from the past!");
        } else {
            CompletableFuture<String> responseFuture = new CompletableFuture<>();
            String priceMessageId = UUID.randomUUID().toString();
            responseFutures.put(priceMessageId, responseFuture);
            sendEncodedMessage(jsonMessage.toString(), priceMessageId, "check_room_reservation_price_topic");
            logger.info("Check room reservation price:Message was sent.");
            try {
                String response = responseFuture.get(5, TimeUnit.SECONDS);
                responseFutures.remove(priceMessageId);
                JSONObject jsonPrice = new JSONObject(response);
                if (response.contains("Error")) {
                    sendRequestMessage(response, messageId, "error_request_topic");
                } else {
                    ReservationDto reservationDto = ReservationDto.builder()
                            .startDate(LocalDate.parse(jsonMessage.optString("startDate")))
                            .endDate(LocalDate.parse(jsonMessage.optString("endDate")))
                            .hotelCity(jsonMessage.optString("hotelCity"))
                            .hotelName(jsonMessage.optString("hotelName"))
                            .roomNumber(jsonMessage.optLong("roomNumber"))
                            .clientEmail(jsonMessage.optString("clientEmail"))
                            .amountPayable(jsonPrice.optDouble("message"))
                            .build();
                    Reservation reservation = ReservationMapper.Instance.mapReservationDtoToReservation(reservationDto);
                    reservationRepository.save(reservation);
                    sendEncodedMessage("Reservation was created!", messageId, "response_create_reservation_topic");
                    logger.info("Reservation was created:Message was sent.");
                }
            } catch (Exception e) {
                logger.severe("Error while creating reservation: " + e.getMessage());
            }
        }

    }

    @KafkaListener(topics = "check_reservation_topic", groupId = "hotel_ml_reservation_service")
    private void checkFreeRoom(String message) throws Exception {
        try {
            String decodedMessage = encryptorUtil.decrypt(message);
            JSONObject json = new JSONObject(decodedMessage);
            JSONObject jsonMessage = json.getJSONObject("message");
            String messageId = json.optString("messageId");
            jsonMessage.remove("messageId");
            LocalDate startDate = LocalDate.parse(jsonMessage.optString("startDate"));
            LocalDate endDate = LocalDate.parse(jsonMessage.optString("endDate"));
            Set<Reservation> checkReservations = reservationRepository.findReservationByHotelNameAndHotelCityAndRoomNumber(jsonMessage.optString("hotel"), jsonMessage.optString("city"), jsonMessage.optLong("room"));
            if (checkReservations.isEmpty() || isDateRangeAvailable(checkReservations, startDate, endDate)) {
                sendRequestMessage("True", messageId, "boolean_reservation_topic");
            } else {
                sendRequestMessage("False", messageId, "boolean_reservation_topic");
            }
        } catch (Exception e) {
            logger.severe("Error while checking reservation!: " + e.getMessage());
        }
    }

    @KafkaListener(topics = "all_user_reservation_request_topic", groupId = "hotel_ml_reservation_service")
    private void getAllUserReservations(String message) throws Exception {
        try {
            String decodedMessage = encryptorUtil.decrypt(message);
            JSONObject json = new JSONObject(decodedMessage);
            JSONObject jsonMessage = json.getJSONObject("message");
            String messageId = json.optString("messageId");
            List<Reservation> reservations = reservationRepository.findByClientEmail(jsonMessage.optString("email"));
            List<ReservationDto> reservationDtos = ReservationMapper.Instance.mapReservationListToReservationDtoList(reservations);
            if (reservationDtos.isEmpty()) {
                sendRequestMessage("Error:There is no reservations on your account!", messageId, "error_request_topic");
                logger.severe("Error:There is no reservations on your account!");
            } else {
                JSONArray jsonArray = new JSONArray(reservationDtos);
                sendEncodedMessage(jsonArray.toString(), messageId, "all_user_reservation_response_topic");
                logger.info("All user reservations on your account:Message was sent.");
            }
        } catch (Exception e) {
            logger.severe("Error while getting reservation list!: " + e.getMessage());
        }
    }

    @KafkaListener(topics = "update_reservation_topic", groupId = "hotel_ml_reservation_service")
    private void updateReservationByUuid(String message) throws Exception {
        try {
            String decodedMessage = encryptorUtil.decrypt(message);
            JSONObject json = new JSONObject(decodedMessage);
            JSONObject jsonMessage = json.getJSONObject("message");
            String messageId = json.optString("messageId");
            LocalDate startDate = LocalDate.parse(jsonMessage.optString("startDate"));
            LocalDate endDate = LocalDate.parse(jsonMessage.optString("endDate"));
            try {
                if (startDate.isBefore(LocalDate.now()) || endDate.isBefore(LocalDate.now())) {
                    sendRequestMessage("Error:You are trying to select a date from the past!", messageId, "error_request_topic");
                    logger.severe("Error:You are trying to select a date from the past!");
                }
                if (startDate.isAfter(endDate)) {
                    sendRequestMessage("Error:Starting date can't be after ending date!", messageId, "error_request_topic");
                    logger.severe("Error:Starting date can't be after ending date!");
                }
                Reservation reservation = reservationRepository.findByUuid(UUID.fromString(jsonMessage.optString("uuid"))).orElseThrow(ReservationNotFoundException::new);
                if (startDate.equals(reservation.getStartDate()) && endDate.equals(reservation.getEndDate())) {
                    sendRequestMessage("Error:Data has not changed!", messageId, "error_request_topic");
                    logger.severe("Error:Data has not changed!");
                }
                Set<Reservation> checkReservations = reservationRepository.findReservationByHotelNameAndHotelCityAndRoomNumber(reservation.getHotelName(), reservation.getHotelCity(), reservation.getRoomNumber());
                if (checkReservations.isEmpty() || isDateRangeAvailable(checkReservations, startDate, endDate) || checkReservations.contains(reservation)) {
                    CompletableFuture<String> responseFuture = new CompletableFuture<>();
                    String priceMessageId = UUID.randomUUID().toString();
                    responseFutures.put(priceMessageId, responseFuture);
                    JSONObject jsonPrice = new JSONObject().put("hotelName", reservation.getHotelName()).put("hotelCity", reservation.getHotelCity()).put("roomNumber", reservation.getRoomNumber()).put("startDate", startDate).put("endDate", endDate);
                    sendEncodedMessage(jsonPrice.toString(), priceMessageId, "check_room_reservation_price_topic");
                    logger.info("Check room reservation price:Message was sent.");
                    String response = responseFuture.get(5, TimeUnit.SECONDS);
                    responseFutures.remove(priceMessageId);
                    if (response.contains("Error")) {
                        sendRequestMessage(response, messageId, "error_request_topic");
                    } else {
                        jsonPrice.clear();
                        jsonPrice = new JSONObject(response);
                        reservation.setStartDate(LocalDate.parse(jsonMessage.optString("startDate")));
                        reservation.setEndDate(LocalDate.parse(jsonMessage.optString("endDate")));
                        reservation.setAmountPayable(jsonPrice.optDouble("message"));
                        reservationRepository.save(reservation);
                        sendRequestMessage("Reservation was successfully updated! ", messageId, "success_request_topic");
                        logger.info("Reservation was successfully updated:Message was sent.");
                    }
                } else {
                    sendRequestMessage("Error:There is no room on the date given!", messageId, "error_request_topic");
                    logger.severe("Error:There is no room on the date given!");
                }
            } catch (Exception e) {
                logger.severe("Error:Error while deleting reservation: " + e.getMessage());
                sendRequestMessage("Error:Reservation with this uuid does not exist!", messageId, "error_request_topic");
            }

        } catch (Exception e) {
            logger.severe("Error while creating hotel: " + e.getMessage());
        }
    }

    @KafkaListener(topics = "delete_reservation_topic", groupId = "hotel_ml_reservation_service")
    private void deleteReservationByUuid(String message) throws Exception {
        try {
            String decodedMessage = encryptorUtil.decrypt(message);
            JSONObject json = new JSONObject(decodedMessage);
            JSONObject jsonMessage = json.getJSONObject("message");
            String messageId = json.optString("messageId");
            try {
                Reservation reservation = reservationRepository.findByUuid(UUID.fromString(jsonMessage.optString("uuid"))).orElseThrow(ReservationNotFoundException::new);
                reservationRepository.delete(reservation);
                sendRequestMessage("Reservation was successfully deleted! ", messageId, "success_request_topic");
                logger.info("Reservation was successfully deleted:Message was sent.");
            } catch (Exception e) {
                sendRequestMessage("Error:Reservation with this uuid does not exist!", messageId, "error_request_topic");
                logger.severe("Error:Reservation with this uuid does not exist!");
            }
        } catch (Exception e) {
            logger.severe("Error while creating hotel: " + e.getMessage());
        }
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

    private String sendEncodedMessage(String message, String messageId, String topic) {
        try {
            JSONObject json = new JSONObject();
            json.put("messageId", messageId);
            if (message != null) {
                switch (message) {
                    case String s when s.contains("[") -> json.put("message", new JSONArray(s));
                    case String s when s.contains("{") -> json.put("message", new JSONObject(s));
                    default -> json.put("message", message);
                }
            }
            String encodedMessage = encryptorUtil.encrypt(json.toString());
            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, encodedMessage);
            future.whenComplete((result, exception) -> {
                if (exception != null) logger.severe(exception.getMessage());
                else logger.info("Message send successfully!");
            });
            return message;
        } catch (Exception e) {
            throw new ErrorWhileEncodeException();
        }
    }

    @KafkaListener(topics = "room_price_topic", groupId = "hotel_ml_reservation_service")
    public void earnReservationPrice(String message) {
        try {
            getRequestMessage(encryptorUtil.decrypt(message));
        } catch (Exception e) {
            throw new ErrorWhileEncodeException();
        }
    }

    private void getRequestMessage(String message) {
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
}
