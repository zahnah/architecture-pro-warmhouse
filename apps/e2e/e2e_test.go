package e2e

import (
	"bytes"
	"context"
	"encoding/json"
	"fmt"
	"log"
	"net/http"
	"net/url"
	"os"
	"testing"
	"time"
)

const (
	minTemperature = 18.0
	maxTemperature = 28.0
	pollCount      = 5
)

var (
	env    *stack
	client = &http.Client{Timeout: 10 * time.Second}
)

func TestMain(m *testing.M) {
	os.Exit(run(m))
}

func run(m *testing.M) int {
	ctx := context.Background()

	s, err := startStack(ctx)
	defer func() {
		if s == nil {
			return
		}
		if err := s.Terminate(ctx); err != nil {
			log.Printf("terminate stack: %v", err)
		}
	}()
	if err != nil {
		log.Printf("start stack: %v", err)
		return 1
	}
	env = s

	return m.Run()
}

type temperatureResponse struct {
	Value       float64   `json:"value"`
	Unit        string    `json:"unit"`
	Timestamp   time.Time `json:"timestamp"`
	Location    string    `json:"location"`
	Status      string    `json:"status"`
	SensorID    string    `json:"sensor_id"`
	SensorType  string    `json:"sensor_type"`
	Description string    `json:"description"`
}

type sensor struct {
	ID          int       `json:"id"`
	Name        string    `json:"name"`
	Type        string    `json:"type"`
	Location    string    `json:"location"`
	Value       float64   `json:"value"`
	Unit        string    `json:"unit"`
	Status      string    `json:"status"`
	LastUpdated time.Time `json:"last_updated"`
	CreatedAt   time.Time `json:"created_at"`
}

func TestTemperatureAPI(t *testing.T) {
	cases := []struct {
		name         string
		path         string
		query        url.Values
		wantLocation string
		wantSensorID string
	}{
		{"location Living Room", "/temperature", url.Values{"location": {"Living Room"}}, "Living Room", "1"},
		{"location Bedroom", "/temperature", url.Values{"location": {"Bedroom"}}, "Bedroom", "2"},
		{"location Kitchen", "/temperature", url.Values{"location": {"Kitchen"}}, "Kitchen", "3"},
		{"unknown location", "/temperature", url.Values{"location": {"Garage"}}, "Garage", "0"},
		{"sensorId query", "/temperature", url.Values{"sensorId": {"2"}}, "Bedroom", "2"},
		{"location and sensorId", "/temperature", url.Values{"location": {"Attic"}, "sensorId": {"42"}}, "Attic", "42"},
		{"sensor id path 1", "/temperature/1", nil, "Living Room", "1"},
		{"sensor id path 3", "/temperature/3", nil, "Kitchen", "3"},
		{"unknown sensor id path", "/temperature/99", nil, "Unknown", "99"},
		{"no parameters", "/temperature", nil, "Unknown", "0"},
	}

	for _, tc := range cases {
		t.Run(tc.name, func(t *testing.T) {
			endpoint := env.TemperatureURL + tc.path
			if len(tc.query) > 0 {
				endpoint += "?" + tc.query.Encode()
			}

			var got temperatureResponse
			doJSON(t, http.MethodGet, endpoint, nil, http.StatusOK, &got)

			if got.Location != tc.wantLocation {
				t.Errorf("location = %q, want %q", got.Location, tc.wantLocation)
			}
			if got.SensorID != tc.wantSensorID {
				t.Errorf("sensor_id = %q, want %q", got.SensorID, tc.wantSensorID)
			}
			assertTemperature(t, got.Value)
			if got.Unit != "°C" || got.Status != "active" || got.SensorType != "temperature" {
				t.Errorf("unexpected unit/status/sensor_type: %+v", got)
			}
			if got.Timestamp.IsZero() {
				t.Error("timestamp is empty")
			}
		})
	}
}

// TestSensorFlow repeats the reviewer's Postman scenario: Create Sensor, then Get All Sensors
// must show a fresh temperature from temperature-api on every call.
func TestSensorFlow(t *testing.T) {
	var created sensor
	doJSON(t, http.MethodPost, env.AppURL+"/api/v1/sensors", map[string]string{
		"name":     "Living Room Temperature",
		"type":     "temperature",
		"location": "Living Room",
		"unit":     "°C",
	}, http.StatusCreated, &created)
	t.Cleanup(func() {
		doJSON(t, http.MethodDelete, fmt.Sprintf("%s/api/v1/sensors/%d", env.AppURL, created.ID), nil, http.StatusOK, nil)
	})

	if created.ID <= 0 || created.Name != "Living Room Temperature" || created.Type != "temperature" ||
		created.Location != "Living Room" || created.Unit != "°C" {
		t.Fatalf("unexpected created sensor: %+v", created)
	}
	if created.Status != "inactive" || created.Value != 0 {
		t.Errorf("new sensor must be inactive with zero value, got %+v", created)
	}

	t.Run("Get All Sensors returns fresh temperature", func(t *testing.T) {
		values := make(map[float64]struct{})
		var lastUpdated time.Time
		for i := 0; i < pollCount; i++ {
			var sensors []sensor
			doJSON(t, http.MethodGet, env.AppURL+"/api/v1/sensors", nil, http.StatusOK, &sensors)

			got := findSensor(t, sensors, created.ID)
			assertTemperature(t, got.Value)
			if got.Status != "active" {
				t.Errorf("call %d: status = %q, want active", i, got.Status)
			}
			if !got.LastUpdated.After(lastUpdated) {
				t.Errorf("call %d: last_updated %s did not advance past %s", i, got.LastUpdated, lastUpdated)
			}
			lastUpdated = got.LastUpdated
			values[got.Value] = struct{}{}
		}
		if len(values) < 2 {
			t.Errorf("temperature did not change across %d calls: %v", pollCount, values)
		}
	})

	t.Run("Get Sensor by ID returns fresh temperature", func(t *testing.T) {
		var got sensor
		doJSON(t, http.MethodGet, fmt.Sprintf("%s/api/v1/sensors/%d", env.AppURL, created.ID), nil, http.StatusOK, &got)
		if got.ID != created.ID || got.Status != "active" {
			t.Errorf("unexpected sensor: %+v", got)
		}
		assertTemperature(t, got.Value)
	})

	t.Run("temperature by location", func(t *testing.T) {
		var got temperatureResponse
		doJSON(t, http.MethodGet, env.AppURL+"/api/v1/sensors/temperature/"+url.PathEscape("Kitchen"), nil, http.StatusOK, &got)
		if got.Location != "Kitchen" || got.Unit != "°C" || got.Status != "active" {
			t.Errorf("unexpected temperature: %+v", got)
		}
		assertTemperature(t, got.Value)
	})
}

func findSensor(t *testing.T, sensors []sensor, id int) sensor {
	t.Helper()
	for _, s := range sensors {
		if s.ID == id {
			return s
		}
	}
	t.Fatalf("sensor %d not found in %+v", id, sensors)
	return sensor{}
}

func assertTemperature(t *testing.T, value float64) {
	t.Helper()
	if value < minTemperature || value > maxTemperature {
		t.Errorf("temperature %v is outside [%v, %v]", value, minTemperature, maxTemperature)
	}
}

func doJSON(t *testing.T, method, endpoint string, body any, wantStatus int, out any) {
	t.Helper()

	var reqBody bytes.Buffer
	if body != nil {
		if err := json.NewEncoder(&reqBody).Encode(body); err != nil {
			t.Fatalf("encode request: %v", err)
		}
	}
	req, err := http.NewRequest(method, endpoint, &reqBody)
	if err != nil {
		t.Fatalf("build request: %v", err)
	}
	req.Header.Set("Content-Type", "application/json")

	resp, err := client.Do(req)
	if err != nil {
		t.Fatalf("%s %s: %v", method, endpoint, err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != wantStatus {
		t.Fatalf("%s %s: status %d, want %d", method, endpoint, resp.StatusCode, wantStatus)
	}
	if out != nil {
		if err := json.NewDecoder(resp.Body).Decode(out); err != nil {
			t.Fatalf("%s %s: decode response: %v", method, endpoint, err)
		}
	}
}
