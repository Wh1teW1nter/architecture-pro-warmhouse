package com.warmhouse.device.repository;

import com.warmhouse.device.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeviceRepository extends JpaRepository<Device, Long> {

    List<Device> findByType(String type);

    List<Device> findByLocation(String location);
}
