package ru.warmhouse.heating.service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.warmhouse.heating.domain.DeviceAdapter;
import ru.warmhouse.heating.domain.DomainEventPublisher;
import ru.warmhouse.heating.domain.HeatingMode;
import ru.warmhouse.heating.domain.ProcessedEvents;
import ru.warmhouse.heating.domain.Thermostat;
import ru.warmhouse.heating.domain.ThermostatNotFoundException;
import ru.warmhouse.heating.domain.ThermostatRepository;

@Service
public class HeatingService {

    private static final Logger log = LoggerFactory.getLogger(HeatingService.class);

    private final ThermostatRepository thermostats;
    private final ProcessedEvents processedEvents;
    private final DeviceAdapter device;
    private final DomainEventPublisher events;
    private final Clock clock;

    public HeatingService(ThermostatRepository thermostats, ProcessedEvents processedEvents,
                          DeviceAdapter device, DomainEventPublisher events, Clock clock) {
        this.thermostats = thermostats;
        this.processedEvents = processedEvents;
        this.device = device;
        this.events = events;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public Thermostat get(String deviceId) {
        return thermostats.findByDeviceId(deviceId).orElseThrow(() -> new ThermostatNotFoundException(deviceId));
    }

    @Transactional(readOnly = true)
    public List<Thermostat> listByHome(String homeId) {
        return thermostats.findByHomeId(homeId);
    }

    @Transactional(readOnly = true)
    public List<Thermostat> listAll() {
        return thermostats.findAll();
    }

    @Transactional
    public Thermostat setTarget(String deviceId, double temperature) {
        Thermostat thermostat = get(deviceId);
        thermostat.setTarget(temperature, clock.instant());
        return settingsChanged(thermostat);
    }

    @Transactional
    public Thermostat setMode(String deviceId, HeatingMode mode) {
        Thermostat thermostat = get(deviceId);
        thermostat.setMode(mode, clock.instant());
        return settingsChanged(thermostat);
    }

    @Transactional
    public void onDeviceRegistered(String eventId, String deviceId, String homeId) {
        if (!processedEvents.markProcessed(eventId)) {
            return;
        }
        // A replayed registration must not reset settings the owner has already made
        if (thermostats.findByDeviceId(deviceId).filter(t -> t.getHomeId().equals(homeId)).isPresent()) {
            return;
        }
        thermostats.save(Thermostat.register(deviceId, homeId, clock.instant()));
        log.info("Thermostat {} registered in home {}", deviceId, homeId);
    }

    @Transactional
    public void onDeviceUnregistered(String eventId, String deviceId) {
        if (!processedEvents.markProcessed(eventId)) {
            return;
        }
        if (thermostats.findByDeviceId(deviceId).isEmpty()) {
            return;
        }
        thermostats.delete(deviceId);
        device.clearDesiredConfig(deviceId);
        log.info("Thermostat {} unregistered", deviceId);
    }

    @Transactional
    public void onStateReported(String deviceId, double temperature, Instant measuredAt) {
        thermostats.findByDeviceId(deviceId).ifPresent(thermostat -> {
            boolean cameOnline = thermostat.markOnline();
            boolean relayChanged = thermostat.applyReading(temperature, measuredAt);
            thermostats.save(thermostat);

            events.publishReading(thermostat, temperature, measuredAt);
            if (cameOnline) {
                events.publishConnectivityChanged(thermostat);
            }
            if (relayChanged) {
                events.publishStateChanged(thermostat);
            }
        });
    }

    @Transactional
    public void onConnectionLost(String deviceId) {
        thermostats.findByDeviceId(deviceId).ifPresent(thermostat -> {
            if (thermostat.markOffline(clock.instant())) {
                thermostats.save(thermostat);
                events.publishConnectivityChanged(thermostat);
            }
        });
    }

    private Thermostat settingsChanged(Thermostat thermostat) {
        thermostats.save(thermostat);
        device.publishDesiredConfig(thermostat);
        events.publishStateChanged(thermostat);
        return thermostat;
    }
}
