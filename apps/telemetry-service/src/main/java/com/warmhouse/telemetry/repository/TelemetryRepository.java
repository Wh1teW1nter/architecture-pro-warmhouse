package com.warmhouse.telemetry.repository;

import com.warmhouse.telemetry.entity.TelemetryRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface TelemetryRepository extends JpaRepository<TelemetryRecord, Long> {

    List<TelemetryRecord> findBySensorIdOrderByTimestampDesc(String sensorId, org.springframework.data.domain.Pageable pageable);

    List<TelemetryRecord> findByMetricTypeAndLocationOrderByTimestampDesc(String metricType, String location, org.springframework.data.domain.Pageable pageable);

    @Query("SELECT t FROM TelemetryRecord t WHERE t.sensorId = :sensorId AND t.metricType = 'temperature' ORDER BY t.timestamp DESC")
    List<TelemetryRecord> findLatestTemperatureBySensorId(@Param("sensorId") String sensorId, org.springframework.data.domain.Pageable pageable);

    @Query("SELECT t FROM TelemetryRecord t WHERE t.metricType = 'temperature' AND t.location = :location ORDER BY t.timestamp DESC")
    List<TelemetryRecord> findLatestTemperatureByLocation(@Param("location") String location, org.springframework.data.domain.Pageable pageable);

    List<TelemetryRecord> findBySensorIdAndTimestampBetweenOrderByTimestampAsc(String sensorId, Instant from, Instant to);
}
