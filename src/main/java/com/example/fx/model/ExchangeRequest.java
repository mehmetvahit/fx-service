package com.example.fx.model;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ExchangeRequest {
    private double amount;
    private String from;
    private String to;
}