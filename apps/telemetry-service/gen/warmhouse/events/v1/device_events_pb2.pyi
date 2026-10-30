import datetime

from google.protobuf import any_pb2 as _any_pb2
from google.protobuf import timestamp_pb2 as _timestamp_pb2
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from collections.abc import Mapping as _Mapping
from typing import ClassVar as _ClassVar, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class DeviceRegistered(_message.Message):
    __slots__ = ("event_id", "occurred_at", "device_id", "home_id", "type", "model_id")
    EVENT_ID_FIELD_NUMBER: _ClassVar[int]
    OCCURRED_AT_FIELD_NUMBER: _ClassVar[int]
    DEVICE_ID_FIELD_NUMBER: _ClassVar[int]
    HOME_ID_FIELD_NUMBER: _ClassVar[int]
    TYPE_FIELD_NUMBER: _ClassVar[int]
    MODEL_ID_FIELD_NUMBER: _ClassVar[int]
    event_id: str
    occurred_at: _timestamp_pb2.Timestamp
    device_id: str
    home_id: str
    type: str
    model_id: str
    def __init__(self, event_id: _Optional[str] = ..., occurred_at: _Optional[_Union[datetime.datetime, _timestamp_pb2.Timestamp, _Mapping]] = ..., device_id: _Optional[str] = ..., home_id: _Optional[str] = ..., type: _Optional[str] = ..., model_id: _Optional[str] = ...) -> None: ...

class DeviceUnregistered(_message.Message):
    __slots__ = ("event_id", "occurred_at", "device_id", "home_id")
    EVENT_ID_FIELD_NUMBER: _ClassVar[int]
    OCCURRED_AT_FIELD_NUMBER: _ClassVar[int]
    DEVICE_ID_FIELD_NUMBER: _ClassVar[int]
    HOME_ID_FIELD_NUMBER: _ClassVar[int]
    event_id: str
    occurred_at: _timestamp_pb2.Timestamp
    device_id: str
    home_id: str
    def __init__(self, event_id: _Optional[str] = ..., occurred_at: _Optional[_Union[datetime.datetime, _timestamp_pb2.Timestamp, _Mapping]] = ..., device_id: _Optional[str] = ..., home_id: _Optional[str] = ...) -> None: ...

class TelemetryReading(_message.Message):
    __slots__ = ("event_id", "occurred_at", "device_id", "home_id", "metric", "value", "unit", "measured_at")
    EVENT_ID_FIELD_NUMBER: _ClassVar[int]
    OCCURRED_AT_FIELD_NUMBER: _ClassVar[int]
    DEVICE_ID_FIELD_NUMBER: _ClassVar[int]
    HOME_ID_FIELD_NUMBER: _ClassVar[int]
    METRIC_FIELD_NUMBER: _ClassVar[int]
    VALUE_FIELD_NUMBER: _ClassVar[int]
    UNIT_FIELD_NUMBER: _ClassVar[int]
    MEASURED_AT_FIELD_NUMBER: _ClassVar[int]
    event_id: str
    occurred_at: _timestamp_pb2.Timestamp
    device_id: str
    home_id: str
    metric: str
    value: float
    unit: str
    measured_at: _timestamp_pb2.Timestamp
    def __init__(self, event_id: _Optional[str] = ..., occurred_at: _Optional[_Union[datetime.datetime, _timestamp_pb2.Timestamp, _Mapping]] = ..., device_id: _Optional[str] = ..., home_id: _Optional[str] = ..., metric: _Optional[str] = ..., value: _Optional[float] = ..., unit: _Optional[str] = ..., measured_at: _Optional[_Union[datetime.datetime, _timestamp_pb2.Timestamp, _Mapping]] = ...) -> None: ...

class DeviceStateChanged(_message.Message):
    __slots__ = ("event_id", "occurred_at", "device_id", "home_id", "type", "state")
    EVENT_ID_FIELD_NUMBER: _ClassVar[int]
    OCCURRED_AT_FIELD_NUMBER: _ClassVar[int]
    DEVICE_ID_FIELD_NUMBER: _ClassVar[int]
    HOME_ID_FIELD_NUMBER: _ClassVar[int]
    TYPE_FIELD_NUMBER: _ClassVar[int]
    STATE_FIELD_NUMBER: _ClassVar[int]
    event_id: str
    occurred_at: _timestamp_pb2.Timestamp
    device_id: str
    home_id: str
    type: str
    state: _any_pb2.Any
    def __init__(self, event_id: _Optional[str] = ..., occurred_at: _Optional[_Union[datetime.datetime, _timestamp_pb2.Timestamp, _Mapping]] = ..., device_id: _Optional[str] = ..., home_id: _Optional[str] = ..., type: _Optional[str] = ..., state: _Optional[_Union[_any_pb2.Any, _Mapping]] = ...) -> None: ...

class DeviceConnectivityChanged(_message.Message):
    __slots__ = ("event_id", "occurred_at", "device_id", "home_id", "online")
    EVENT_ID_FIELD_NUMBER: _ClassVar[int]
    OCCURRED_AT_FIELD_NUMBER: _ClassVar[int]
    DEVICE_ID_FIELD_NUMBER: _ClassVar[int]
    HOME_ID_FIELD_NUMBER: _ClassVar[int]
    ONLINE_FIELD_NUMBER: _ClassVar[int]
    event_id: str
    occurred_at: _timestamp_pb2.Timestamp
    device_id: str
    home_id: str
    online: bool
    def __init__(self, event_id: _Optional[str] = ..., occurred_at: _Optional[_Union[datetime.datetime, _timestamp_pb2.Timestamp, _Mapping]] = ..., device_id: _Optional[str] = ..., home_id: _Optional[str] = ..., online: _Optional[bool] = ...) -> None: ...
