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
    @Column(name = "clientName")
    private String clientName;
    @Column(name = "startDate")
    private LocalDate startDate;
    @Column(name = "endDate")
    private LocalDate endDate;

}
