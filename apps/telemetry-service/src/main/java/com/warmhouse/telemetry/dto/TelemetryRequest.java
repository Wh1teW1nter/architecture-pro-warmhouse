package com.warmhouse.telemetry.dto;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public class TelemetryRequest {

    private String deviceId;
    private String sensorId;
    @NotNull
    private String metricType;
    @NotNull
    private Double value;
    private String unit;
    private Instant timestamp;
    private String location;

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
}
