package e2e

import (
	"context"
	"errors"
	"fmt"
	"net/http"
	"strconv"
	"testing"
	"time"

	"google.golang.org/grpc"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/credentials/insecure"
	"google.golang.org/grpc/status"
	"google.golang.org/protobuf/types/known/timestamppb"

	heatingv1 "smarthome/gen/warmhouse/heating/v1"
	telemetryv1 "smarthome/gen/warmhouse/telemetry/v1"
)

// Events travel through Kafka, so the microservices catch up with the monolith eventually
const eventualTimeout = 30 * time.Second

var (
	heating   heatingv1.HeatingServiceClient
	telemetry telemetryv1.TelemetryServiceClient
)

func connectServices(s *stack) (func(), error) {
	heatingConn, err := grpc.NewClient(s.HeatingAddr, grpc.WithTransportCredentials(insecure.NewCredentials()))
	if err != nil {
		return nil, err
	}
	telemetryConn, err := grpc.NewClient(s.TelemetryAddr, grpc.WithTransportCredentials(insecure.NewCredentials()))
	if err != nil {
		heatingConn.Close()
		return nil, err
	}
	heating = heatingv1.NewHeatingServiceClient(heatingConn)
	telemetry = telemetryv1.NewTelemetryServiceClient(telemetryConn)
	return func() {
		heatingConn.Close()
		telemetryConn.Close()
	}, nil
}

// TestMigrationFlow checks what has moved from the monolith to the microservices:
// sensors are announced through device.lifecycle, readings come from Telemetry Service,
// heating settings are applied by Heating Service.
func TestMigrationFlow(t *testing.T) {
	created := createSensor(t, "Bedroom Temperature", "Bedroom")
	id := deviceID(created)

	t.Run("Create Sensor registers a thermostat in Heating Service", func(t *testing.T) {
		thermostat := eventually(t, func(ctx context.Context) (*heatingv1.Thermostat, error) {
			return heating.GetThermostat(ctx, &heatingv1.GetThermostatRequest{DeviceId: id})
		})
		if thermostat.GetHomeId() != defaultHomeID {
			t.Errorf("home_id = %q, want %q", thermostat.GetHomeId(), defaultHomeID)
		}
		if thermostat.GetMode() != heatingv1.HeatingMode_HEATING_MODE_OFF || thermostat.GetTargetTemperature() != 20 {
			t.Errorf("a new thermostat must be off with the default target: %+v", thermostat)
		}

		listed, err := heating.ListThermostats(context.Background(), &heatingv1.ListThermostatsRequest{HomeId: defaultHomeID})
		if err != nil {
			t.Fatalf("ListThermostats: %v", err)
		}
		if !containsThermostat(listed.GetItems(), id) {
			t.Errorf("thermostat %s is missing in ListThermostats", id)
		}
	})

	t.Run("readings are stored in Telemetry Service and served by the monolith", func(t *testing.T) {
		waitForReading(t, id)
		eventually(t, func(ctx context.Context) (*telemetryv1.ReadingSeries, error) {
			series, err := listReadings(ctx, id, created.CreatedAt)
			if err == nil && len(series.GetPoints()) < 3 {
				err = fmt.Errorf("only %d readings so far", len(series.GetPoints()))
			}
			return series, err
		})

		var got sensor
		doJSON(t, http.MethodGet, fmt.Sprintf("%s/api/v1/sensors/%d", env.AppURL, created.ID), nil, http.StatusOK, &got)
		assertTemperature(t, got.Value)
		if got.Status != "active" {
			t.Errorf("status = %q, want active", got.Status)
		}

		series, err := listReadings(context.Background(), id, created.CreatedAt)
		if err != nil {
			t.Fatalf("ListReadings: %v", err)
		}
		if series.GetUnit() != "celsius" {
			t.Errorf("unit = %q, want celsius", series.GetUnit())
		}
		found := false
		for _, p := range series.GetPoints() {
			if p.GetTime().AsTime().Equal(got.LastUpdated) && p.GetValue() == got.Value {
				found = true
				break
			}
		}
		if !found {
			t.Errorf("monolith returned %v at %s, which is not a Telemetry Service reading", got.Value, got.LastUpdated)
		}
	})

	t.Run("Telemetry Service records the thermostat going online", func(t *testing.T) {
		eventually(t, func(ctx context.Context) (*telemetryv1.ListAvailabilityResponse, error) {
			resp, err := telemetry.ListAvailability(ctx, &telemetryv1.ListAvailabilityRequest{
				DeviceId: id,
				From:     timestamppb.New(created.CreatedAt.Add(-time.Minute)),
			})
			if err == nil && (len(resp.GetItems()) == 0 || !resp.GetItems()[0].GetOnline()) {
				err = errors.New("no online event yet")
			}
			return resp, err
		})
	})

	t.Run("Update Sensor Value changes the thermostat in Heating Service", func(t *testing.T) {
		patchValue(t, created.ID, 22.5, "active", http.StatusOK)
		thermostat := getThermostat(t, id)
		if thermostat.GetTargetTemperature() != 22.5 || thermostat.GetMode() != heatingv1.HeatingMode_HEATING_MODE_MANUAL {
			t.Errorf("want target 22.5 in MANUAL, got %+v", thermostat)
		}

		patchValue(t, created.ID, 21, "off", http.StatusOK)
		thermostat = getThermostat(t, id)
		if thermostat.GetTargetTemperature() != 21 || thermostat.GetMode() != heatingv1.HeatingMode_HEATING_MODE_OFF {
			t.Errorf("want target 21 and OFF, got %+v", thermostat)
		}
		if thermostat.GetRelayOn() != nil && thermostat.GetRelayOn().GetValue() {
			t.Error("relay must be off when heating is off")
		}

		patchValue(t, created.ID, 50, "active", http.StatusBadRequest)
		if got := getThermostat(t, id).GetTargetTemperature(); got != 21 {
			t.Errorf("rejected target changed the thermostat: target = %v", got)
		}
	})

	t.Run("Delete Sensor unregisters the thermostat, history stays", func(t *testing.T) {
		doJSON(t, http.MethodDelete, fmt.Sprintf("%s/api/v1/sensors/%d", env.AppURL, created.ID), nil, http.StatusOK, nil)

		eventually(t, func(ctx context.Context) (struct{}, error) {
			_, err := heating.GetThermostat(ctx, &heatingv1.GetThermostatRequest{DeviceId: id})
			if status.Code(err) == codes.NotFound {
				return struct{}{}, nil
			}
			return struct{}{}, fmt.Errorf("thermostat is still there: %v", err)
		})

		if _, err := telemetry.GetLatestReadings(context.Background(), &telemetryv1.GetLatestReadingsRequest{DeviceId: id}); err != nil {
			t.Errorf("readings must outlive the device: %v", err)
		}
	})
}

func deviceID(s sensor) string {
	return strconv.Itoa(s.ID)
}

// waitForReading blocks until Telemetry Service has a temperature for the device
func waitForReading(t *testing.T, id string) {
	t.Helper()
	eventually(t, func(ctx context.Context) (*telemetryv1.GetLatestReadingsResponse, error) {
		return telemetry.GetLatestReadings(ctx, &telemetryv1.GetLatestReadingsRequest{DeviceId: id})
	})
}

func listReadings(ctx context.Context, id string, since time.Time) (*telemetryv1.ReadingSeries, error) {
	return telemetry.ListReadings(ctx, &telemetryv1.ListReadingsRequest{
		DeviceId: id,
		Metric:   "temperature",
		From:     timestamppb.New(since.Add(-time.Minute)),
	})
}

func getThermostat(t *testing.T, id string) *heatingv1.Thermostat {
	t.Helper()
	thermostat, err := heating.GetThermostat(context.Background(), &heatingv1.GetThermostatRequest{DeviceId: id})
	if err != nil {
		t.Fatalf("GetThermostat(%s): %v", id, err)
	}
	return thermostat
}

func patchValue(t *testing.T, id int, value float64, sensorStatus string, wantStatus int) {
	t.Helper()
	doJSON(t, http.MethodPatch, fmt.Sprintf("%s/api/v1/sensors/%d/value", env.AppURL, id),
		map[string]any{"value": value, "status": sensorStatus}, wantStatus, nil)
}

func containsThermostat(items []*heatingv1.Thermostat, id string) bool {
	for _, item := range items {
		if item.GetDeviceId() == id {
			return true
		}
	}
	return false
}

// eventually retries call until it succeeds or eventualTimeout passes
func eventually[T any](t *testing.T, call func(ctx context.Context) (T, error)) T {
	t.Helper()
	deadline := time.Now().Add(eventualTimeout)
	for {
		ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
		result, err := call(ctx)
		cancel()
		if err == nil {
			return result
		}
		if time.Now().After(deadline) {
			t.Fatalf("not reached within %s: %v", eventualTimeout, err)
		}
		time.Sleep(200 * time.Millisecond)
	}
}
