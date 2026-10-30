package ru.warmhouse.heating.events;

/** Kafka topics and headers from AsyncAPI event-bus.yaml */
final class Topics {

    static final String DEVICE_LIFECYCLE = "device.lifecycle";
    static final String TELEMETRY_READING = "telemetry.reading";
    static final String DEVICE_STATE_CHANGED = "device.state.changed";
    static final String DEVICE_CONNECTIVITY_CHANGED = "device.connectivity.changed";

    static final String EVENT_TYPE_HEADER = "eventType";

    /** Device type this service is responsible for, DeviceRegistered.type */
    static final String DEVICE_TYPE = "heating";

    private Topics() {
    }
}
