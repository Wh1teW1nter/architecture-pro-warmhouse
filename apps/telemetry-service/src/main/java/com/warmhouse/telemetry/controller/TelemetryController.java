package com.warmhouse.telemetry.controller;

import com.warmhouse.telemetry.dto.TelemetryRequest;
import com.warmhouse.telemetry.entity.TelemetryRecord;
import com.warmhouse.telemetry.repository.TelemetryRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/telemetry")
public class TelemetryController {

    private final TelemetryRepository repository;

    public TelemetryController(TelemetryRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public ResponseEntity<TelemetryRecord> ingest(@Valid @RequestBody TelemetryRequest request) {
        TelemetryRecord record = new TelemetryRecord();
        record.setDeviceId(request.getDeviceId());
        record.setSensorId(request.getSensorId());
        record.setMetricType(request.getMetricType());
        record.setValue(request.getValue());
        record.setUnit(request.getUnit() != null ? request.getUnit() : "C");
        record.setTimestamp(request.getTimestamp() != null ? request.getTimestamp() : Instant.now());
        record.setLocation(request.getLocation());
        record.setStatus("ok");
        TelemetryRecord saved = repository.save(record);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/current/{sensorId}")
    public ResponseEntity<TelemetryRecord> getCurrent(@PathVariable String sensorId) {
        return repository.findBySensorIdOrderByTimestampDesc(sensorId, org.springframework.data.domain.PageRequest.of(0, 1))
                .stream()
                .findFirst()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<TelemetryRecord> query(
            @RequestParam(required = false) String sensorId,
            @RequestParam(required = false) String metricType,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @RequestParam(defaultValue = "100") int limit) {
        if (sensorId != null && from != null && to != null) {
            return repository.findBySensorIdAndTimestampBetweenOrderByTimestampAsc(sensorId, from, to);
        }
        if (sensorId != null) {
            return repository.findBySensorIdOrderByTimestampDesc(sensorId, org.springframework.data.domain.PageRequest.of(0, limit));
        }
        return repository.findAll(org.springframework.data.domain.PageRequest.of(0, limit)).getContent();
    }
}
