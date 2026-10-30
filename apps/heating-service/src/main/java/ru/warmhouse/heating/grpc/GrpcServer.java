package ru.warmhouse.heating.grpc;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.TimeUnit;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.HealthStatusManager;
import io.grpc.protobuf.services.ProtoReflectionServiceV1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import ru.warmhouse.heating.HeatingProperties;

/** gRPC server with the HeatingService API, standard health checks and reflection for grpcurl */
@Component
public class GrpcServer implements SmartLifecycle {

    private static final Logger log = LoggerFactory.getLogger(GrpcServer.class);

    private final Server server;
    private volatile boolean running;

    public GrpcServer(HeatingProperties properties, HeatingGrpcService heating) {
        this.server = ServerBuilder.forPort(properties.grpcPort())
                .addService(heating)
                .addService(new HealthStatusManager().getHealthService())
                .addService(ProtoReflectionServiceV1.newInstance())
                .build();
    }

    @Override
    public void start() {
        try {
            server.start();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        running = true;
        log.info("gRPC server listening on {}", server.getPort());
    }

    @Override
    public void stop() {
        server.shutdown();
        try {
            server.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }
}
