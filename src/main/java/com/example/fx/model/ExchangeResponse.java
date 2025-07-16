package com.example.fx.model;

import lombok.*;

import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ExchangeResponse {
    private double converted;
    private UUID id;
}