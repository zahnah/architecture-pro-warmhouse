package ru.warmhouse.heating.domain;

/** Kafka delivers at least once; consumers skip events they have already handled */
public interface ProcessedEvents {

    /** @return false if the event was already processed */
    boolean markProcessed(String eventId);
}
