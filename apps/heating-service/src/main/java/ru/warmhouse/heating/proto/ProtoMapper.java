package ru.warmhouse.heating.proto;

import java.time.Instant;

import com.google.protobuf.BoolValue;
import com.google.protobuf.DoubleValue;
import com.google.protobuf.Timestamp;

import ru.warmhouse.heating.domain.HeatingMode;
import ru.warmhouse.heating.domain.Thermostat;
import ru.warmhouse.heating.v1.ThermostatState;

/** Domain model to the warmhouse.heating.v1 contract and back */
public final class ProtoMapper {

    private ProtoMapper() {
    }

    public static ru.warmhouse.heating.v1.Thermostat toProto(Thermostat t) {
        var builder = ru.warmhouse.heating.v1.Thermostat.newBuilder()
                .setDeviceId(t.getDeviceId())
                .setHomeId(t.getHomeId())
                .setTargetTemperature(t.getTargetTemperature())
                .setMode(toProto(t.getMode()))
                .setOnline(t.isOnline());
        if (t.getCurrentTemperature() != null) {
            builder.setCurrentTemperature(DoubleValue.of(t.getCurrentTemperature()));
        }
        if (t.getRelayOn() != null) {
            builder.setRelayOn(BoolValue.of(t.getRelayOn()));
        }
        if (t.getLastSeenAt() != null) {
            builder.setLastSeenAt(toTimestamp(t.getLastSeenAt()));
        }
        if (t.getUpdatedAt() != null) {
            builder.setUpdatedAt(toTimestamp(t.getUpdatedAt()));
        }
        return builder.build();
    }

    public static ThermostatState toState(Thermostat t) {
        var builder = ThermostatState.newBuilder()
                .setMode(toProto(t.getMode()))
                .setTargetTemperature(t.getTargetTemperature())
                .setRelayOn(Boolean.TRUE.equals(t.getRelayOn()));
        if (t.getCurrentTemperature() != null) {
            builder.setCurrentTemperature(DoubleValue.of(t.getCurrentTemperature()));
        }
        return builder.build();
    }

    public static ru.warmhouse.heating.v1.HeatingMode toProto(HeatingMode mode) {
        return switch (mode) {
            case OFF -> ru.warmhouse.heating.v1.HeatingMode.HEATING_MODE_OFF;
            case MANUAL -> ru.warmhouse.heating.v1.HeatingMode.HEATING_MODE_MANUAL;
            case ECO -> ru.warmhouse.heating.v1.HeatingMode.HEATING_MODE_ECO;
        };
    }

    /** @throws IllegalArgumentException for HEATING_MODE_UNSPECIFIED and unknown values */
    public static HeatingMode fromProto(ru.warmhouse.heating.v1.HeatingMode mode) {
        return switch (mode) {
            case HEATING_MODE_OFF -> HeatingMode.OFF;
            case HEATING_MODE_MANUAL -> HeatingMode.MANUAL;
            case HEATING_MODE_ECO -> HeatingMode.ECO;
            default -> throw new IllegalArgumentException("mode is required");
        };
    }

    public static Timestamp toTimestamp(Instant instant) {
        return Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
    }
}
