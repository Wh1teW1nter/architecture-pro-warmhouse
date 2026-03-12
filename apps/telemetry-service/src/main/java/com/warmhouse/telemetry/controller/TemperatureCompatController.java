package com.warmhouse.telemetry.controller;

import com.warmhouse.telemetry.entity.TelemetryRecord;
import com.warmhouse.telemetry.repository.TelemetryRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
public class TemperatureCompatController {

    private final TelemetryRepository repository;

    public TemperatureCompatController(TelemetryRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/temperature")
    public ResponseEntity<Map<String, Object>> getByLocation(@RequestParam String location) {
        return repository.findLatestTemperatureByLocation(location, org.springframework.data.domain.PageRequest.of(0, 1))
                .stream()
                .findFirst()
                .map(this::toTemperatureResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/temperature/{sensorId}")
    public ResponseEntity<Map<String, Object>> getBySensorId(@PathVariable String sensorId) {
        return repository.findLatestTemperatureBySensorId(sensorId, org.springframework.data.domain.PageRequest.of(0, 1))
                .stream()
                .findFirst()
                .map(this::toTemperatureResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private Map<String, Object> toTemperatureResponse(TelemetryRecord t) {
        Map<String, Object> m = new HashMap<>();
        m.put("value", t.getValue());
        m.put("unit", t.getUnit() != null ? t.getUnit() : "C");
        m.put("timestamp", t.getTimestamp());
        m.put("location", t.getLocation() != null ? t.getLocation() : "");
        m.put("status", t.getStatus() != null ? t.getStatus() : "ok");
        m.put("sensor_id", t.getSensorId());
        m.put("sensor_type", "temperature");
        m.put("description", "Temperature from telemetry service");
        return m;
    }
}
