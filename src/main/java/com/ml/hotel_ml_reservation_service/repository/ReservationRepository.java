package com.ml.hotel_ml_reservation_service.repository;

import com.ml.hotel_ml_reservation_service.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, UUID> {
    Optional<Reservation> findByUuid(UUID id);
    List<Reservation> findByClientEmail(String clientEmail);
    Set<Reservation> findReservationByHotelNameAndHotelCityAndRoomNumber(String hotelName, String hotelCity, Long roomNumber);
}
