import logging
import signal
import threading
from concurrent import futures

import grpc
from grpc_health.v1 import health, health_pb2, health_pb2_grpc
from grpc_reflection.v1alpha import reflection
from warmhouse.telemetry.v1 import telemetry_service_pb2 as pb
from warmhouse.telemetry.v1 import telemetry_service_pb2_grpc as pb_grpc

from telemetry.consumer import TelemetryConsumer
from telemetry.grpc_api import TelemetryServicer
from telemetry.health import HealthServer
from telemetry.repository import AvailabilityRepository, Database, ReadingRepository
from telemetry.settings import Settings

log = logging.getLogger("telemetry")


class Application:
    def __init__(self, settings: Settings):
        self.db = Database(settings.database_url)
        self.db.migrate()
        readings = ReadingRepository(self.db)
        availability = AvailabilityRepository(self.db)

        self.consumer = TelemetryConsumer(settings.kafka_brokers, readings, availability)

        self.grpc = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
        pb_grpc.add_TelemetryServiceServicer_to_server(TelemetryServicer(readings, availability), self.grpc)
        health_servicer = health.HealthServicer()
        health_servicer.set("", health_pb2.HealthCheckResponse.SERVING)
        health_pb2_grpc.add_HealthServicer_to_server(health_servicer, self.grpc)
        reflection.enable_server_reflection(
            (pb.DESCRIPTOR.services_by_name["TelemetryService"].full_name, reflection.SERVICE_NAME), self.grpc)
        self.grpc.add_insecure_port(f"[::]:{settings.grpc_port}")
        self.grpc_port = settings.grpc_port

        self.health = HealthServer(settings.http_port, self.db)

    def run(self) -> None:
        stopped = threading.Event()
        signal.signal(signal.SIGTERM, lambda *_: stopped.set())
        signal.signal(signal.SIGINT, lambda *_: stopped.set())

        self.consumer.start()
        self.grpc.start()
        self.health.start()
        log.info("Telemetry service started, gRPC on %d", self.grpc_port)

        stopped.wait()
        log.info("Shutting down")
        self.health.stop()
        self.grpc.stop(grace=5).wait()
        self.consumer.stop()
        self.db.close()


def main() -> None:
    logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(name)s: %(message)s")
    Application(Settings.from_env()).run()


if __name__ == "__main__":
    main()
