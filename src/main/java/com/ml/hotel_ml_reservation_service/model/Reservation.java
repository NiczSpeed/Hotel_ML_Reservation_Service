package com.ml.hotel_ml_reservation_service.model;

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
    private String hotelCity;
    @Column(name = "hotelName")
    private String hotelName;
    @Column(name = "roomNumber")
    private Long roomNumber;
    @Column(name = "clientEmail")
    private String clientEmail;
    @Column(name = "startDate")
    private LocalDate startDate;
    @Column(name = "endDate")
    private LocalDate endDate;
    @Column(name = "amountPayable")
    private Double amountPayable;

}
