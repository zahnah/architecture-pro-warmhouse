import os
from dataclasses import dataclass


@dataclass(frozen=True)
class Settings:
    database_url: str
    kafka_brokers: str
    grpc_port: int
    http_port: int

    @classmethod
    def from_env(cls) -> "Settings":
        return cls(
            database_url=os.getenv("DATABASE_URL", "postgresql://telemetry:telemetry@localhost:5432/telemetry"),
            kafka_brokers=os.getenv("KAFKA_BROKERS", "localhost:9092"),
            grpc_port=int(os.getenv("GRPC_PORT", "9091")),
            http_port=int(os.getenv("HTTP_PORT", "8083")),
        )
