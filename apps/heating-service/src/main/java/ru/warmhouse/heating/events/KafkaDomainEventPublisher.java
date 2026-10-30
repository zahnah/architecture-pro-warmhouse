package ru.warmhouse.heating.events;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import com.google.protobuf.Any;
import com.google.protobuf.Message;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import ru.warmhouse.events.v1.DeviceConnectivityChanged;
import ru.warmhouse.events.v1.DeviceStateChanged;
import ru.warmhouse.events.v1.TelemetryReading;
import ru.warmhouse.heating.domain.DomainEventPublisher;
import ru.warmhouse.heating.domain.Thermostat;
import ru.warmhouse.heating.proto.ProtoMapper;

@Component
public class KafkaDomainEventPublisher implements DomainEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaDomainEventPublisher.class);

    private final KafkaTemplate<String, byte[]> kafka;
    private final Clock clock;

    public KafkaDomainEventPublisher(KafkaTemplate<String, byte[]> kafka, Clock clock) {
        this.kafka = kafka;
        this.clock = clock;
    }

    @Override
    public void publishReading(Thermostat t, double temperature, Instant measuredAt) {
        send(Topics.TELEMETRY_READING, t.getDeviceId(), TelemetryReading.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setOccurredAt(ProtoMapper.toTimestamp(clock.instant()))
                .setDeviceId(t.getDeviceId())
                .setHomeId(t.getHomeId())
                .setMetric("temperature")
                .setValue(temperature)
                .setUnit("celsius")
                .setMeasuredAt(ProtoMapper.toTimestamp(measuredAt))
                .build());
    }

    @Override
    public void publishStateChanged(Thermostat t) {
        send(Topics.DEVICE_STATE_CHANGED, t.getDeviceId(), DeviceStateChanged.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setOccurredAt(ProtoMapper.toTimestamp(clock.instant()))
                .setDeviceId(t.getDeviceId())
                .setHomeId(t.getHomeId())
                .setType(Topics.DEVICE_TYPE)
                .setState(Any.pack(ProtoMapper.toState(t)))
                .build());
    }

    @Override
    public void publishConnectivityChanged(Thermostat t) {
        send(Topics.DEVICE_CONNECTIVITY_CHANGED, t.getDeviceId(), DeviceConnectivityChanged.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setOccurredAt(ProtoMapper.toTimestamp(clock.instant()))
                .setDeviceId(t.getDeviceId())
                .setHomeId(t.getHomeId())
                .setOnline(t.isOnline())
                .build());
    }

    private void send(String topic, String deviceId, Message event) {
        var record = new ProducerRecord<>(topic, deviceId, event.toByteArray());
        record.headers().add(Topics.EVENT_TYPE_HEADER,
                event.getDescriptorForType().getName().getBytes(StandardCharsets.UTF_8));
        kafka.send(record).whenComplete((result, error) -> {
            if (error != null) {
                log.error("Failed to publish {} for {} to {}", event.getDescriptorForType().getName(), deviceId, topic, error);
            }
        });
    }
}
