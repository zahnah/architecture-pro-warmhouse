package ru.warmhouse.heating.v1;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 * <pre>
 * Настройки и состояние термостатов.
 * </pre>
 */
@io.grpc.stub.annotations.GrpcGenerated
public final class HeatingServiceGrpc {

  private HeatingServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "warmhouse.heating.v1.HeatingService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<ru.warmhouse.heating.v1.ListThermostatsRequest,
      ru.warmhouse.heating.v1.ListThermostatsResponse> getListThermostatsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ListThermostats",
      requestType = ru.warmhouse.heating.v1.ListThermostatsRequest.class,
      responseType = ru.warmhouse.heating.v1.ListThermostatsResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<ru.warmhouse.heating.v1.ListThermostatsRequest,
      ru.warmhouse.heating.v1.ListThermostatsResponse> getListThermostatsMethod() {
    io.grpc.MethodDescriptor<ru.warmhouse.heating.v1.ListThermostatsRequest, ru.warmhouse.heating.v1.ListThermostatsResponse> getListThermostatsMethod;
    if ((getListThermostatsMethod = HeatingServiceGrpc.getListThermostatsMethod) == null) {
      synchronized (HeatingServiceGrpc.class) {
        if ((getListThermostatsMethod = HeatingServiceGrpc.getListThermostatsMethod) == null) {
          HeatingServiceGrpc.getListThermostatsMethod = getListThermostatsMethod =
              io.grpc.MethodDescriptor.<ru.warmhouse.heating.v1.ListThermostatsRequest, ru.warmhouse.heating.v1.ListThermostatsResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ListThermostats"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ru.warmhouse.heating.v1.ListThermostatsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ru.warmhouse.heating.v1.ListThermostatsResponse.getDefaultInstance()))
              .setSchemaDescriptor(new HeatingServiceMethodDescriptorSupplier("ListThermostats"))
              .build();
        }
      }
    }
    return getListThermostatsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<ru.warmhouse.heating.v1.GetThermostatRequest,
      ru.warmhouse.heating.v1.Thermostat> getGetThermostatMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetThermostat",
      requestType = ru.warmhouse.heating.v1.GetThermostatRequest.class,
      responseType = ru.warmhouse.heating.v1.Thermostat.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<ru.warmhouse.heating.v1.GetThermostatRequest,
      ru.warmhouse.heating.v1.Thermostat> getGetThermostatMethod() {
    io.grpc.MethodDescriptor<ru.warmhouse.heating.v1.GetThermostatRequest, ru.warmhouse.heating.v1.Thermostat> getGetThermostatMethod;
    if ((getGetThermostatMethod = HeatingServiceGrpc.getGetThermostatMethod) == null) {
      synchronized (HeatingServiceGrpc.class) {
        if ((getGetThermostatMethod = HeatingServiceGrpc.getGetThermostatMethod) == null) {
          HeatingServiceGrpc.getGetThermostatMethod = getGetThermostatMethod =
              io.grpc.MethodDescriptor.<ru.warmhouse.heating.v1.GetThermostatRequest, ru.warmhouse.heating.v1.Thermostat>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetThermostat"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ru.warmhouse.heating.v1.GetThermostatRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ru.warmhouse.heating.v1.Thermostat.getDefaultInstance()))
              .setSchemaDescriptor(new HeatingServiceMethodDescriptorSupplier("GetThermostat"))
              .build();
        }
      }
    }
    return getGetThermostatMethod;
  }

  private static volatile io.grpc.MethodDescriptor<ru.warmhouse.heating.v1.SetTargetRequest,
      ru.warmhouse.heating.v1.Thermostat> getSetTargetMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SetTarget",
      requestType = ru.warmhouse.heating.v1.SetTargetRequest.class,
      responseType = ru.warmhouse.heating.v1.Thermostat.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<ru.warmhouse.heating.v1.SetTargetRequest,
      ru.warmhouse.heating.v1.Thermostat> getSetTargetMethod() {
    io.grpc.MethodDescriptor<ru.warmhouse.heating.v1.SetTargetRequest, ru.warmhouse.heating.v1.Thermostat> getSetTargetMethod;
    if ((getSetTargetMethod = HeatingServiceGrpc.getSetTargetMethod) == null) {
      synchronized (HeatingServiceGrpc.class) {
        if ((getSetTargetMethod = HeatingServiceGrpc.getSetTargetMethod) == null) {
          HeatingServiceGrpc.getSetTargetMethod = getSetTargetMethod =
              io.grpc.MethodDescriptor.<ru.warmhouse.heating.v1.SetTargetRequest, ru.warmhouse.heating.v1.Thermostat>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SetTarget"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ru.warmhouse.heating.v1.SetTargetRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ru.warmhouse.heating.v1.Thermostat.getDefaultInstance()))
              .setSchemaDescriptor(new HeatingServiceMethodDescriptorSupplier("SetTarget"))
              .build();
        }
      }
    }
    return getSetTargetMethod;
  }

  private static volatile io.grpc.MethodDescriptor<ru.warmhouse.heating.v1.SetModeRequest,
      ru.warmhouse.heating.v1.Thermostat> getSetModeMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SetMode",
      requestType = ru.warmhouse.heating.v1.SetModeRequest.class,
      responseType = ru.warmhouse.heating.v1.Thermostat.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<ru.warmhouse.heating.v1.SetModeRequest,
      ru.warmhouse.heating.v1.Thermostat> getSetModeMethod() {
    io.grpc.MethodDescriptor<ru.warmhouse.heating.v1.SetModeRequest, ru.warmhouse.heating.v1.Thermostat> getSetModeMethod;
    if ((getSetModeMethod = HeatingServiceGrpc.getSetModeMethod) == null) {
      synchronized (HeatingServiceGrpc.class) {
        if ((getSetModeMethod = HeatingServiceGrpc.getSetModeMethod) == null) {
          HeatingServiceGrpc.getSetModeMethod = getSetModeMethod =
              io.grpc.MethodDescriptor.<ru.warmhouse.heating.v1.SetModeRequest, ru.warmhouse.heating.v1.Thermostat>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SetMode"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ru.warmhouse.heating.v1.SetModeRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ru.warmhouse.heating.v1.Thermostat.getDefaultInstance()))
              .setSchemaDescriptor(new HeatingServiceMethodDescriptorSupplier("SetMode"))
              .build();
        }
      }
    }
    return getSetModeMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static HeatingServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<HeatingServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<HeatingServiceStub>() {
        @java.lang.Override
        public HeatingServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new HeatingServiceStub(channel, callOptions);
        }
      };
    return HeatingServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports all types of calls on the service
   */
  public static HeatingServiceBlockingV2Stub newBlockingV2Stub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<HeatingServiceBlockingV2Stub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<HeatingServiceBlockingV2Stub>() {
        @java.lang.Override
        public HeatingServiceBlockingV2Stub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new HeatingServiceBlockingV2Stub(channel, callOptions);
        }
      };
    return HeatingServiceBlockingV2Stub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static HeatingServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<HeatingServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<HeatingServiceBlockingStub>() {
        @java.lang.Override
        public HeatingServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new HeatingServiceBlockingStub(channel, callOptions);
        }
      };
    return HeatingServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static HeatingServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<HeatingServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<HeatingServiceFutureStub>() {
        @java.lang.Override
        public HeatingServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new HeatingServiceFutureStub(channel, callOptions);
        }
      };
    return HeatingServiceFutureStub.newStub(factory, channel);
  }

  /**
   * <pre>
   * Настройки и состояние термостатов.
   * </pre>
   */
  public interface AsyncService {

    /**
     * <pre>
     * Термостаты дома
     * </pre>
     */
    default void listThermostats(ru.warmhouse.heating.v1.ListThermostatsRequest request,
        io.grpc.stub.StreamObserver<ru.warmhouse.heating.v1.ListThermostatsResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getListThermostatsMethod(), responseObserver);
    }

    /**
     * <pre>
     * Желаемые и фактические настройки, статус в сети
     * </pre>
     */
    default void getThermostat(ru.warmhouse.heating.v1.GetThermostatRequest request,
        io.grpc.stub.StreamObserver<ru.warmhouse.heating.v1.Thermostat> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetThermostatMethod(), responseObserver);
    }

    /**
     * <pre>
     * Задать целевую температуру; выключенный термостат переходит в HEATING_MODE_MANUAL
     * </pre>
     */
    default void setTarget(ru.warmhouse.heating.v1.SetTargetRequest request,
        io.grpc.stub.StreamObserver<ru.warmhouse.heating.v1.Thermostat> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSetTargetMethod(), responseObserver);
    }

    /**
     * <pre>
     * Включить, выключить отопление или выбрать режим
     * </pre>
     */
    default void setMode(ru.warmhouse.heating.v1.SetModeRequest request,
        io.grpc.stub.StreamObserver<ru.warmhouse.heating.v1.Thermostat> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSetModeMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service HeatingService.
   * <pre>
   * Настройки и состояние термостатов.
   * </pre>
   */
  public static abstract class HeatingServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return HeatingServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service HeatingService.
   * <pre>
   * Настройки и состояние термостатов.
   * </pre>
   */
  public static final class HeatingServiceStub
      extends io.grpc.stub.AbstractAsyncStub<HeatingServiceStub> {
    private HeatingServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected HeatingServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new HeatingServiceStub(channel, callOptions);
    }

    /**
     * <pre>
     * Термостаты дома
     * </pre>
     */
    public void listThermostats(ru.warmhouse.heating.v1.ListThermostatsRequest request,
        io.grpc.stub.StreamObserver<ru.warmhouse.heating.v1.ListThermostatsResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getListThermostatsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Желаемые и фактические настройки, статус в сети
     * </pre>
     */
    public void getThermostat(ru.warmhouse.heating.v1.GetThermostatRequest request,
        io.grpc.stub.StreamObserver<ru.warmhouse.heating.v1.Thermostat> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetThermostatMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Задать целевую температуру; выключенный термостат переходит в HEATING_MODE_MANUAL
     * </pre>
     */
    public void setTarget(ru.warmhouse.heating.v1.SetTargetRequest request,
        io.grpc.stub.StreamObserver<ru.warmhouse.heating.v1.Thermostat> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSetTargetMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Включить, выключить отопление или выбрать режим
     * </pre>
     */
    public void setMode(ru.warmhouse.heating.v1.SetModeRequest request,
        io.grpc.stub.StreamObserver<ru.warmhouse.heating.v1.Thermostat> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSetModeMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service HeatingService.
   * <pre>
   * Настройки и состояние термостатов.
   * </pre>
   */
  public static final class HeatingServiceBlockingV2Stub
      extends io.grpc.stub.AbstractBlockingStub<HeatingServiceBlockingV2Stub> {
    private HeatingServiceBlockingV2Stub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected HeatingServiceBlockingV2Stub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new HeatingServiceBlockingV2Stub(channel, callOptions);
    }

    /**
     * <pre>
     * Термостаты дома
     * </pre>
     */
    public ru.warmhouse.heating.v1.ListThermostatsResponse listThermostats(ru.warmhouse.heating.v1.ListThermostatsRequest request) throws io.grpc.StatusException {
      return io.grpc.stub.ClientCalls.blockingV2UnaryCall(
          getChannel(), getListThermostatsMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Желаемые и фактические настройки, статус в сети
     * </pre>
     */
    public ru.warmhouse.heating.v1.Thermostat getThermostat(ru.warmhouse.heating.v1.GetThermostatRequest request) throws io.grpc.StatusException {
      return io.grpc.stub.ClientCalls.blockingV2UnaryCall(
          getChannel(), getGetThermostatMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Задать целевую температуру; выключенный термостат переходит в HEATING_MODE_MANUAL
     * </pre>
     */
    public ru.warmhouse.heating.v1.Thermostat setTarget(ru.warmhouse.heating.v1.SetTargetRequest request) throws io.grpc.StatusException {
      return io.grpc.stub.ClientCalls.blockingV2UnaryCall(
          getChannel(), getSetTargetMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Включить, выключить отопление или выбрать режим
     * </pre>
     */
    public ru.warmhouse.heating.v1.Thermostat setMode(ru.warmhouse.heating.v1.SetModeRequest request) throws io.grpc.StatusException {
      return io.grpc.stub.ClientCalls.blockingV2UnaryCall(
          getChannel(), getSetModeMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do limited synchronous rpc calls to service HeatingService.
   * <pre>
   * Настройки и состояние термостатов.
   * </pre>
   */
  public static final class HeatingServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<HeatingServiceBlockingStub> {
    private HeatingServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected HeatingServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new HeatingServiceBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * Термостаты дома
     * </pre>
     */
    public ru.warmhouse.heating.v1.ListThermostatsResponse listThermostats(ru.warmhouse.heating.v1.ListThermostatsRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getListThermostatsMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Желаемые и фактические настройки, статус в сети
     * </pre>
     */
    public ru.warmhouse.heating.v1.Thermostat getThermostat(ru.warmhouse.heating.v1.GetThermostatRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetThermostatMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Задать целевую температуру; выключенный термостат переходит в HEATING_MODE_MANUAL
     * </pre>
     */
    public ru.warmhouse.heating.v1.Thermostat setTarget(ru.warmhouse.heating.v1.SetTargetRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSetTargetMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Включить, выключить отопление или выбрать режим
     * </pre>
     */
    public ru.warmhouse.heating.v1.Thermostat setMode(ru.warmhouse.heating.v1.SetModeRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSetModeMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service HeatingService.
   * <pre>
   * Настройки и состояние термостатов.
   * </pre>
   */
  public static final class HeatingServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<HeatingServiceFutureStub> {
    private HeatingServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected HeatingServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new HeatingServiceFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * Термостаты дома
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<ru.warmhouse.heating.v1.ListThermostatsResponse> listThermostats(
        ru.warmhouse.heating.v1.ListThermostatsRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getListThermostatsMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Желаемые и фактические настройки, статус в сети
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<ru.warmhouse.heating.v1.Thermostat> getThermostat(
        ru.warmhouse.heating.v1.GetThermostatRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetThermostatMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Задать целевую температуру; выключенный термостат переходит в HEATING_MODE_MANUAL
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<ru.warmhouse.heating.v1.Thermostat> setTarget(
        ru.warmhouse.heating.v1.SetTargetRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSetTargetMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Включить, выключить отопление или выбрать режим
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<ru.warmhouse.heating.v1.Thermostat> setMode(
        ru.warmhouse.heating.v1.SetModeRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSetModeMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_LIST_THERMOSTATS = 0;
  private static final int METHODID_GET_THERMOSTAT = 1;
  private static final int METHODID_SET_TARGET = 2;
  private static final int METHODID_SET_MODE = 3;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_LIST_THERMOSTATS:
          serviceImpl.listThermostats((ru.warmhouse.heating.v1.ListThermostatsRequest) request,
              (io.grpc.stub.StreamObserver<ru.warmhouse.heating.v1.ListThermostatsResponse>) responseObserver);
          break;
        case METHODID_GET_THERMOSTAT:
          serviceImpl.getThermostat((ru.warmhouse.heating.v1.GetThermostatRequest) request,
              (io.grpc.stub.StreamObserver<ru.warmhouse.heating.v1.Thermostat>) responseObserver);
          break;
        case METHODID_SET_TARGET:
          serviceImpl.setTarget((ru.warmhouse.heating.v1.SetTargetRequest) request,
              (io.grpc.stub.StreamObserver<ru.warmhouse.heating.v1.Thermostat>) responseObserver);
          break;
        case METHODID_SET_MODE:
          serviceImpl.setMode((ru.warmhouse.heating.v1.SetModeRequest) request,
              (io.grpc.stub.StreamObserver<ru.warmhouse.heating.v1.Thermostat>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getListThermostatsMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              ru.warmhouse.heating.v1.ListThermostatsRequest,
              ru.warmhouse.heating.v1.ListThermostatsResponse>(
                service, METHODID_LIST_THERMOSTATS)))
        .addMethod(
          getGetThermostatMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              ru.warmhouse.heating.v1.GetThermostatRequest,
              ru.warmhouse.heating.v1.Thermostat>(
                service, METHODID_GET_THERMOSTAT)))
        .addMethod(
          getSetTargetMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              ru.warmhouse.heating.v1.SetTargetRequest,
              ru.warmhouse.heating.v1.Thermostat>(
                service, METHODID_SET_TARGET)))
        .addMethod(
          getSetModeMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              ru.warmhouse.heating.v1.SetModeRequest,
              ru.warmhouse.heating.v1.Thermostat>(
                service, METHODID_SET_MODE)))
        .build();
  }

  private static abstract class HeatingServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    HeatingServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return ru.warmhouse.heating.v1.HeatingServiceOuterClass.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("HeatingService");
    }
  }

  private static final class HeatingServiceFileDescriptorSupplier
      extends HeatingServiceBaseDescriptorSupplier {
    HeatingServiceFileDescriptorSupplier() {}
  }

  private static final class HeatingServiceMethodDescriptorSupplier
      extends HeatingServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    HeatingServiceMethodDescriptorSupplier(java.lang.String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (HeatingServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new HeatingServiceFileDescriptorSupplier())
              .addMethod(getListThermostatsMethod())
              .addMethod(getGetThermostatMethod())
              .addMethod(getSetTargetMethod())
              .addMethod(getSetModeMethod())
              .build();
        }
      }
    }
    return result;
  }
}
