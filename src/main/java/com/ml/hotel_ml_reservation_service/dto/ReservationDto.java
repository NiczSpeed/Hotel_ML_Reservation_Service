package com.ml.hotel_ml_reservation_service.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReservationDto {

    private UUID uuid;
    private String hotelCity;
    private String hotelName;
    private Long roomNumber;
    private String clientEmail;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double amountPayable;

}
