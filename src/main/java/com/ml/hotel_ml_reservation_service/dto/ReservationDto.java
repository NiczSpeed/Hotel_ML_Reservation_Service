package com.ml.hotel_ml_reservation_service.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReservationDto {

    private String hotelCity;
    private String hotelName;
    private Long roomNumber;
    private String clientName;
    private LocalDate startDate;
    private LocalDate endDate;


}
