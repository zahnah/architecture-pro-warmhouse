package handlers

import (
	"context"
	"errors"
	"fmt"
	"log"
	"net/http"
	"strconv"
	"time"

	"smarthome/db"
	"smarthome/models"
	"smarthome/services"

	"github.com/gin-gonic/gin"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/status"
)

// Microservice calls must not hold up the legacy API for long
const microserviceTimeout = 2 * time.Second

// SensorHandler handles sensor-related requests.
// Temperature sensors are being moved to the microservices: their readings come from
// Telemetry Service and heating settings go to Heating Service. Nil clients keep the legacy behaviour.
type SensorHandler struct {
	DB                 *db.DB
	TemperatureService *services.TemperatureService
	Telemetry          *services.TelemetryClient
	Heating            *services.HeatingClient
	Devices            *services.DevicePublisher
}

// NewSensorHandler creates a new SensorHandler
func NewSensorHandler(db *db.DB, temperatureService *services.TemperatureService, telemetry *services.TelemetryClient,
	heating *services.HeatingClient, devices *services.DevicePublisher) *SensorHandler {
	return &SensorHandler{
		DB:                 db,
		TemperatureService: temperatureService,
		Telemetry:          telemetry,
		Heating:            heating,
		Devices:            devices,
	}
}

// RegisterRoutes registers the sensor routes
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

// GetSensors handles GET /api/v1/sensors
func (h *SensorHandler) GetSensors(c *gin.Context) {
	sensors, err := h.DB.GetSensors(context.Background())
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	for i := range sensors {
		if sensors[i].Type == models.Temperature {
			h.refreshTemperature(c.Request.Context(), &sensors[i])
		}
	}

	c.JSON(http.StatusOK, sensors)
}

// GetSensorByID handles GET /api/v1/sensors/:id
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
		h.refreshTemperature(c.Request.Context(), &sensor)
	}

	c.JSON(http.StatusOK, sensor)
}

// refreshTemperature puts the latest reading into the response: from Telemetry Service,
// or from temperature-api while Telemetry has nothing for the sensor
func (h *SensorHandler) refreshTemperature(ctx context.Context, sensor *models.Sensor) {
	deviceID := strconv.Itoa(sensor.ID)

	if h.Telemetry != nil {
		ctx, cancel := context.WithTimeout(ctx, microserviceTimeout)
		reading, err := h.Telemetry.LatestTemperature(ctx, deviceID)
		cancel()
		if err == nil {
			sensor.Value = reading.GetValue()
			sensor.Status = "active"
			sensor.LastUpdated = reading.GetTime().AsTime()
			return
		}
		log.Printf("No telemetry for sensor %d, falling back to temperature-api: %v", sensor.ID, err)
	}

	tempData, err := h.TemperatureService.GetTemperatureByID(deviceID)
	if err != nil {
		log.Printf("Failed to fetch temperature data for sensor %d: %v", sensor.ID, err)
		return
	}
	sensor.Value = tempData.Value
	sensor.Status = tempData.Status
	sensor.LastUpdated = tempData.Timestamp
}

// GetTemperatureByLocation handles GET /api/v1/sensors/temperature/:location
func (h *SensorHandler) GetTemperatureByLocation(c *gin.Context) {
	location := c.Param("location")
	if location == "" {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Location is required"})
		return
	}

	// Fetch temperature data from the external API
	tempData, err := h.TemperatureService.GetTemperature(location)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{
			"error": fmt.Sprintf("Failed to fetch temperature data: %v", err),
		})
		return
	}

	// Return the temperature data
	c.JSON(http.StatusOK, gin.H{
		"location":    tempData.Location,
		"value":       tempData.Value,
		"unit":        tempData.Unit,
		"status":      tempData.Status,
		"timestamp":   tempData.Timestamp,
		"description": tempData.Description,
	})
}

// CreateSensor handles POST /api/v1/sensors
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

	// Without an outbox a failed publish is only logged: the sensor works through the legacy path
	if h.Devices != nil && sensor.Type == models.Temperature {
		if err := h.Devices.PublishRegistered(c.Request.Context(), strconv.Itoa(sensor.ID)); err != nil {
			log.Printf("Failed to announce sensor %d to the microservices: %v", sensor.ID, err)
		}
	}

	c.JSON(http.StatusCreated, sensor)
}

// UpdateSensor handles PUT /api/v1/sensors/:id
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

// DeleteSensor handles DELETE /api/v1/sensors/:id
func (h *SensorHandler) DeleteSensor(c *gin.Context) {
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

	err = h.DB.DeleteSensor(context.Background(), id)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	if h.Devices != nil && sensor.Type == models.Temperature {
		if err := h.Devices.PublishUnregistered(c.Request.Context(), strconv.Itoa(id)); err != nil {
			log.Printf("Failed to unregister sensor %d in the microservices: %v", id, err)
		}
	}

	c.JSON(http.StatusOK, gin.H{"message": "Sensor deleted successfully"})
}

// UpdateSensorValue handles PATCH /api/v1/sensors/:id/value
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

	if h.Heating != nil {
		sensor, err := h.DB.GetSensorByID(context.Background(), id)
		if err != nil {
			c.JSON(http.StatusNotFound, gin.H{"error": "Sensor not found"})
			return
		}
		if sensor.Type == models.Temperature {
			ctx, cancel := context.WithTimeout(c.Request.Context(), microserviceTimeout)
			off := request.Status == "off" || request.Status == "inactive"
			_, err := h.Heating.Apply(ctx, strconv.Itoa(id), request.Value, off)
			cancel()
			if err != nil {
				c.JSON(heatingErrorStatus(err), gin.H{"error": fmt.Sprintf("Heating service: %v", status.Convert(err).Message())})
				return
			}
		}
	}

	// Kept in the monolith's table for clients that still read value/status from it
	err = h.DB.UpdateSensorValue(context.Background(), id, request.Value, request.Status)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, gin.H{"message": "Sensor value updated successfully"})
}

func heatingErrorStatus(err error) int {
	if errors.Is(err, context.DeadlineExceeded) {
		return http.StatusGatewayTimeout
	}
	switch status.Code(err) {
	case codes.InvalidArgument:
		return http.StatusBadRequest
	case codes.NotFound:
		// The thermostat appears in Heating Service shortly after device.lifecycle is consumed
		return http.StatusConflict
	case codes.DeadlineExceeded:
		return http.StatusGatewayTimeout
	default:
		return http.StatusBadGateway
	}
}
