package ru.warmhouse.heating.device;

import java.time.Instant;

/** Normalized thermostat reading */
public record DeviceReading(double temperature, Instant measuredAt) {
}
