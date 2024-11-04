package com.ml.hotel_ml_reservation_service.model;

import com.ml.hotel_ml_reservation_service.utils.Encryptor;
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
    @Convert(converter = Encryptor.class)
    private String hotelCity;
    @Column(name = "hotelName")
    @Convert(converter = Encryptor.class)
    private String hotelName;
    @Column(name = "roomNumber")
    private Long roomNumber;
    @Column(name = "clientEmail")
    @Convert(converter = Encryptor.class)
    private String clientEmail;
    @Column(name = "startDate")
    @Convert(converter = Encryptor.class)
    private LocalDate startDate;
    @Convert(converter = Encryptor.class)
    @Column(name = "endDate")
    private LocalDate endDate;
    @Column(name = "amountPayable")
    @Convert(converter = Encryptor.class)
    private Double amountPayable;

}
