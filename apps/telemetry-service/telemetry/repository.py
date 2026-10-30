from dataclasses import dataclass
from datetime import datetime
from typing import Optional

from psycopg.rows import class_row
from psycopg_pool import ConnectionPool

# The timescaledb extension is created by the database owner (apps/postgres/init-services.sql)
SCHEMA = """
CREATE TABLE IF NOT EXISTS readings (
    time      TIMESTAMPTZ      NOT NULL,
    device_id TEXT             NOT NULL,
    home_id   TEXT             NOT NULL,
    metric    TEXT             NOT NULL,
    value     DOUBLE PRECISION NOT NULL,
    unit      TEXT             NOT NULL,
    event_id  TEXT             NOT NULL
);
SELECT create_hypertable('readings', by_range('time'), if_not_exists => TRUE);
CREATE UNIQUE INDEX IF NOT EXISTS readings_event_id ON readings (event_id, time);
CREATE INDEX IF NOT EXISTS readings_device_metric_time ON readings (device_id, metric, time DESC);
CREATE INDEX IF NOT EXISTS readings_home_time ON readings (home_id, time DESC);
SELECT add_retention_policy('readings', INTERVAL '90 days', if_not_exists => TRUE);

CREATE TABLE IF NOT EXISTS availability (
    time      TIMESTAMPTZ NOT NULL,
    device_id TEXT        NOT NULL,
    home_id   TEXT        NOT NULL,
    online    BOOLEAN     NOT NULL,
    event_id  TEXT        NOT NULL
);
SELECT create_hypertable('availability', by_range('time'), if_not_exists => TRUE);
CREATE UNIQUE INDEX IF NOT EXISTS availability_event_id ON availability (event_id, time);
CREATE INDEX IF NOT EXISTS availability_device_time ON availability (device_id, time DESC);
SELECT add_retention_policy('availability', INTERVAL '90 days', if_not_exists => TRUE);
"""

# AggregationInterval of telemetry_service.proto to time_bucket widths
BUCKETS = {
    1: "1 minute",
    2: "5 minutes",
    3: "1 hour",
    4: "1 day",
}


@dataclass(frozen=True)
class Reading:
    device_id: str
    metric: str
    value: float
    unit: str
    time: datetime


@dataclass(frozen=True)
class SeriesPoint:
    time: datetime
    value: float
    min: Optional[float] = None
    max: Optional[float] = None


@dataclass(frozen=True)
class AvailabilityChange:
    time: datetime
    online: bool


class Database:
    def __init__(self, url: str):
        self.pool = ConnectionPool(url, min_size=1, max_size=10, kwargs={"autocommit": True}, open=True)

    def migrate(self) -> None:
        with self.pool.connection() as conn:
            conn.execute(SCHEMA)

    def ping(self) -> None:
        with self.pool.connection() as conn:
            conn.execute("SELECT 1")

    def close(self) -> None:
        self.pool.close()


class ReadingRepository:
    def __init__(self, db: Database):
        self._pool = db.pool

    def add(self, event_id: str, device_id: str, home_id: str, metric: str, value: float, unit: str,
            time: datetime) -> bool:
        """Returns False if the event was already stored."""
        with self._pool.connection() as conn:
            cur = conn.execute(
                """INSERT INTO readings (time, device_id, home_id, metric, value, unit, event_id)
                   VALUES (%s, %s, %s, %s, %s, %s, %s)
                   ON CONFLICT DO NOTHING""",
                (time, device_id, home_id, metric, value, unit, event_id),
            )
            return cur.rowcount == 1

    def latest_for_device(self, device_id: str) -> list[Reading]:
        with self._pool.connection() as conn:
            cur = conn.cursor(row_factory=class_row(Reading))
            return cur.execute(
                """SELECT DISTINCT ON (metric) device_id, metric, value, unit, time
                   FROM readings WHERE device_id = %s
                   ORDER BY metric, time DESC""",
                (device_id,),
            ).fetchall()

    def latest_for_home(self, home_id: str, metric: str = "") -> list[Reading]:
        with self._pool.connection() as conn:
            cur = conn.cursor(row_factory=class_row(Reading))
            return cur.execute(
                """SELECT DISTINCT ON (device_id, metric) device_id, metric, value, unit, time
                   FROM readings WHERE home_id = %s AND (%s = '' OR metric = %s)
                   ORDER BY device_id, metric, time DESC""",
                (home_id, metric, metric),
            ).fetchall()

    def unit(self, device_id: str, metric: str) -> str:
        with self._pool.connection() as conn:
            row = conn.execute(
                "SELECT unit FROM readings WHERE device_id = %s AND metric = %s ORDER BY time DESC LIMIT 1",
                (device_id, metric),
            ).fetchone()
            return row[0] if row else ""

    def series(self, device_id: str, metric: str, start: datetime, end: datetime, interval: int,
               limit: int) -> list[SeriesPoint]:
        with self._pool.connection() as conn:
            cur = conn.cursor(row_factory=class_row(SeriesPoint))
            if interval in BUCKETS:
                return cur.execute(
                    """SELECT time_bucket(%s::interval, time) AS time,
                              avg(value) AS value, min(value) AS min, max(value) AS max
                       FROM readings
                       WHERE device_id = %s AND metric = %s AND time >= %s AND time < %s
                       GROUP BY 1 ORDER BY 1 LIMIT %s""",
                    (BUCKETS[interval], device_id, metric, start, end, limit),
                ).fetchall()
            return cur.execute(
                """SELECT time, value FROM readings
                   WHERE device_id = %s AND metric = %s AND time >= %s AND time < %s
                   ORDER BY time LIMIT %s""",
                (device_id, metric, start, end, limit),
            ).fetchall()


class AvailabilityRepository:
    def __init__(self, db: Database):
        self._pool = db.pool

    def add(self, event_id: str, device_id: str, home_id: str, online: bool, time: datetime) -> bool:
        """Returns False if the event was already stored."""
        with self._pool.connection() as conn:
            cur = conn.execute(
                """INSERT INTO availability (time, device_id, home_id, online, event_id)
                   VALUES (%s, %s, %s, %s, %s)
                   ON CONFLICT DO NOTHING""",
                (time, device_id, home_id, online, event_id),
            )
            return cur.rowcount == 1

    def changes(self, device_id: str, start: datetime, end: datetime, limit: int) -> list[AvailabilityChange]:
        with self._pool.connection() as conn:
            cur = conn.cursor(row_factory=class_row(AvailabilityChange))
            return cur.execute(
                """SELECT time, online FROM availability
                   WHERE device_id = %s AND time >= %s AND time < %s
                   ORDER BY time LIMIT %s""",
                (device_id, start, end, limit),
            ).fetchall()
