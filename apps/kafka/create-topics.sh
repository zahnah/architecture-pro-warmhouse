#!/bin/sh
# Creates the event bus topics from schemas/api/asyncapi/event-bus.yaml; safe to run repeatedly.
set -eu

BOOTSTRAP="${KAFKA_BOOTSTRAP:-kafka:9092}"
TOPICS=/opt/kafka/bin/kafka-topics.sh

create() {
  topic="$1"
  partitions="$2"
  shift 2
  "$TOPICS" --bootstrap-server "$BOOTSTRAP" --create --if-not-exists \
    --topic "$topic" --partitions "$partitions" --replication-factor 1 "$@"
}

# Compacted: the last event per device is kept forever, a new service restores the device list from it
create device.lifecycle 12 --config cleanup.policy=compact --config retention.ms=-1
create telemetry.reading 24 --config cleanup.policy=delete --config retention.ms=604800000
create device.state.changed 12 --config cleanup.policy=delete --config retention.ms=604800000
create device.connectivity.changed 12 --config cleanup.policy=delete --config retention.ms=604800000

"$TOPICS" --bootstrap-server "$BOOTSTRAP" --list
