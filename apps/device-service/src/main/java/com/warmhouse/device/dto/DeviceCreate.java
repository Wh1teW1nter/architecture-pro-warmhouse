package com.warmhouse.device.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class DeviceCreate {

    @NotBlank
    private String name;
    @NotBlank
    private String type;
    @NotBlank
    private String location;
    private String unit;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
}
