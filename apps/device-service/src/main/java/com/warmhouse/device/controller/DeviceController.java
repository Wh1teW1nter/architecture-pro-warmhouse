package com.warmhouse.device.controller;

import com.warmhouse.device.dto.DeviceCreate;
import com.warmhouse.device.dto.DeviceUpdate;
import com.warmhouse.device.entity.Device;
import com.warmhouse.device.repository.DeviceRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/devices")
public class DeviceController {

    private final DeviceRepository repository;

    public DeviceController(DeviceRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Device> list(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String location) {
        if (type != null && !type.isBlank()) return repository.findByType(type);
        if (location != null && !location.isBlank()) return repository.findByLocation(location);
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Device> getById(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Device> create(@Valid @RequestBody DeviceCreate dto) {
        Device d = new Device();
        d.setName(dto.getName());
        d.setType(dto.getType());
        d.setLocation(dto.getLocation());
        d.setUnit(dto.getUnit());
        d.setStatus("inactive");
        d.setValue(0.0);
        Instant now = Instant.now();
        d.setLastUpdated(now);
        d.setCreatedAt(now);
        Device saved = repository.save(d);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Device> update(@PathVariable Long id, @RequestBody DeviceUpdate dto) {
        Optional<Device> opt = repository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        Device d = opt.get();
        if (dto.getName() != null) d.setName(dto.getName());
        if (dto.getType() != null) d.setType(dto.getType());
        if (dto.getLocation() != null) d.setLocation(dto.getLocation());
        if (dto.getValue() != null) d.setValue(dto.getValue());
        if (dto.getUnit() != null) d.setUnit(dto.getUnit());
        if (dto.getStatus() != null) d.setStatus(dto.getStatus());
        d.setLastUpdated(Instant.now());
        return ResponseEntity.ok(repository.save(d));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repository.existsById(id)) return ResponseEntity.notFound().build();
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
