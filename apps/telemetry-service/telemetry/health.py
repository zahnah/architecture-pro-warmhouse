import json
import logging
import threading
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer

from telemetry.repository import Database

log = logging.getLogger(__name__)


class HealthServer:
    """GET /health for docker healthchecks; healthy while the database answers."""

    def __init__(self, port: int, db: Database):
        handler = _handler(db)
        self._server = ThreadingHTTPServer(("0.0.0.0", port), handler)
        self._thread = threading.Thread(target=self._server.serve_forever, name="health", daemon=True)

    def start(self) -> None:
        self._thread.start()

    def stop(self) -> None:
        self._server.shutdown()
        self._server.server_close()


def _handler(db: Database) -> type[BaseHTTPRequestHandler]:
    class Handler(BaseHTTPRequestHandler):
        def do_GET(self):
            if self.path != "/health":
                self._reply(404, {"error": "not found"})
                return
            try:
                db.ping()
            except Exception as e:
                self._reply(503, {"status": "down", "error": str(e)})
                return
            self._reply(200, {"status": "ok"})

        def _reply(self, code: int, body: dict) -> None:
            payload = json.dumps(body).encode()
            self.send_response(code)
            self.send_header("Content-Type", "application/json")
            self.send_header("Content-Length", str(len(payload)))
            self.end_headers()
            self.wfile.write(payload)

        def log_message(self, format, *args):
            log.debug(format, *args)

    return Handler
