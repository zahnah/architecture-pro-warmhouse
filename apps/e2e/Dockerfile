# Build context is apps/: the tests build temperature-api and smart_home images from their sources
FROM golang:1.26-alpine

ENV CGO_ENABLED=0

WORKDIR /src/apps/e2e

COPY e2e/go.mod e2e/go.sum ./
RUN go mod download

COPY . /src/apps

CMD ["go", "test", "-v", "-count=1", "-timeout=10m", "./..."]
