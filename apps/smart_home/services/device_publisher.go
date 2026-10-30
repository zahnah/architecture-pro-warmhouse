package services

import (
	"context"
	"fmt"
	"time"

	"github.com/google/uuid"
	"github.com/segmentio/kafka-go"
	"google.golang.org/protobuf/proto"
	"google.golang.org/protobuf/types/known/timestamppb"

	eventsv1 "smarthome/gen/warmhouse/events/v1"
)

const (
	deviceLifecycleTopic = "device.lifecycle"
	eventTypeHeader      = "eventType"
	// The monolith's temperature sensors are thermostats of the heating Device Service
	heatingDeviceType = "heating"
)

// DevicePublisher announces sensors to the microservices through device.lifecycle,
// playing the part of Platform Service until it is extracted from the monolith
type DevicePublisher struct {
	writer *kafka.Writer
	homeID string
}

// NewDevicePublisher creates a publisher; all sensors belong to homeID
func NewDevicePublisher(brokers []string, homeID string) *DevicePublisher {
	return &DevicePublisher{
		writer: &kafka.Writer{
			Addr:  kafka.TCP(brokers...),
			Topic: deviceLifecycleTopic,
			// Same partitioning as the Java client, so a device's events stay in one partition
			Balancer:     &kafka.Murmur2Balancer{},
			RequiredAcks: kafka.RequireAll,
			BatchTimeout: 10 * time.Millisecond,
		},
		homeID: homeID,
	}
}

// PublishRegistered sends DeviceRegistered for a new sensor
func (p *DevicePublisher) PublishRegistered(ctx context.Context, deviceID string) error {
	return p.publish(ctx, deviceID, &eventsv1.DeviceRegistered{
		EventId:    uuid.NewString(),
		OccurredAt: timestamppb.Now(),
		DeviceId:   deviceID,
		HomeId:     p.homeID,
		Type:       heatingDeviceType,
	})
}

// PublishUnregistered sends DeviceUnregistered for a deleted sensor
func (p *DevicePublisher) PublishUnregistered(ctx context.Context, deviceID string) error {
	return p.publish(ctx, deviceID, &eventsv1.DeviceUnregistered{
		EventId:    uuid.NewString(),
		OccurredAt: timestamppb.Now(),
		DeviceId:   deviceID,
		HomeId:     p.homeID,
	})
}

func (p *DevicePublisher) publish(ctx context.Context, deviceID string, event proto.Message) error {
	value, err := proto.Marshal(event)
	if err != nil {
		return fmt.Errorf("marshal event: %w", err)
	}
	eventType := string(event.ProtoReflect().Descriptor().Name())
	err = p.writer.WriteMessages(ctx, kafka.Message{
		Key:     []byte(deviceID),
		Value:   value,
		Headers: []kafka.Header{{Key: eventTypeHeader, Value: []byte(eventType)}},
	})
	if err != nil {
		return fmt.Errorf("publish %s: %w", eventType, err)
	}
	return nil
}

// Close flushes pending messages
func (p *DevicePublisher) Close() error {
	return p.writer.Close()
}
