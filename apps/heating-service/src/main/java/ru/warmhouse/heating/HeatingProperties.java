package ru.warmhouse.heating;

import java.net.URI;
import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @param grpcPort          port of the HeatingService gRPC API
 * @param pollInterval      how often thermostats are polled for readings
 * @param temperatureApiUrl device emulator that stands in for the MQTT broker in the MVP
 */
@ConfigurationProperties("heating")
public record HeatingProperties(int grpcPort, Duration pollInterval, URI temperatureApiUrl) {
}
