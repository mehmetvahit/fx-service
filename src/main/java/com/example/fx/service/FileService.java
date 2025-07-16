package com.example.fx.service;

import com.example.fx.model.ExchangeRequest;
import com.example.fx.model.ExchangeResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileService {

    private static final Logger logger = LoggerFactory.getLogger(FileService.class);
    private final ExchangeService exchangeService;

    public List<ExchangeResponse> handle(MultipartFile file) {
        logger.info("Starting bulk file processing: {}", file.getOriginalFilename());

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {

            List<ExchangeRequest> validRequests = reader.lines()
                    .skip(1) // Skip header
                    .map(line -> {
                        String[] parts = line.split(",");
                        if (parts.length < 3) {
                            logger.warn("Skipping malformed line: {}", line);
                            return null;
                        }
                        try {
                            double amount = Double.parseDouble(parts[0].trim());
                            String from = parts[1].trim();
                            String to = parts[2].trim();
                            return new ExchangeRequest(amount, from, to);
                        } catch (Exception ex) {
                            logger.error("Error parsing line '{}': {}", line, ex.getMessage());
                            return null;
                        }
                    })
                    .filter(req -> req != null)
                    .collect(Collectors.toList());

            if (validRequests.isEmpty()) {
                logger.error(" No valid conversion requests found in uploaded file.");
                throw new RuntimeException("No valid conversion lines found.");
            }

            List<ExchangeResponse> results = validRequests.stream()
                    .map(exchangeService::convert)
                    .collect(Collectors.toList());

            logger.info("Successfully processed {} valid conversion requests.", results.size());
            return results;

        } catch (Exception e) {
            logger.error("Error reading or processing uploaded file: {}", e.getMessage(), e);
            throw new RuntimeException("File processing error", e);
        }
    }
}
