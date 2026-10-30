# Smart Home Sensor Management API

## Prerequisites

- Docker and Docker Compose

## Getting Started

### Option 1: Using Docker Compose (Recommended)

The easiest way to start the application is to use Docker Compose:

```bash
./init.sh
```

This script will:

1. Build and start the PostgreSQL, temperature-api and application containers
2. Wait for the services to be ready
3. Display information about how to access the API

Alternatively, you can run Docker Compose directly:

```bash
docker-compose up -d
```

The API will be available at http://localhost:8080, temperature-api at http://localhost:8081.

From the repository root the same containers are built and started by `make build` and `make up`.

### Option 2: Manual setup

If you prefer to run the application without Docker:

1. Start the PostgreSQL database:

```bash
docker-compose up -d postgres
```

2. Build and run the application:

```bash
go build -o smarthome
./smarthome
```

## API Testing

A Postman collection is provided for testing the API. Import the `smarthome-api.postman_collection.json` file into Postman to get started.

Call "Create Sensor" and then "Get All Sensors" several times: temperature sensors get a new random `value` from temperature-api on every call.

## E2E Tests

`e2e/` contains Go tests built on [testcontainers-go](https://golang.testcontainers.org/). They build the temperature-api and smart_home images, start them with PostgreSQL (initialized by `smart_home/init.sql`) in an isolated network on random ports, and run the Postman scenario above. Tests run in a container with the Docker socket mounted, so only Docker is required:

```bash
docker compose run --rm --build e2e
```

The tests are a mandatory step of `make precommit` in the repository root and do not conflict with containers started by `docker compose up`.

## Temperature API

Mock service in `temperature-api/` (Go, port 8081) that returns a random temperature:

- `GET /temperature?location=Living%20Room` - by location (`sensorId` query parameter is optional)
- `GET /temperature/1` - by sensor ID
- `GET /health` - Health check

Location and sensor ID default to each other: 1 - Living Room, 2 - Bedroom, 3 - Kitchen, otherwise Unknown / 0.

## API Endpoints

- `GET /health` - Health check
- `GET /api/v1/sensors` - Get all sensors
- `GET /api/v1/sensors/:id` - Get a specific sensor
- `POST /api/v1/sensors` - Create a new sensor
- `PUT /api/v1/sensors/:id` - Update a sensor
- `DELETE /api/v1/sensors/:id` - Delete a sensor
- `PATCH /api/v1/sensors/:id/value` - Update a sensor's value and status
