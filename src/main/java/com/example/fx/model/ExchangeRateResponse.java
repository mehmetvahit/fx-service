package com.example.fx.model;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ExchangeRateResponse {
    private String from;
    private String to;
    private double rate;
}