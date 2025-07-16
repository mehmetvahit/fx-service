package com.example.fx.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Table(name = "conversion_record")
public class ConversionRecord {
    @Id
    private UUID id;

    private double amount;

    @Column(name = "from_currency")    // Avoid reserved word 'from'
    private String from;


    @Column(name = "to_currency")      // Avoid reserved word 'to'
    private String to;

    private double converted;

    @Column(name = "conversion_date")  // Avoid reserved word 'date'
    private LocalDate date;

    // Getters, setters, constructors...
}


