package services

import (
	"time"

	"google.golang.org/grpc"
	"google.golang.org/grpc/backoff"
	"google.golang.org/grpc/credentials/insecure"
)

// dialService connects lazily, so the monolith starts even if the microservice is down.
// The reconnect delay is capped well below the gRPC default of two minutes, so the
// microservice is used again soon after it comes back.
func dialService(addr string) (*grpc.ClientConn, error) {
	return grpc.NewClient(addr,
		grpc.WithTransportCredentials(insecure.NewCredentials()),
		// Without it the DNS resolver also looks up a _grpc_config TXT record; Docker DNS forwards
		// that query to the upstream resolver, which delays the first call by seconds
		grpc.WithDisableServiceConfig(),
		grpc.WithConnectParams(grpc.ConnectParams{
			Backoff: backoff.Config{
				BaseDelay:  200 * time.Millisecond,
				Multiplier: 1.6,
				Jitter:     0.2,
				MaxDelay:   5 * time.Second,
			},
			MinConnectTimeout: 2 * time.Second,
		}),
	)
}
