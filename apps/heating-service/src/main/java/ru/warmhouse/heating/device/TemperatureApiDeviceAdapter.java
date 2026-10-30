package ru.warmhouse.heating.device;

import java.time.Duration;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import ru.warmhouse.heating.HeatingProperties;
import ru.warmhouse.heating.domain.DeviceAdapter;
import ru.warmhouse.heating.domain.Thermostat;

/**
 * Talks to thermostats through temperature-api, which emulates the devices in the MVP
 * instead of the MQTT broker. The emulator has no settings endpoint, so desired settings are only logged.
 */
@Component
public class TemperatureApiDeviceAdapter implements DeviceAdapter {

    private static final Logger log = LoggerFactory.getLogger(TemperatureApiDeviceAdapter.class);
    private static final Duration TIMEOUT = Duration.ofSeconds(2);

    private final RestClient client;

    public TemperatureApiDeviceAdapter(HeatingProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(TIMEOUT);
        requestFactory.setReadTimeout(TIMEOUT);
        this.client = RestClient.builder()
                .baseUrl(properties.temperatureApiUrl().toString())
                .requestFactory(requestFactory)
                .build();
    }

    @Override
    public void publishDesiredConfig(Thermostat thermostat) {
        log.info("Desired config for {}: mode={}, target={}",
                thermostat.getDeviceId(), thermostat.getMode(), thermostat.getTargetTemperature());
    }

    @Override
    public void clearDesiredConfig(String deviceId) {
        log.info("Desired config for {} cleared", deviceId);
    }

    /** @throws RestClientException if the device does not answer */
    public DeviceReading read(String deviceId) {
        Response response = client.get()
                .uri("/temperature/{deviceId}", deviceId)
                .retrieve()
                .body(Response.class);
        if (response == null) {
            throw new RestClientException("empty response from device " + deviceId);
        }
        Instant measuredAt = response.timestamp() == null ? Instant.now() : Instant.parse(response.timestamp());
        return new DeviceReading(response.value(), measuredAt);
    }

    /** Subset of the temperature-api response */
    record Response(double value, String timestamp) {
    }
}
