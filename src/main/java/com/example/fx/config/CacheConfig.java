package com.example.fx.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.*;

import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        Caffeine<Object, Object> caffeine = Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.MINUTES)   // 🔁 expires 1 min after write
                .maximumSize(1000);                        // optional LRU size

        CaffeineCacheManager manager = new CaffeineCacheManager("exchangeRates");
        manager.setCaffeine(caffeine);
        return manager;
    }
}
