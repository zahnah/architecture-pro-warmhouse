package e2e

import (
	"context"
	"errors"
	"fmt"
	"path/filepath"
	"time"

	"github.com/testcontainers/testcontainers-go"
	"github.com/testcontainers/testcontainers-go/network"
	"github.com/testcontainers/testcontainers-go/wait"
)

// appsDir is the apps/ directory with the services under test, relative to this package
const appsDir = ".."

// stack is the smart_home monolith with its dependencies, started the same way as apps/docker-compose.yml
type stack struct {
	network    *testcontainers.DockerNetwork
	containers []testcontainers.Container

	AppURL         string
	TemperatureURL string
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

	temperatureAPI, err := s.start(ctx, temperatureAPIRequest(nw.Name))
	if err != nil {
		return s, fmt.Errorf("start temperature-api: %w", err)
	}
	if s.TemperatureURL, err = temperatureAPI.PortEndpoint(ctx, "8081/tcp", "http"); err != nil {
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
		Image: "postgres:16-alpine",
		Env: map[string]string{
			"POSTGRES_USER":     "postgres",
			"POSTGRES_PASSWORD": "postgres",
		},
		Files: []testcontainers.ContainerFile{{
			HostFilePath:      filepath.Join(appsDir, "smart_home", "init.sql"),
			ContainerFilePath: "/docker-entrypoint-initdb.d/init.sql",
			FileMode:          0o644,
		}},
		Networks:       []string{networkName},
		NetworkAliases: map[string][]string{networkName: {"postgres"}},
		// The first "ready" line comes from the temporary server that runs init scripts
		WaitingFor: wait.ForLog("database system is ready to accept connections").
			WithOccurrence(2).
			WithStartupTimeout(time.Minute),
	}
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

func appRequest(networkName string) testcontainers.ContainerRequest {
	return testcontainers.ContainerRequest{
		FromDockerfile: testcontainers.FromDockerfile{
			Context:    filepath.Join(appsDir, "smart_home"),
			Dockerfile: "Dockerfile",
		},
		Env: map[string]string{
			"DATABASE_URL":        "postgres://postgres:postgres@postgres:5432/smarthome",
			"TEMPERATURE_API_URL": "http://temperature-api:8081",
		},
		ExposedPorts: []string{"8080/tcp"},
		Networks:     []string{networkName},
		WaitingFor: wait.ForHTTP("/health").
			WithPort("8080/tcp").
			WithStartupTimeout(2 * time.Minute),
	}
}
