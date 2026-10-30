import datetime

from google.api import annotations_pb2 as _annotations_pb2
from google.api import field_behavior_pb2 as _field_behavior_pb2
from google.protobuf import timestamp_pb2 as _timestamp_pb2
from google.protobuf import wrappers_pb2 as _wrappers_pb2
from protoc_gen_openapiv2.options import annotations_pb2 as _annotations_pb2_1
from google.protobuf.internal import containers as _containers
from google.protobuf.internal import enum_type_wrapper as _enum_type_wrapper
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from collections.abc import Iterable as _Iterable, Mapping as _Mapping
from typing import ClassVar as _ClassVar, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class AggregationInterval(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = ()
    AGGREGATION_INTERVAL_UNSPECIFIED: _ClassVar[AggregationInterval]
    AGGREGATION_INTERVAL_1M: _ClassVar[AggregationInterval]
    AGGREGATION_INTERVAL_5M: _ClassVar[AggregationInterval]
    AGGREGATION_INTERVAL_1H: _ClassVar[AggregationInterval]
    AGGREGATION_INTERVAL_1D: _ClassVar[AggregationInterval]
AGGREGATION_INTERVAL_UNSPECIFIED: AggregationInterval
AGGREGATION_INTERVAL_1M: AggregationInterval
AGGREGATION_INTERVAL_5M: AggregationInterval
AGGREGATION_INTERVAL_1H: AggregationInterval
AGGREGATION_INTERVAL_1D: AggregationInterval

class Reading(_message.Message):
    __slots__ = ("device_id", "metric", "value", "unit", "time")
    DEVICE_ID_FIELD_NUMBER: _ClassVar[int]
    METRIC_FIELD_NUMBER: _ClassVar[int]
    VALUE_FIELD_NUMBER: _ClassVar[int]
    UNIT_FIELD_NUMBER: _ClassVar[int]
    TIME_FIELD_NUMBER: _ClassVar[int]
    device_id: str
    metric: str
    value: float
    unit: str
    time: _timestamp_pb2.Timestamp
    def __init__(self, device_id: _Optional[str] = ..., metric: _Optional[str] = ..., value: _Optional[float] = ..., unit: _Optional[str] = ..., time: _Optional[_Union[datetime.datetime, _timestamp_pb2.Timestamp, _Mapping]] = ...) -> None: ...

class GetLatestReadingsRequest(_message.Message):
    __slots__ = ("device_id",)
    DEVICE_ID_FIELD_NUMBER: _ClassVar[int]
    device_id: str
    def __init__(self, device_id: _Optional[str] = ...) -> None: ...

class GetLatestReadingsResponse(_message.Message):
    __slots__ = ("items",)
    ITEMS_FIELD_NUMBER: _ClassVar[int]
    items: _containers.RepeatedCompositeFieldContainer[Reading]
    def __init__(self, items: _Optional[_Iterable[_Union[Reading, _Mapping]]] = ...) -> None: ...

class ListReadingsRequest(_message.Message):
    __slots__ = ("device_id", "metric", "to", "interval", "limit")
    DEVICE_ID_FIELD_NUMBER: _ClassVar[int]
    METRIC_FIELD_NUMBER: _ClassVar[int]
    FROM_FIELD_NUMBER: _ClassVar[int]
    TO_FIELD_NUMBER: _ClassVar[int]
    INTERVAL_FIELD_NUMBER: _ClassVar[int]
    LIMIT_FIELD_NUMBER: _ClassVar[int]
    device_id: str
    metric: str
    to: _timestamp_pb2.Timestamp
    interval: AggregationInterval
    limit: int
    def __init__(self, device_id: _Optional[str] = ..., metric: _Optional[str] = ..., to: _Optional[_Union[datetime.datetime, _timestamp_pb2.Timestamp, _Mapping]] = ..., interval: _Optional[_Union[AggregationInterval, str]] = ..., limit: _Optional[int] = ..., **kwargs) -> None: ...

class SeriesPoint(_message.Message):
    __slots__ = ("time", "value", "min", "max")
    TIME_FIELD_NUMBER: _ClassVar[int]
    VALUE_FIELD_NUMBER: _ClassVar[int]
    MIN_FIELD_NUMBER: _ClassVar[int]
    MAX_FIELD_NUMBER: _ClassVar[int]
    time: _timestamp_pb2.Timestamp
    value: float
    min: _wrappers_pb2.DoubleValue
    max: _wrappers_pb2.DoubleValue
    def __init__(self, time: _Optional[_Union[datetime.datetime, _timestamp_pb2.Timestamp, _Mapping]] = ..., value: _Optional[float] = ..., min: _Optional[_Union[_wrappers_pb2.DoubleValue, _Mapping]] = ..., max: _Optional[_Union[_wrappers_pb2.DoubleValue, _Mapping]] = ...) -> None: ...

class ReadingSeries(_message.Message):
    __slots__ = ("device_id", "metric", "unit", "interval", "points")
    DEVICE_ID_FIELD_NUMBER: _ClassVar[int]
    METRIC_FIELD_NUMBER: _ClassVar[int]
    UNIT_FIELD_NUMBER: _ClassVar[int]
    INTERVAL_FIELD_NUMBER: _ClassVar[int]
    POINTS_FIELD_NUMBER: _ClassVar[int]
    device_id: str
    metric: str
    unit: str
    interval: AggregationInterval
    points: _containers.RepeatedCompositeFieldContainer[SeriesPoint]
    def __init__(self, device_id: _Optional[str] = ..., metric: _Optional[str] = ..., unit: _Optional[str] = ..., interval: _Optional[_Union[AggregationInterval, str]] = ..., points: _Optional[_Iterable[_Union[SeriesPoint, _Mapping]]] = ...) -> None: ...

class GetHomeLatestReadingsRequest(_message.Message):
    __slots__ = ("home_id", "metric")
    HOME_ID_FIELD_NUMBER: _ClassVar[int]
    METRIC_FIELD_NUMBER: _ClassVar[int]
    home_id: str
    metric: str
    def __init__(self, home_id: _Optional[str] = ..., metric: _Optional[str] = ...) -> None: ...

class GetHomeLatestReadingsResponse(_message.Message):
    __slots__ = ("items",)
    ITEMS_FIELD_NUMBER: _ClassVar[int]
    items: _containers.RepeatedCompositeFieldContainer[Reading]
    def __init__(self, items: _Optional[_Iterable[_Union[Reading, _Mapping]]] = ...) -> None: ...

class ListAvailabilityRequest(_message.Message):
    __slots__ = ("device_id", "to", "limit")
    DEVICE_ID_FIELD_NUMBER: _ClassVar[int]
    FROM_FIELD_NUMBER: _ClassVar[int]
    TO_FIELD_NUMBER: _ClassVar[int]
    LIMIT_FIELD_NUMBER: _ClassVar[int]
    device_id: str
    to: _timestamp_pb2.Timestamp
    limit: int
    def __init__(self, device_id: _Optional[str] = ..., to: _Optional[_Union[datetime.datetime, _timestamp_pb2.Timestamp, _Mapping]] = ..., limit: _Optional[int] = ..., **kwargs) -> None: ...

class AvailabilityChange(_message.Message):
    __slots__ = ("time", "online")
    TIME_FIELD_NUMBER: _ClassVar[int]
    ONLINE_FIELD_NUMBER: _ClassVar[int]
    time: _timestamp_pb2.Timestamp
    online: bool
    def __init__(self, time: _Optional[_Union[datetime.datetime, _timestamp_pb2.Timestamp, _Mapping]] = ..., online: _Optional[bool] = ...) -> None: ...

class ListAvailabilityResponse(_message.Message):
    __slots__ = ("device_id", "items")
    DEVICE_ID_FIELD_NUMBER: _ClassVar[int]
    ITEMS_FIELD_NUMBER: _ClassVar[int]
    device_id: str
    items: _containers.RepeatedCompositeFieldContainer[AvailabilityChange]
    def __init__(self, device_id: _Optional[str] = ..., items: _Optional[_Iterable[_Union[AvailabilityChange, _Mapping]]] = ...) -> None: ...
