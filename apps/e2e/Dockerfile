# Build context is apps/: the tests build the service images from their sources
# and import the gRPC stubs of smart_home
FROM golang:1.26-alpine

ENV CGO_ENABLED=0

WORKDIR /src/apps/e2e

COPY e2e/go.mod e2e/go.sum ./
COPY smart_home/go.mod smart_home/go.sum /src/apps/smart_home/
RUN go mod download

COPY . /src/apps

# The first run builds heating-service with Maven, which takes a few minutes
CMD ["go", "test", "-v", "-count=1", "-timeout=15m", "./..."]
