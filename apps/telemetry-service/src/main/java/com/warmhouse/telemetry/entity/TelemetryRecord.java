package com.warmhouse.telemetry.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "telemetry_data")
public class TelemetryRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id")
    private String deviceId;

    @Column(name = "sensor_id")
    private String sensorId;

    @Column(name = "metric_type", nullable = false)
    private String metricType;

    @Column(nullable = false)
    private Double value;

    private String unit;

    @Column(name = "ts", nullable = false)
    private Instant timestamp;

    @Column(name = "location")
    private String location;

    @Column(name = "status")
    private String status = "ok";

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    public String getSensorId() { return sensorId; }
    public void setSensorId(String sensorId) { this.sensorId = sensorId; }
    public String getMetricType() { return metricType; }
    public void setMetricType(String metricType) { this.metricType = metricType; }
    public Double getValue() { return value; }
    public void setValue(Double value) { this.value = value; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
