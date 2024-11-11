package com.ml.hotel_ml_reservation_service.model;

import com.ml.hotel_ml_reservation_service.utils.converters.DoubleConverter;
import com.ml.hotel_ml_reservation_service.utils.converters.LocalDateConverter;
import com.ml.hotel_ml_reservation_service.utils.converters.StringConverter;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Table(name = "Reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "uuid")
    private UUID uuid;

    @Column(name = "hotelCity")
    @Convert(converter = StringConverter.class)
    private String hotelCity;

    @Column(name = "hotelName")
    @Convert(converter = StringConverter.class)
    private String hotelName;

    @Column(name = "roomNumber")
    private Long roomNumber;

    @Column(name = "clientEmail")
    @Convert(converter = StringConverter.class)
    private String clientEmail;

    @Column(name = "startDate")
    @Convert(converter = LocalDateConverter.class)
    private LocalDate startDate;

    @Column(name = "endDate")
    @Convert(converter = LocalDateConverter.class)
    private LocalDate endDate;

    @Column(name = "amountPayable")
    @Convert(converter = DoubleConverter.class)
    private Double amountPayable;

}
