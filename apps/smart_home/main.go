package main

import (
	"context"
	"log"
	"net/http"
	"os"
	"os/signal"
	"strings"
	"syscall"
	"time"

	"smarthome/db"
	"smarthome/handlers"
	"smarthome/services"

	"github.com/gin-gonic/gin"
)

func main() {
	// Set up database connection
	dbURL := getEnv("DATABASE_URL", "postgres://postgres:postgres@localhost:5432/smarthome")
	database, err := db.New(dbURL)
	if err != nil {
		log.Fatalf("Unable to connect to database: %v\n", err)
	}
	defer database.Close()

	log.Println("Connected to database successfully")

	// Initialize temperature service
	temperatureAPIURL := getEnv("TEMPERATURE_API_URL", "http://temperature-api:8081")
	temperatureService := services.NewTemperatureService(temperatureAPIURL)
	log.Printf("Temperature service initialized with API URL: %s\n", temperatureAPIURL)

	// Microservices that take over temperature sensors; an empty address keeps the legacy path
	var telemetryClient *services.TelemetryClient
	if addr := os.Getenv("TELEMETRY_GRPC_ADDR"); addr != "" {
		telemetryClient, err = services.NewTelemetryClient(addr)
		if err != nil {
			log.Fatalf("Unable to create telemetry client: %v\n", err)
		}
		defer telemetryClient.Close()
		log.Printf("Temperature readings are taken from Telemetry Service at %s\n", addr)
	}

	var heatingClient *services.HeatingClient
	if addr := os.Getenv("HEATING_GRPC_ADDR"); addr != "" {
		heatingClient, err = services.NewHeatingClient(addr)
		if err != nil {
			log.Fatalf("Unable to create heating client: %v\n", err)
		}
		defer heatingClient.Close()
		log.Printf("Heating settings are sent to Heating Service at %s\n", addr)
	}

	var devicePublisher *services.DevicePublisher
	if brokers := os.Getenv("KAFKA_BROKERS"); brokers != "" {
		homeID := getEnv("DEFAULT_HOME_ID", "00000000-0000-0000-0000-000000000001")
		devicePublisher = services.NewDevicePublisher(strings.Split(brokers, ","), homeID)
		defer devicePublisher.Close()
		log.Printf("Sensors are published to device.lifecycle at %s for home %s\n", brokers, homeID)
	}

	// Initialize router
	router := gin.Default()

	// Health check endpoint
	router.GET("/health", func(c *gin.Context) {
		c.JSON(http.StatusOK, gin.H{
			"status": "ok",
		})
	})

	// API routes
	apiRoutes := router.Group("/api/v1")

	// Register sensor routes
	sensorHandler := handlers.NewSensorHandler(database, temperatureService, telemetryClient, heatingClient, devicePublisher)
	sensorHandler.RegisterRoutes(apiRoutes)

	// Start server
	srv := &http.Server{
		Addr:    getEnv("PORT", ":8080"),
		Handler: router,
	}

	// Start the server in a goroutine
	go func() {
		log.Printf("Server starting on %s\n", srv.Addr)
		if err := srv.ListenAndServe(); err != nil && err != http.ErrServerClosed {
			log.Fatalf("Failed to start server: %v\n", err)
		}
	}()

	// Wait for interrupt signal to gracefully shut down the server
	quit := make(chan os.Signal, 1)
	signal.Notify(quit, syscall.SIGINT, syscall.SIGTERM)
	<-quit
	log.Println("Shutting down server...")

	// Create a deadline for server shutdown
	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()
	if err := srv.Shutdown(ctx); err != nil {
		log.Fatalf("Server forced to shutdown: %v\n", err)
	}

	log.Println("Server exited properly")
}

// getEnv gets an environment variable or returns a default value
func getEnv(key, defaultValue string) string {
	value := os.Getenv(key)
	if value == "" {
		return defaultValue
	}
	return value
}
