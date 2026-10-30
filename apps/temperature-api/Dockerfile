# Build stage
FROM golang:1.22-alpine AS builder

WORKDIR /app

COPY go.mod ./
RUN go mod download

COPY . .
RUN CGO_ENABLED=0 GOOS=linux go build -o temperature-api

# Runtime stage
FROM alpine:3.20

WORKDIR /app

COPY --from=builder /app/temperature-api .

ENV PORT=8081
EXPOSE 8081

CMD ["./temperature-api"]
