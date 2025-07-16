package com.example.fx.repo;

import com.example.fx.model.ConversionRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ConversionRepository extends JpaRepository<ConversionRecord, UUID> {
    List<ConversionRecord> findByDate(LocalDate date);
}