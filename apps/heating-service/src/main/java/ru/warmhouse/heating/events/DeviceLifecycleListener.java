package ru.warmhouse.heating.events;

import java.nio.charset.StandardCharsets;

import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import ru.warmhouse.events.v1.DeviceRegistered;
import ru.warmhouse.events.v1.DeviceUnregistered;
import ru.warmhouse.heating.service.HeatingService;

/** Keeps the list of thermostats in sync with device.lifecycle */
@Component
public class DeviceLifecycleListener {

    private static final Logger log = LoggerFactory.getLogger(DeviceLifecycleListener.class);

    private final HeatingService heating;

    public DeviceLifecycleListener(HeatingService heating) {
        this.heating = heating;
    }

    @KafkaListener(topics = Topics.DEVICE_LIFECYCLE)
    public void onLifecycleEvent(ConsumerRecord<String, byte[]> record) throws InvalidProtocolBufferException {
        Header header = record.headers().lastHeader(Topics.EVENT_TYPE_HEADER);
        String eventType = header == null ? "" : new String(header.value(), StandardCharsets.UTF_8);
        switch (eventType) {
            case "DeviceRegistered" -> {
                DeviceRegistered event = DeviceRegistered.parseFrom(record.value());
                if (Topics.DEVICE_TYPE.equals(event.getType())) {
                    heating.onDeviceRegistered(event.getEventId(), event.getDeviceId(), event.getHomeId());
                }
            }
            case "DeviceUnregistered" -> {
                DeviceUnregistered event = DeviceUnregistered.parseFrom(record.value());
                heating.onDeviceUnregistered(event.getEventId(), event.getDeviceId());
            }
            default -> log.warn("Skipping {} event with unknown type '{}'", Topics.DEVICE_LIFECYCLE, eventType);
        }
    }
}
