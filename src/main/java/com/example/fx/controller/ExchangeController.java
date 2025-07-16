package com.example.fx.controller;

import com.example.fx.model.*;
import com.example.fx.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ExchangeController {

    private static final Logger logger = LoggerFactory.getLogger(ExchangeController.class);

    private final ExchangeService exchangeService;
    private final FileService fileService;

    @GetMapping("/rate")
    public ResponseEntity<ExchangeRateResponse> getRate(@RequestParam String from, @RequestParam String to) {
        logger.info("Executing method: getRate");
        return ResponseEntity.ok(exchangeService.getRateWithCaching(from, to));
    }

    @PostMapping("/convert")
    public ResponseEntity<ExchangeResponse> convert(@RequestBody ExchangeRequest request) {
        logger.info("Executing method: convert");
        return ResponseEntity.ok(exchangeService.convert(request));
    }

    @GetMapping("/history")
    public ResponseEntity<List<ConversionRecord>> history(
            @RequestParam(required = false) UUID id,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        logger.info("Executing method: history");
        return ResponseEntity.ok(exchangeService.history(id, date));
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<ExchangeResponse>> bulk(@RequestParam MultipartFile file) {
        logger.info("Executing method: bulk");
        return ResponseEntity.ok(fileService.handle(file));
    }
}
