package services

import (
	"bytes"
	"encoding/json"
	"fmt"
	"net/http"
	"time"

	"smarthome/models"
)

type DeviceService struct {
	BaseURL    string
	HTTPClient *http.Client
}

type DeviceResponse struct {
	ID          int64     `json:"id"`
	Name        string    `json:"name"`
	Type        string    `json:"type"`
	Location    string    `json:"location"`
	Value       float64   `json:"value"`
	Unit        string    `json:"unit"`
	Status      string    `json:"status"`
	LastUpdated time.Time `json:"lastUpdated"`
	CreatedAt   time.Time `json:"createdAt"`
}

func NewDeviceService(baseURL string) *DeviceService {
	return &DeviceService{
		BaseURL: baseURL,
		HTTPClient: &http.Client{
			Timeout: 10 * time.Second,
		},
	}
}

func (s *DeviceService) GetDevices() ([]models.Sensor, error) {
	if s.BaseURL == "" {
		return nil, nil
	}
	resp, err := s.HTTPClient.Get(s.BaseURL + "/api/v1/devices")
	if err != nil {
		return nil, fmt.Errorf("device service request: %w", err)
	}
	defer resp.Body.Close()
	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("device service returned %d", resp.StatusCode)
	}
	var devices []DeviceResponse
	if err := json.NewDecoder(resp.Body).Decode(&devices); err != nil {
		return nil, err
	}
	out := make([]models.Sensor, len(devices))
	for i, d := range devices {
		out[i] = models.Sensor{
			ID:          int(d.ID),
			Name:        d.Name,
			Type:        models.SensorType(d.Type),
			Location:    d.Location,
			Value:       d.Value,
			Unit:        d.Unit,
			Status:      d.Status,
			LastUpdated: d.LastUpdated,
			CreatedAt:   d.CreatedAt,
		}
	}
	return out, nil
}

func (s *DeviceService) CreateDevice(sensor models.SensorCreate) error {
	if s.BaseURL == "" {
		return nil
	}
	body := map[string]string{
		"name":     sensor.Name,
		"type":     string(sensor.Type),
		"location": sensor.Location,
		"unit":     sensor.Unit,
	}
	jsonBody, _ := json.Marshal(body)
	req, err := http.NewRequest(http.MethodPost, s.BaseURL+"/api/v1/devices", bytes.NewReader(jsonBody))
	if err != nil {
		return err
	}
	req.Header.Set("Content-Type", "application/json")
	resp, err := s.HTTPClient.Do(req)
	if err != nil {
		return err
	}
	defer resp.Body.Close()
	if resp.StatusCode != http.StatusCreated && resp.StatusCode != http.StatusOK {
		return fmt.Errorf("device service create returned %d", resp.StatusCode)
	}
	return nil
}
