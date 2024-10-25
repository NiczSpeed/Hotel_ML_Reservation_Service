package com.ml.hotel_ml_reservation_service.mapper;

import com.ml.hotel_ml_reservation_service.dto.ReservationDto;
import com.ml.hotel_ml_reservation_service.model.Reservation;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface ReservationMapper {

    ReservationMapper Instance = Mappers.getMapper(ReservationMapper.class);

    Reservation mapReservationDtoToReservation(ReservationDto reservationDto);
    ReservationDto mapReservationToReservationDto(Reservation reservation);

    List<ReservationDto> mapReservationListToReservationDtoList(List<Reservation> reservationList);
    List<Reservation> mapReservationDtoListToReservationList(List<ReservationDto> reservationDtoList);

}
