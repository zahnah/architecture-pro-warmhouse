package ru.warmhouse.heating.device;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import ru.warmhouse.heating.domain.Thermostat;
import ru.warmhouse.heating.service.HeatingService;

/** Collects readings from registered thermostats; an unanswered poll marks the thermostat offline */
@Component
public class DevicePoller {

    private static final Logger log = LoggerFactory.getLogger(DevicePoller.class);

    private final TemperatureApiDeviceAdapter device;
    private final HeatingService heating;

    public DevicePoller(TemperatureApiDeviceAdapter device, HeatingService heating) {
        this.device = device;
        this.heating = heating;
    }

    @Scheduled(fixedDelayString = "${heating.poll-interval}")
    public void poll() {
        for (Thermostat thermostat : heating.listAll()) {
            String deviceId = thermostat.getDeviceId();
            DeviceReading reading;
            try {
                reading = device.read(deviceId);
            } catch (RestClientException e) {
                log.warn("Thermostat {} is unreachable: {}", deviceId, e.getMessage());
                heating.onConnectionLost(deviceId);
                continue;
            }
            heating.onStateReported(deviceId, reading.temperature(), reading.measuredAt());
        }
    }
}
