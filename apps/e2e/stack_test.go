package e2e

import (
	"context"
	"errors"
	"fmt"
	"io"
	"path/filepath"
	"time"

	"github.com/testcontainers/testcontainers-go"
	"github.com/testcontainers/testcontainers-go/network"
	"github.com/testcontainers/testcontainers-go/wait"
)

// appsDir is the apps/ directory with the services under test, relative to this package
const appsDir = ".."

const (
	postgresImage = "timescale/timescaledb:2.31.0-pg16"
	kafkaImage    = "apache/kafka:4.3.2"
	defaultHomeID = "00000000-0000-0000-0000-000000000001"
	// Faster than in docker-compose.yml to keep the tests short
	devicePollInterval = 500 * time.Millisecond
)

// stack is the smart_home monolith with the microservices and their dependencies,
// started the same way as apps/docker-compose.yml
type stack struct {
	network    *testcontainers.DockerNetwork
	containers []testcontainers.Container

	AppURL         string
	TemperatureURL string
	HeatingAddr    string
	TelemetryAddr  string
}

func startStack(ctx context.Context) (*stack, error) {
	nw, err := network.New(ctx)
	if err != nil {
		return nil, fmt.Errorf("create network: %w", err)
	}
	s := &stack{network: nw}

	if _, err := s.start(ctx, postgresRequest(nw.Name)); err != nil {
		return s, fmt.Errorf("start postgres: %w", err)
	}

	kafka, err := s.start(ctx, kafkaRequest(nw.Name))
	if err != nil {
		return s, fmt.Errorf("start kafka: %w", err)
	}
	if err := createTopics(ctx, kafka); err != nil {
		return s, err
	}

	temperatureAPI, err := s.start(ctx, temperatureAPIRequest(nw.Name))
	if err != nil {
		return s, fmt.Errorf("start temperature-api: %w", err)
	}
	if s.TemperatureURL, err = temperatureAPI.PortEndpoint(ctx, "8081/tcp", "http"); err != nil {
		return s, err
	}

	heating, err := s.start(ctx, heatingRequest(nw.Name))
	if err != nil {
		return s, fmt.Errorf("start heating-service: %w", err)
	}
	if s.HeatingAddr, err = heating.PortEndpoint(ctx, "9090/tcp", ""); err != nil {
		return s, err
	}

	telemetry, err := s.start(ctx, telemetryRequest(nw.Name))
	if err != nil {
		return s, fmt.Errorf("start telemetry-service: %w", err)
	}
	if s.TelemetryAddr, err = telemetry.PortEndpoint(ctx, "9091/tcp", ""); err != nil {
		return s, err
	}

	app, err := s.start(ctx, appRequest(nw.Name))
	if err != nil {
		return s, fmt.Errorf("start app: %w", err)
	}
	if s.AppURL, err = app.PortEndpoint(ctx, "8080/tcp", "http"); err != nil {
		return s, err
	}

	return s, nil
}

func (s *stack) start(ctx context.Context, req testcontainers.ContainerRequest) (testcontainers.Container, error) {
	c, err := testcontainers.GenericContainer(ctx, testcontainers.GenericContainerRequest{
		ContainerRequest: req,
		Started:          true,
	})
	if c != nil {
		s.containers = append(s.containers, c)
	}
	return c, err
}

func (s *stack) Terminate(ctx context.Context) error {
	var errs []error
	for i := len(s.containers) - 1; i >= 0; i-- {
		errs = append(errs, s.containers[i].Terminate(ctx))
	}
	if s.network != nil {
		errs = append(errs, s.network.Remove(ctx))
	}
	return errors.Join(errs...)
}

func postgresRequest(networkName string) testcontainers.ContainerRequest {
	return testcontainers.ContainerRequest{
		Image: postgresImage,
		Env: map[string]string{
			"POSTGRES_USER":     "postgres",
			"POSTGRES_PASSWORD": "postgres",
		},
		Files: []testcontainers.ContainerFile{
			{
				HostFilePath:      filepath.Join(appsDir, "smart_home", "init.sql"),
				ContainerFilePath: "/docker-entrypoint-initdb.d/01-smarthome.sql",
				FileMode:          0o644,
			},
			{
				HostFilePath:      filepath.Join(appsDir, "postgres", "init-services.sql"),
				ContainerFilePath: "/docker-entrypoint-initdb.d/02-services.sql",
				FileMode:          0o644,
			},
		},
		Networks:       []string{networkName},
		NetworkAliases: map[string][]string{networkName: {"postgres"}},
		// The first "ready" line comes from the temporary server that runs init scripts
		WaitingFor: wait.ForLog("database system is ready to accept connections").
			WithOccurrence(2).
			WithStartupTimeout(time.Minute),
	}
}

func kafkaRequest(networkName string) testcontainers.ContainerRequest {
	return testcontainers.ContainerRequest{
		Image: kafkaImage,
		Env: map[string]string{
			"KAFKA_NODE_ID":                                  "1",
			"KAFKA_PROCESS_ROLES":                            "broker,controller",
			"KAFKA_CONTROLLER_QUORUM_VOTERS":                 "1@kafka:9093",
			"KAFKA_CONTROLLER_LISTENER_NAMES":                "CONTROLLER",
			"KAFKA_LISTENERS":                                "PLAINTEXT://:9092,CONTROLLER://:9093",
			"KAFKA_ADVERTISED_LISTENERS":                     "PLAINTEXT://kafka:9092",
			"KAFKA_LISTENER_SECURITY_PROTOCOL_MAP":           "PLAINTEXT:PLAINTEXT,CONTROLLER:PLAINTEXT",
			"KAFKA_INTER_BROKER_LISTENER_NAME":               "PLAINTEXT",
			"KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR":         "1",
			"KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR": "1",
			"KAFKA_TRANSACTION_STATE_LOG_MIN_ISR":            "1",
			"KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS":         "0",
			"KAFKA_AUTO_CREATE_TOPICS_ENABLE":                "false",
		},
		Files: []testcontainers.ContainerFile{{
			HostFilePath:      filepath.Join(appsDir, "kafka", "create-topics.sh"),
			ContainerFilePath: "/create-topics.sh",
			FileMode:          0o755,
		}},
		Networks:       []string{networkName},
		NetworkAliases: map[string][]string{networkName: {"kafka"}},
		WaitingFor:     wait.ForLog("Kafka Server started").WithStartupTimeout(time.Minute),
	}
}

// createTopics does the job of kafka-init from docker-compose.yml
func createTopics(ctx context.Context, kafka testcontainers.Container) error {
	code, out, err := kafka.Exec(ctx, []string{"sh", "/create-topics.sh"})
	if err != nil {
		return fmt.Errorf("create topics: %w", err)
	}
	if code != 0 {
		output, _ := io.ReadAll(out)
		return fmt.Errorf("create topics: exit code %d: %s", code, output)
	}
	return nil
}

func temperatureAPIRequest(networkName string) testcontainers.ContainerRequest {
	return testcontainers.ContainerRequest{
		FromDockerfile: testcontainers.FromDockerfile{
			Context:    filepath.Join(appsDir, "temperature-api"),
			Dockerfile: "Dockerfile",
		},
		ExposedPorts:   []string{"8081/tcp"},
		Networks:       []string{networkName},
		NetworkAliases: map[string][]string{networkName: {"temperature-api"}},
		WaitingFor: wait.ForHTTP("/health").
			WithPort("8081/tcp").
			WithStartupTimeout(time.Minute),
	}
}

func heatingRequest(networkName string) testcontainers.ContainerRequest {
	return testcontainers.ContainerRequest{
		FromDockerfile: testcontainers.FromDockerfile{
			Context:    filepath.Join(appsDir, "heating-service"),
			Dockerfile: "Dockerfile",
		},
		Env: map[string]string{
			"DATABASE_URL":        "jdbc:postgresql://postgres:5432/devices",
			"DATABASE_USER":       "heating",
			"DATABASE_PASSWORD":   "heating",
			"KAFKA_BROKERS":       "kafka:9092",
			"TEMPERATURE_API_URL": "http://temperature-api:8081",
			"POLL_INTERVAL":       fmt.Sprintf("%dms", devicePollInterval.Milliseconds()),
		},
		ExposedPorts:   []string{"8082/tcp", "9090/tcp"},
		Networks:       []string{networkName},
		NetworkAliases: map[string][]string{networkName: {"heating-service"}},
		WaitingFor: wait.ForHTTP("/actuator/health").
			WithPort("8082/tcp").
			WithStartupTimeout(2 * time.Minute),
	}
}

func telemetryRequest(networkName string) testcontainers.ContainerRequest {
	return testcontainers.ContainerRequest{
		FromDockerfile: testcontainers.FromDockerfile{
			Context:    filepath.Join(appsDir, "telemetry-service"),
			Dockerfile: "Dockerfile",
		},
		Env: map[string]string{
			"DATABASE_URL":  "postgresql://telemetry:telemetry@postgres:5432/telemetry",
			"KAFKA_BROKERS": "kafka:9092",
		},
		ExposedPorts:   []string{"8083/tcp", "9091/tcp"},
		Networks:       []string{networkName},
		NetworkAliases: map[string][]string{networkName: {"telemetry-service"}},
		WaitingFor: wait.ForHTTP("/health").
			WithPort("8083/tcp").
			WithStartupTimeout(time.Minute),
	}
}

func appRequest(networkName string) testcontainers.ContainerRequest {
	return testcontainers.ContainerRequest{
		FromDockerfile: testcontainers.FromDockerfile{
			Context:    filepath.Join(appsDir, "smart_home"),
			Dockerfile: "Dockerfile",
		},
		Env: map[string]string{
			"DATABASE_URL":        "postgres://postgres:postgres@postgres:5432/smarthome",
			"TEMPERATURE_API_URL": "http://temperature-api:8081",
			"KAFKA_BROKERS":       "kafka:9092",
			"TELEMETRY_GRPC_ADDR": "telemetry-service:9091",
			"HEATING_GRPC_ADDR":   "heating-service:9090",
			"DEFAULT_HOME_ID":     defaultHomeID,
		},
		ExposedPorts: []string{"8080/tcp"},
		Networks:     []string{networkName},
		WaitingFor: wait.ForHTTP("/health").
			WithPort("8080/tcp").
			WithStartupTimeout(2 * time.Minute),
	}
}
