package handlers

import (
	"context"
	"fmt"
	"log"
	"net/http"
	"strconv"

	"smarthome/db"
	"smarthome/models"
	"smarthome/services"

	"github.com/gin-gonic/gin"
)

type SensorHandler struct {
	DB                 *db.DB
	TemperatureService *services.TemperatureService
	DeviceService      *services.DeviceService
}

func NewSensorHandler(db *db.DB, temperatureService *services.TemperatureService, deviceService *services.DeviceService) *SensorHandler {
	return &SensorHandler{
		DB:                 db,
		TemperatureService: temperatureService,
		DeviceService:      deviceService,
	}
}

func (h *SensorHandler) RegisterRoutes(router *gin.RouterGroup) {
	sensors := router.Group("/sensors")
	{
		sensors.GET("", h.GetSensors)
		sensors.GET("/:id", h.GetSensorByID)
		sensors.POST("", h.CreateSensor)
		sensors.PUT("/:id", h.UpdateSensor)
		sensors.DELETE("/:id", h.DeleteSensor)
		sensors.PATCH("/:id/value", h.UpdateSensorValue)
		sensors.GET("/temperature/:location", h.GetTemperatureByLocation)
	}
}

func (h *SensorHandler) GetSensors(c *gin.Context) {
	var sensors []models.Sensor
	useDeviceService := false

	if h.DeviceService != nil {
		fromMS, err := h.DeviceService.GetDevices()
		if err == nil {
			sensors = fromMS
			useDeviceService = true
		} else {
			log.Printf("Device service unavailable, falling back to local DB: %v", err)
		}
	}
	if !useDeviceService {
		var err error
		sensors, err = h.DB.GetSensors(context.Background())
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
			return
		}
	}

	for i, sensor := range sensors {
		if sensor.Type == models.Temperature {
			tempData, err := h.TemperatureService.GetTemperatureByID(fmt.Sprintf("%d", sensor.ID))
			if err == nil {
				sensors[i].Value = tempData.Value
				sensors[i].Status = tempData.Status
				sensors[i].LastUpdated = tempData.Timestamp
				log.Printf("Updated temperature data for sensor %d from Telemetry service", sensor.ID)
			} else {
				log.Printf("Failed to fetch temperature data for sensor %d: %v", sensor.ID, err)
			}
		}
	}

	c.JSON(http.StatusOK, sensors)
}

func (h *SensorHandler) GetSensorByID(c *gin.Context) {
	id, err := strconv.Atoi(c.Param("id"))
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid sensor ID"})
		return
	}

	sensor, err := h.DB.GetSensorByID(context.Background(), id)
	if err != nil {
		c.JSON(http.StatusNotFound, gin.H{"error": "Sensor not found"})
		return
	}

	if sensor.Type == models.Temperature {
		tempData, err := h.TemperatureService.GetTemperatureByID(fmt.Sprintf("%d", sensor.ID))
		if err == nil {
			sensor.Value = tempData.Value
			sensor.Status = tempData.Status
			sensor.LastUpdated = tempData.Timestamp
			log.Printf("Updated temperature data for sensor %d from external API", sensor.ID)
		} else {
			log.Printf("Failed to fetch temperature data for sensor %d: %v", sensor.ID, err)
		}
	}

	c.JSON(http.StatusOK, sensor)
}

func (h *SensorHandler) GetTemperatureByLocation(c *gin.Context) {
	location := c.Param("location")
	if location == "" {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Location is required"})
		return
	}

	tempData, err := h.TemperatureService.GetTemperature(location)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{
			"error": fmt.Sprintf("Failed to fetch temperature data: %v", err),
		})
		return
	}

	c.JSON(http.StatusOK, gin.H{
		"location":    tempData.Location,
		"value":       tempData.Value,
		"unit":        tempData.Unit,
		"status":      tempData.Status,
		"timestamp":   tempData.Timestamp,
		"description": tempData.Description,
	})
}

func (h *SensorHandler) CreateSensor(c *gin.Context) {
	var sensorCreate models.SensorCreate
	if err := c.ShouldBindJSON(&sensorCreate); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	sensor, err := h.DB.CreateSensor(context.Background(), sensorCreate)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	if h.DeviceService != nil {
		if err := h.DeviceService.CreateDevice(sensorCreate); err != nil {
			log.Printf("Device service create (dual-write) failed: %v", err)
		}
	}

	c.JSON(http.StatusCreated, sensor)
}

func (h *SensorHandler) UpdateSensor(c *gin.Context) {
	id, err := strconv.Atoi(c.Param("id"))
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid sensor ID"})
		return
	}

	var sensorUpdate models.SensorUpdate
	if err := c.ShouldBindJSON(&sensorUpdate); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	sensor, err := h.DB.UpdateSensor(context.Background(), id, sensorUpdate)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, sensor)
}

func (h *SensorHandler) DeleteSensor(c *gin.Context) {
	id, err := strconv.Atoi(c.Param("id"))
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid sensor ID"})
		return
	}

	err = h.DB.DeleteSensor(context.Background(), id)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, gin.H{"message": "Sensor deleted successfully"})
}

func (h *SensorHandler) UpdateSensorValue(c *gin.Context) {
	id, err := strconv.Atoi(c.Param("id"))
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid sensor ID"})
		return
	}

	var request struct {
		Value  float64 `json:"value" binding:"required"`
		Status string  `json:"status" binding:"required"`
	}

	if err := c.ShouldBindJSON(&request); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	err = h.DB.UpdateSensorValue(context.Background(), id, request.Value, request.Status)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, gin.H{"message": "Sensor value updated successfully"})
}
