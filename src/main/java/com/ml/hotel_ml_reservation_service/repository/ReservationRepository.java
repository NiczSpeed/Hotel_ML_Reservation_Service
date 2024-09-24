package com.ml.hotel_ml_reservation_service.repository;

import com.ml.hotel_ml_reservation_service.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;
import java.util.UUID;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, UUID> {
    Set<Reservation> findByHotelCity(String hotelCity);
    Set<Reservation> findByHotelName(String hotelName);
    Set<Reservation> findByRoomNumber(Long roomNumber);
    Set<Reservation> findReservationByHotelNameAndHotelCityAndRoomNumber(String hotelName, String hotelCity, Long roomNumber);
}
