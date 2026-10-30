package ru.warmhouse.heating.persistence;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import ru.warmhouse.heating.domain.HeatingMode;
import ru.warmhouse.heating.domain.Thermostat;
import ru.warmhouse.heating.domain.ThermostatRepository;

@Repository
public class JdbcThermostatRepository implements ThermostatRepository {

    private static final String COLUMNS = """
            device_id, home_id, target_temperature, mode, current_temperature,
            relay_on, online, last_seen_at, updated_at""";

    private final JdbcClient jdbc;

    public JdbcThermostatRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<Thermostat> findByDeviceId(String deviceId) {
        return jdbc.sql("SELECT " + COLUMNS + " FROM thermostats WHERE device_id = ? AND deleted_at IS NULL")
                .param(deviceId)
                .query(JdbcThermostatRepository::map)
                .optional();
    }

    @Override
    public List<Thermostat> findByHomeId(String homeId) {
        return jdbc.sql("SELECT " + COLUMNS + " FROM thermostats WHERE home_id = ? AND deleted_at IS NULL ORDER BY device_id")
                .param(homeId)
                .query(JdbcThermostatRepository::map)
                .list();
    }

    @Override
    public List<Thermostat> findAll() {
        return jdbc.sql("SELECT " + COLUMNS + " FROM thermostats WHERE deleted_at IS NULL ORDER BY device_id")
                .query(JdbcThermostatRepository::map)
                .list();
    }

    @Override
    public void save(Thermostat t) {
        jdbc.sql("""
                        INSERT INTO thermostats (device_id, home_id, target_temperature, mode, current_temperature,
                                                 relay_on, online, last_seen_at, updated_at)
                        VALUES (:deviceId, :homeId, :target, :mode, :current, :relayOn, :online, :lastSeenAt, :updatedAt)
                        ON CONFLICT (device_id) DO UPDATE SET
                            home_id = EXCLUDED.home_id,
                            target_temperature = EXCLUDED.target_temperature,
                            mode = EXCLUDED.mode,
                            current_temperature = EXCLUDED.current_temperature,
                            relay_on = EXCLUDED.relay_on,
                            online = EXCLUDED.online,
                            last_seen_at = EXCLUDED.last_seen_at,
                            updated_at = EXCLUDED.updated_at,
                            deleted_at = NULL""")
                .param("deviceId", t.getDeviceId())
                .param("homeId", t.getHomeId())
                .param("target", t.getTargetTemperature())
                .param("mode", t.getMode().name())
                .param("current", t.getCurrentTemperature())
                .param("relayOn", t.getRelayOn())
                .param("online", t.isOnline())
                .param("lastSeenAt", toTimestamp(t.getLastSeenAt()))
                .param("updatedAt", toTimestamp(t.getUpdatedAt()))
                .update();
    }

    @Override
    public void delete(String deviceId) {
        jdbc.sql("UPDATE thermostats SET deleted_at = now(), online = FALSE WHERE device_id = ? AND deleted_at IS NULL")
                .param(deviceId)
                .update();
    }

    private static Thermostat map(ResultSet rs, int row) throws SQLException {
        return new Thermostat(
                rs.getString("device_id"),
                rs.getString("home_id"),
                rs.getDouble("target_temperature"),
                HeatingMode.valueOf(rs.getString("mode")),
                rs.getObject("current_temperature", Double.class),
                rs.getObject("relay_on", Boolean.class),
                rs.getBoolean("online"),
                toInstant(rs.getTimestamp("last_seen_at")),
                toInstant(rs.getTimestamp("updated_at")));
    }

    private static Timestamp toTimestamp(Instant instant) {
        return instant == null ? null : Timestamp.from(instant);
    }

    private static Instant toInstant(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant();
    }
}
