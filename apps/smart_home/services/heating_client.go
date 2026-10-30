package services

import (
	"context"
	"fmt"

	"google.golang.org/grpc"

	heatingv1 "smarthome/gen/warmhouse/heating/v1"
)

// HeatingClient changes thermostat settings in Heating Service over gRPC
type HeatingClient struct {
	conn   *grpc.ClientConn
	client heatingv1.HeatingServiceClient
}

// NewHeatingClient creates a client for Heating Service at addr
func NewHeatingClient(addr string) (*HeatingClient, error) {
	conn, err := dialService(addr)
	if err != nil {
		return nil, fmt.Errorf("heating client: %w", err)
	}
	return &HeatingClient{conn: conn, client: heatingv1.NewHeatingServiceClient(conn)}, nil
}

// Apply sets the target temperature and, when off is true, switches heating off.
// The target is set first because setting it turns a switched-off thermostat on.
func (c *HeatingClient) Apply(ctx context.Context, deviceID string, target float64, off bool) (*heatingv1.Thermostat, error) {
	thermostat, err := c.client.SetTarget(ctx, &heatingv1.SetTargetRequest{DeviceId: deviceID, Temperature: target})
	if err != nil {
		return nil, err
	}
	if !off {
		return thermostat, nil
	}
	return c.client.SetMode(ctx, &heatingv1.SetModeRequest{DeviceId: deviceID, Mode: heatingv1.HeatingMode_HEATING_MODE_OFF})
}

// Close releases the connection
func (c *HeatingClient) Close() error {
	return c.conn.Close()
}
