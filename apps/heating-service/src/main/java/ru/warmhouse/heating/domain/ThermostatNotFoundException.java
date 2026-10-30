package ru.warmhouse.heating.domain;

public class ThermostatNotFoundException extends RuntimeException {

    public ThermostatNotFoundException(String deviceId) {
        super("thermostat %s not found".formatted(deviceId));
    }
}
