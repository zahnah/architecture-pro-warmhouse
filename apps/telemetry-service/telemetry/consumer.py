import logging
import threading
from datetime import timezone

from confluent_kafka import Consumer, KafkaError, Message
from warmhouse.events.v1 import device_events_pb2 as events

from telemetry.repository import AvailabilityRepository, ReadingRepository

log = logging.getLogger(__name__)

TELEMETRY_READING = "telemetry.reading"
DEVICE_CONNECTIVITY_CHANGED = "device.connectivity.changed"
EVENT_TYPE_HEADER = "eventType"


class TelemetryConsumer:
    """Stores telemetry.reading and device.connectivity.changed events (AsyncAPI event-bus.yaml)."""

    def __init__(self, brokers: str, readings: ReadingRepository, availability: AvailabilityRepository):
        self._consumer = Consumer({
            "bootstrap.servers": brokers,
            "group.id": "telemetry-service",
            "auto.offset.reset": "earliest",
            "enable.auto.commit": False,
        })
        self._readings = readings
        self._availability = availability
        self._stopped = threading.Event()
        self._thread = threading.Thread(target=self._run, name="telemetry-consumer", daemon=True)

    def start(self) -> None:
        self._consumer.subscribe([TELEMETRY_READING, DEVICE_CONNECTIVITY_CHANGED])
        self._thread.start()

    def stop(self) -> None:
        self._stopped.set()
        self._thread.join(timeout=10)
        self._consumer.close()

    def _run(self) -> None:
        while not self._stopped.is_set():
            msg = self._consumer.poll(1.0)
            if msg is None:
                continue
            if msg.error():
                if msg.error().code() != KafkaError._PARTITION_EOF:
                    log.warning("Kafka error: %s", msg.error())
                continue
            try:
                self._handle(msg)
            except Exception:
                # A broken event must not block the partition; at-least-once still holds for the rest
                log.exception("Skipping event at %s[%d]@%d", msg.topic(), msg.partition(), msg.offset())
            self._consumer.commit(message=msg, asynchronous=True)

    def _handle(self, msg: Message) -> None:
        event_type = _header(msg, EVENT_TYPE_HEADER)
        if event_type == "TelemetryReading":
            event = events.TelemetryReading.FromString(msg.value())
            self._readings.add(
                event_id=event.event_id,
                device_id=event.device_id,
                home_id=event.home_id,
                metric=event.metric,
                value=event.value,
                unit=event.unit,
                time=event.measured_at.ToDatetime(tzinfo=timezone.utc),
            )
        elif event_type == "DeviceConnectivityChanged":
            event = events.DeviceConnectivityChanged.FromString(msg.value())
            self._availability.add(
                event_id=event.event_id,
                device_id=event.device_id,
                home_id=event.home_id,
                online=event.online,
                time=event.occurred_at.ToDatetime(tzinfo=timezone.utc),
            )
        else:
            log.warning("Skipping %s event with unknown type '%s'", msg.topic(), event_type)


def _header(msg: Message, name: str) -> str:
    for key, value in msg.headers() or []:
        if key == name and value is not None:
            return value.decode()
    return ""
