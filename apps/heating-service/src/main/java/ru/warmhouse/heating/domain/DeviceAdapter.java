package ru.warmhouse.heating.domain;

/** Delivers desired settings to the physical thermostat */
public interface DeviceAdapter {

    void publishDesiredConfig(Thermostat thermostat);

    void clearDesiredConfig(String deviceId);
}
