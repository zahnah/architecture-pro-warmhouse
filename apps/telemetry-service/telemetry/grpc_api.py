from datetime import datetime, timezone

import grpc
from google.protobuf.timestamp_pb2 import Timestamp
from google.protobuf.wrappers_pb2 import DoubleValue
from warmhouse.telemetry.v1 import telemetry_service_pb2 as pb
from warmhouse.telemetry.v1 import telemetry_service_pb2_grpc as pb_grpc

from telemetry.repository import AvailabilityRepository, Reading, ReadingRepository

DEFAULT_LIMIT = 1000
MAX_LIMIT = 10000


class TelemetryServicer(pb_grpc.TelemetryServiceServicer):
    """TelemetryService of telemetry_service.proto; read-only, data comes from Kafka."""

    def __init__(self, readings: ReadingRepository, availability: AvailabilityRepository):
        self._readings = readings
        self._availability = availability

    def GetLatestReadings(self, request, context):
        _require(context, request.device_id, "device_id")
        items = self._readings.latest_for_device(request.device_id)
        if not items:
            context.abort(grpc.StatusCode.NOT_FOUND, f"no readings for device {request.device_id}")
        return pb.GetLatestReadingsResponse(items=[_reading(r) for r in items])

    def GetHomeLatestReadings(self, request, context):
        _require(context, request.home_id, "home_id")
        items = self._readings.latest_for_home(request.home_id, request.metric)
        return pb.GetHomeLatestReadingsResponse(items=[_reading(r) for r in items])

    def ListReadings(self, request, context):
        _require(context, request.device_id, "device_id")
        _require(context, request.metric, "metric")
        start, end = _period(context, request)
        limit = _limit(context, request.limit)
        points = self._readings.series(request.device_id, request.metric, start, end, request.interval, limit)
        series = pb.ReadingSeries(
            device_id=request.device_id,
            metric=request.metric,
            unit=self._readings.unit(request.device_id, request.metric),
            interval=request.interval,
        )
        for p in points:
            point = series.points.add(time=_timestamp(p.time), value=p.value)
            if p.min is not None:
                point.min.CopyFrom(DoubleValue(value=p.min))
            if p.max is not None:
                point.max.CopyFrom(DoubleValue(value=p.max))
        return series

    def ListAvailability(self, request, context):
        _require(context, request.device_id, "device_id")
        start, end = _period(context, request)
        limit = _limit(context, request.limit)
        changes = self._availability.changes(request.device_id, start, end, limit)
        return pb.ListAvailabilityResponse(
            device_id=request.device_id,
            items=[pb.AvailabilityChange(time=_timestamp(c.time), online=c.online) for c in changes],
        )


def _require(context, value: str, field: str) -> None:
    if not value:
        context.abort(grpc.StatusCode.INVALID_ARGUMENT, f"{field} is required")


def _period(context, request) -> tuple[datetime, datetime]:
    # "from" is a Python keyword, so the field is read with getattr
    if not request.HasField("from"):
        context.abort(grpc.StatusCode.INVALID_ARGUMENT, "from is required")
    start = getattr(request, "from").ToDatetime(tzinfo=timezone.utc)
    end = request.to.ToDatetime(tzinfo=timezone.utc) if request.HasField("to") else datetime.now(timezone.utc)
    if end <= start:
        context.abort(grpc.StatusCode.INVALID_ARGUMENT, "to must be after from")
    return start, end


def _limit(context, limit: int) -> int:
    if limit == 0:
        return DEFAULT_LIMIT
    if limit < 0 or limit > MAX_LIMIT:
        context.abort(grpc.StatusCode.INVALID_ARGUMENT, f"limit must be between 1 and {MAX_LIMIT}")
    return limit


def _reading(r: Reading) -> pb.Reading:
    return pb.Reading(device_id=r.device_id, metric=r.metric, value=r.value, unit=r.unit, time=_timestamp(r.time))


def _timestamp(value: datetime) -> Timestamp:
    ts = Timestamp()
    ts.FromDatetime(value)
    return ts
