package services

import (
	"context"
	"errors"
	"fmt"

	"google.golang.org/grpc"

	telemetryv1 "smarthome/gen/warmhouse/telemetry/v1"
)

const temperatureMetric = "temperature"

// ErrNoReading means Telemetry Service has no temperature for the device yet
var ErrNoReading = errors.New("no temperature reading")

// TelemetryClient reads device readings from Telemetry Service over gRPC
type TelemetryClient struct {
	conn   *grpc.ClientConn
	client telemetryv1.TelemetryServiceClient
}

// NewTelemetryClient creates a client for Telemetry Service at addr
func NewTelemetryClient(addr string) (*TelemetryClient, error) {
	conn, err := dialService(addr)
	if err != nil {
		return nil, fmt.Errorf("telemetry client: %w", err)
	}
	return &TelemetryClient{conn: conn, client: telemetryv1.NewTelemetryServiceClient(conn)}, nil
}

// LatestTemperature returns the last temperature reading of the device
func (c *TelemetryClient) LatestTemperature(ctx context.Context, deviceID string) (*telemetryv1.Reading, error) {
	resp, err := c.client.GetLatestReadings(ctx, &telemetryv1.GetLatestReadingsRequest{DeviceId: deviceID})
	if err != nil {
		return nil, err
	}
	for _, reading := range resp.GetItems() {
		if reading.GetMetric() == temperatureMetric {
			return reading, nil
		}
	}
	return nil, ErrNoReading
}

// Close releases the connection
func (c *TelemetryClient) Close() error {
	return c.conn.Close()
}
