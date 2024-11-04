package com.ml.hotel_ml_reservation_service.model;

import com.ml.hotel_ml_reservation_service.utils.converters.DoubleEncryptor;
import com.ml.hotel_ml_reservation_service.utils.converters.LocalDateEncryptor;
import com.ml.hotel_ml_reservation_service.utils.converters.StringEncryptor;
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
    @Convert(converter = StringEncryptor.class)
    private String hotelCity;

    @Column(name = "hotelName")
    @Convert(converter = StringEncryptor.class)
    private String hotelName;

    @Column(name = "roomNumber")
    private Long roomNumber;

    @Column(name = "clientEmail")
    @Convert(converter = StringEncryptor.class)
    private String clientEmail;

    @Column(name = "startDate")
    @Convert(converter = LocalDateEncryptor.class)
    private LocalDate startDate;

    @Column(name = "endDate")
    @Convert(converter = LocalDateEncryptor.class)
    private LocalDate endDate;

    @Column(name = "amountPayable")
    @Convert(converter = DoubleEncryptor.class)
    private Double amountPayable;

}
