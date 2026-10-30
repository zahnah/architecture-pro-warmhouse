package ru.warmhouse.heating.domain;

import java.time.Instant;

/** Domain events of the heating service, AsyncAPI event-bus.yaml */
public interface DomainEventPublisher {

    void publishReading(Thermostat thermostat, double temperature, Instant measuredAt);

    void publishStateChanged(Thermostat thermostat);

    void publishConnectivityChanged(Thermostat thermostat);
}
