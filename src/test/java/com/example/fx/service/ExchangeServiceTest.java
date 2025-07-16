package com.example.fx.service;

import com.example.fx.model.*;
import com.example.fx.repo.ConversionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExchangeServiceTest {

    @Mock
    private ConversionRepository repository;

    @Mock
    private ExchangeRateCacheService rateCacheService;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache cache;

    @InjectMocks
    private ExchangeService exchangeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        exchangeService = new ExchangeService(repository, rateCacheService, cacheManager);
    }

    @Test
    void testGetRateWithCaching_whenCached() {
        String from = "USD";
        String to = "EUR";
        String key = from + "_" + to;
        ExchangeRateResponse cachedRate = new ExchangeRateResponse(from, to, 0.9);

        when(cacheManager.getCache("exchangeRates")).thenReturn(cache);
        when(cache.get(key, ExchangeRateResponse.class)).thenReturn(cachedRate);

        ExchangeRateResponse result = exchangeService.getRateWithCaching(from, to);

        assertEquals(0.9, result.getRate());
        verifyNoInteractions(rateCacheService); // ✅ Cached, so no API call
    }

    @Test
    void testGetRateWithCaching_whenNotCached() {
        String from = "USD";
        String to = "EUR";
        String key = from + "_" + to;
        ExchangeRateResponse apiRate = new ExchangeRateResponse(from, to, 1.1);

        when(cacheManager.getCache("exchangeRates")).thenReturn(cache);
        when(cache.get(key, ExchangeRateResponse.class)).thenReturn(null); // ⛔ cache miss
        when(rateCacheService.getRate(from, to)).thenReturn(apiRate);

        ExchangeRateResponse result = exchangeService.getRateWithCaching(from, to);

        assertEquals(1.1, result.getRate());
        verify(rateCacheService).getRate(from, to); // ✅ Calls Fixer API
    }

    @Test
    void testConvert() {
        ExchangeRateResponse mockRate = new ExchangeRateResponse("USD", "EUR", 0.9);
        when(cacheManager.getCache("exchangeRates")).thenReturn(cache);
        when(cache.get("USD_EUR", ExchangeRateResponse.class)).thenReturn(mockRate);

        ExchangeRequest request = new ExchangeRequest(100, "USD", "EUR");
        ExchangeResponse responseObj = exchangeService.convert(request);

        assertNotNull(responseObj.getId());
        assertEquals(90.0, responseObj.getConverted(), 0.01);
        verify(repository).save(any(ConversionRecord.class));
    }

    @Test
    void testHistoryById() {
        UUID id = UUID.randomUUID();
        ConversionRecord rec = new ConversionRecord(id, 100, "USD", "EUR", 90, LocalDate.now());
        when(repository.findById(id)).thenReturn(Optional.of(rec));

        List<ConversionRecord> result = exchangeService.history(id, null);
        assertEquals(1, result.size());
        assertEquals(rec.getId(), result.get(0).getId());
    }

    @Test
    void testHistoryByDate() {
        LocalDate date = LocalDate.now();
        ConversionRecord rec = new ConversionRecord(UUID.randomUUID(), 100, "USD", "EUR", 90, date);
        when(repository.findByDate(date)).thenReturn(List.of(rec));

        List<ConversionRecord> result = exchangeService.history(null, date);
        assertEquals(1, result.size());
        assertEquals(date, result.get(0).getDate());
    }
}
