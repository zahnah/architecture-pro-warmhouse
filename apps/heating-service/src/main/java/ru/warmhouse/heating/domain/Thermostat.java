package ru.warmhouse.heating.domain;

import java.time.Instant;
import java.util.Objects;

/**
 * Desired settings and the last reported state of a thermostat.
 * The relay heats while the room is colder than the effective target.
 */
public class Thermostat {

    public static final double MIN_TARGET = 5;
    public static final double MAX_TARGET = 35;
    public static final double DEFAULT_TARGET = 20;
    /** ECO keeps the room this many degrees below the target */
    static final double ECO_SETBACK = 2;

    private final String deviceId;
    private final String homeId;
    private double targetTemperature;
    private HeatingMode mode;
    private Double currentTemperature;
    private Boolean relayOn;
    private boolean online;
    private Instant lastSeenAt;
    private Instant updatedAt;

    public Thermostat(String deviceId, String homeId, double targetTemperature, HeatingMode mode,
                      Double currentTemperature, Boolean relayOn, boolean online,
                      Instant lastSeenAt, Instant updatedAt) {
        this.deviceId = Objects.requireNonNull(deviceId);
        this.homeId = Objects.requireNonNull(homeId);
        this.targetTemperature = targetTemperature;
        this.mode = Objects.requireNonNull(mode);
        this.currentTemperature = currentTemperature;
        this.relayOn = relayOn;
        this.online = online;
        this.lastSeenAt = lastSeenAt;
        this.updatedAt = updatedAt;
    }

    /** A newly connected thermostat is off until the owner sets it up */
    public static Thermostat register(String deviceId, String homeId, Instant now) {
        return new Thermostat(deviceId, homeId, DEFAULT_TARGET, HeatingMode.OFF, null, null, false, null, now);
    }

    /** Sets the target; a thermostat that is off switches to MANUAL */
    public void setTarget(double temperature, Instant now) {
        if (Double.isNaN(temperature) || temperature < MIN_TARGET || temperature > MAX_TARGET) {
            throw new IllegalArgumentException(
                    "target temperature must be between %s and %s".formatted(MIN_TARGET, MAX_TARGET));
        }
        targetTemperature = temperature;
        if (mode == HeatingMode.OFF) {
            mode = HeatingMode.MANUAL;
        }
        settingsChanged(now);
    }

    public void setMode(HeatingMode mode, Instant now) {
        this.mode = Objects.requireNonNull(mode);
        settingsChanged(now);
    }

    /** @return whether the relay state changed */
    public boolean applyReading(double temperature, Instant measuredAt) {
        Boolean before = relayOn;
        currentTemperature = temperature;
        lastSeenAt = measuredAt;
        relayOn = computeRelay();
        updatedAt = measuredAt;
        return !Objects.equals(before, relayOn);
    }

    /** @return whether the thermostat was offline before */
    public boolean markOnline() {
        boolean changed = !online;
        online = true;
        return changed;
    }

    /** @return whether the thermostat was online before */
    public boolean markOffline(Instant now) {
        boolean changed = online;
        online = false;
        if (changed) {
            updatedAt = now;
        }
        return changed;
    }

    private void settingsChanged(Instant now) {
        relayOn = computeRelay();
        updatedAt = now;
    }

    private Boolean computeRelay() {
        if (currentTemperature == null) {
            return null;
        }
        return mode != HeatingMode.OFF && currentTemperature < effectiveTarget();
    }

    private double effectiveTarget() {
        return mode == HeatingMode.ECO ? targetTemperature - ECO_SETBACK : targetTemperature;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getHomeId() {
        return homeId;
    }

    public double getTargetTemperature() {
        return targetTemperature;
    }

    public HeatingMode getMode() {
        return mode;
    }

    public Double getCurrentTemperature() {
        return currentTemperature;
    }

    public Boolean getRelayOn() {
        return relayOn;
    }

    public boolean isOnline() {
        return online;
    }

    public Instant getLastSeenAt() {
        return lastSeenAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
