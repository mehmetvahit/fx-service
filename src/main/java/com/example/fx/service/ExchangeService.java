package com.example.fx.service;

import com.example.fx.model.*;
import com.example.fx.repo.ConversionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;


@Service
@RequiredArgsConstructor
public class ExchangeService {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ConversionRepository repo;
    private final ExchangeRateCacheService rateCacheService;
    private final CacheManager cacheManager; // Inject CacheManager to access the cache


    /**
     * Public method that fetches exchange rate and logs whether it's possibly from cache.
     */


    public ExchangeRateResponse getRateWithCaching(String from, String to) {
        String key = from + "_" + to;
        Cache cache = cacheManager.getCache("exchangeRates");

        if (cache != null) {
            ExchangeRateResponse cached = cache.get(key, ExchangeRateResponse.class);
            if (cached != null) {
                logger.info("Retrieved exchange rate from cache for {} -> {}: {}", from, to, cached.getRate());
                return cached;
            }
        }

        ExchangeRateResponse fresh = rateCacheService.getRate(from, to);
        if (fresh != null) {
            logger.info("Retrieved exchange rate from external API for {} -> {}: {}", from, to, fresh.getRate());
            return fresh;
        } else {
            logger.error("Failed to retrieve exchange rate from Fixer for {} -> {}", from, to);
            throw new IllegalStateException("Exchange rate could not be retrieved.");
        }
    }



    /**
     * Converts currency using a fetched exchange rate.
     */
    public ExchangeResponse convert(ExchangeRequest req) {
        logger.info("Executing method: convert {} {} -> {}", req.getAmount(), req.getFrom(), req.getTo());

        double rate = getRateWithCaching(req.getFrom(), req.getTo()).getRate();
        double converted = req.getAmount() * rate;

        ConversionRecord rec = new ConversionRecord(
                UUID.randomUUID(),
                req.getAmount(),
                req.getFrom(),
                req.getTo(),
                converted,
                LocalDate.now());

        repo.save(rec);
        logger.info("Conversion recorded with ID {}", rec.getId());

        return new ExchangeResponse(converted, rec.getId());
    }

    /**
     * Returns conversion history by transaction ID or date.
     */
    public List<ConversionRecord> history(UUID id, LocalDate date) {
        logger.info("Executing method: history (id: {}, date: {})", id, date);

        if (id != null) {
            return repo.findById(id).map(List::of).orElse(List.of());
        }

        if (date != null) {
            return repo.findByDate(date);
        }

        logger.warn("No id or date provided for history request");
        throw new IllegalArgumentException("Provide id or date");
    }
}
