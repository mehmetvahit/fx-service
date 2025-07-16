package com.example.fx.service;

import com.example.fx.model.ExchangeRateResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExchangeRateCacheService {

    private static final Logger logger = LoggerFactory.getLogger(ExchangeRateCacheService.class);

    private final RestTemplate restTemplate;

    @Value("${fixer.url}")
    private String fixerUrl;

    @Value("${fixer.key}")
    private String fixerKey;

    /**
     * Fetches the exchange rate from Fixer.io and caches it.
     *
     * @param from Source currency code
     * @param to   Target currency code
     * @return Exchange rate response
     */
    @Cacheable(value = "exchangeRates", key = "#from + '_' + #to")
    public ExchangeRateResponse getRate(String from, String to) {
        logger.info("Calling Fixer API for {} -> {}", from, to);

        String url = String.format("%s?base=%s&symbols=%s", fixerUrl, from, to);

        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", fixerKey);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            Map body = response.getBody();

            if (body == null || !body.containsKey("rates")) {
                logger.error("Missing 'rates' in Fixer response for {} -> {}", from, to);
                throw new IllegalStateException("Invalid response from FX provider");
            }

            Map rates = (Map) body.get("rates");
            Object rateObj = rates.get(to);

            if (rateObj == null) {
                logger.error("Rate not found in Fixer response for {} -> {}", from, to);
                throw new IllegalArgumentException("Rate not found for " + to);
            }

            double rate = Double.parseDouble(rateObj.toString());
            logger.info("Fixer rate retrieved for {} -> {}: {}", from, to, rate);
            return new ExchangeRateResponse(from, to, rate);

        } catch (RestClientException e) {
            logger.error("Error calling Fixer API: {}", e.getMessage(), e);
            throw new RuntimeException("External FX service error");
        } catch (Exception e) {
            logger.error("Unexpected error in getRate: {}", e.getMessage(), e);
            throw e;
        }
    }
}
