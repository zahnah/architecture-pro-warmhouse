package ru.warmhouse.heating.domain;

import java.util.List;
import java.util.Optional;

/** Thermostats that are connected to a home; unregistered ones are not returned */
public interface ThermostatRepository {

    Optional<Thermostat> findByDeviceId(String deviceId);

    List<Thermostat> findByHomeId(String homeId);

    List<Thermostat> findAll();

    /** Inserts or updates; saving a previously deleted device brings it back */
    void save(Thermostat thermostat);

    void delete(String deviceId);
}
