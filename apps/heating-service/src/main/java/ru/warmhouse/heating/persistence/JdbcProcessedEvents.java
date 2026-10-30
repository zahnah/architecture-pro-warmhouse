package ru.warmhouse.heating.persistence;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import ru.warmhouse.heating.domain.ProcessedEvents;

@Repository
public class JdbcProcessedEvents implements ProcessedEvents {

    private final JdbcClient jdbc;

    public JdbcProcessedEvents(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean markProcessed(String eventId) {
        return jdbc.sql("INSERT INTO processed_events (event_id) VALUES (?) ON CONFLICT DO NOTHING")
                .param(eventId)
                .update() == 1;
    }
}
