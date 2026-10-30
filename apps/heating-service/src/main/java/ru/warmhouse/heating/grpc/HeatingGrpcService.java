package ru.warmhouse.heating.grpc;

import java.util.function.Supplier;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Component;

import ru.warmhouse.heating.domain.ThermostatNotFoundException;
import ru.warmhouse.heating.proto.ProtoMapper;
import ru.warmhouse.heating.service.HeatingService;
import ru.warmhouse.heating.v1.GetThermostatRequest;
import ru.warmhouse.heating.v1.HeatingServiceGrpc;
import ru.warmhouse.heating.v1.ListThermostatsRequest;
import ru.warmhouse.heating.v1.ListThermostatsResponse;
import ru.warmhouse.heating.v1.SetModeRequest;
import ru.warmhouse.heating.v1.SetTargetRequest;
import ru.warmhouse.heating.v1.Thermostat;

@Component
public class HeatingGrpcService extends HeatingServiceGrpc.HeatingServiceImplBase {

    private final HeatingService heating;

    public HeatingGrpcService(HeatingService heating) {
        this.heating = heating;
    }

    @Override
    public void listThermostats(ListThermostatsRequest request, StreamObserver<ListThermostatsResponse> response) {
        respond(response, () -> {
            require(request.getHomeId(), "home_id");
            return ListThermostatsResponse.newBuilder()
                    .addAllItems(heating.listByHome(request.getHomeId()).stream().map(ProtoMapper::toProto).toList())
                    .build();
        });
    }

    @Override
    public void getThermostat(GetThermostatRequest request, StreamObserver<Thermostat> response) {
        respond(response, () -> {
            require(request.getDeviceId(), "device_id");
            return ProtoMapper.toProto(heating.get(request.getDeviceId()));
        });
    }

    @Override
    public void setTarget(SetTargetRequest request, StreamObserver<Thermostat> response) {
        respond(response, () -> {
            require(request.getDeviceId(), "device_id");
            return ProtoMapper.toProto(heating.setTarget(request.getDeviceId(), request.getTemperature()));
        });
    }

    @Override
    public void setMode(SetModeRequest request, StreamObserver<Thermostat> response) {
        respond(response, () -> {
            require(request.getDeviceId(), "device_id");
            return ProtoMapper.toProto(heating.setMode(request.getDeviceId(), ProtoMapper.fromProto(request.getMode())));
        });
    }

    private static void require(String value, String field) {
        if (value.isEmpty()) {
            throw new IllegalArgumentException(field + " is required");
        }
    }

    private static <T> void respond(StreamObserver<T> response, Supplier<T> call) {
        T result;
        try {
            result = call.get();
        } catch (ThermostatNotFoundException e) {
            response.onError(Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
            return;
        } catch (IllegalArgumentException e) {
            response.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
            return;
        }
        response.onNext(result);
        response.onCompleted();
    }
}
