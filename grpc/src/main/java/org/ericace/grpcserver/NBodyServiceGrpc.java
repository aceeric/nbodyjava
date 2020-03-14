package org.ericace.grpcserver;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 * <pre>
 * Defines a gRPC Service that enables entities external to the running JVM to view / modify
 * simulation configurables, thus changing the behavior of the simulation on the fly
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.28.0)",
    comments = "Source: nbodyservice.proto")
public final class NBodyServiceGrpc {

  private NBodyServiceGrpc() {}

  public static final String SERVICE_NAME = "nbodyservice.NBodyService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<org.ericace.grpcserver.ItemCount,
      org.ericace.grpcserver.ResultCode> getSetComputationThreadsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SetComputationThreads",
      requestType = org.ericace.grpcserver.ItemCount.class,
      responseType = org.ericace.grpcserver.ResultCode.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.ericace.grpcserver.ItemCount,
      org.ericace.grpcserver.ResultCode> getSetComputationThreadsMethod() {
    io.grpc.MethodDescriptor<org.ericace.grpcserver.ItemCount, org.ericace.grpcserver.ResultCode> getSetComputationThreadsMethod;
    if ((getSetComputationThreadsMethod = NBodyServiceGrpc.getSetComputationThreadsMethod) == null) {
      synchronized (NBodyServiceGrpc.class) {
        if ((getSetComputationThreadsMethod = NBodyServiceGrpc.getSetComputationThreadsMethod) == null) {
          NBodyServiceGrpc.getSetComputationThreadsMethod = getSetComputationThreadsMethod =
              io.grpc.MethodDescriptor.<org.ericace.grpcserver.ItemCount, org.ericace.grpcserver.ResultCode>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SetComputationThreads"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.ericace.grpcserver.ItemCount.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.ericace.grpcserver.ResultCode.getDefaultInstance()))
              .setSchemaDescriptor(new NBodyServiceMethodDescriptorSupplier("SetComputationThreads"))
              .build();
        }
      }
    }
    return getSetComputationThreadsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.ericace.grpcserver.ItemCount,
      org.ericace.grpcserver.ResultCode> getSetResultQueueSizeMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SetResultQueueSize",
      requestType = org.ericace.grpcserver.ItemCount.class,
      responseType = org.ericace.grpcserver.ResultCode.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.ericace.grpcserver.ItemCount,
      org.ericace.grpcserver.ResultCode> getSetResultQueueSizeMethod() {
    io.grpc.MethodDescriptor<org.ericace.grpcserver.ItemCount, org.ericace.grpcserver.ResultCode> getSetResultQueueSizeMethod;
    if ((getSetResultQueueSizeMethod = NBodyServiceGrpc.getSetResultQueueSizeMethod) == null) {
      synchronized (NBodyServiceGrpc.class) {
        if ((getSetResultQueueSizeMethod = NBodyServiceGrpc.getSetResultQueueSizeMethod) == null) {
          NBodyServiceGrpc.getSetResultQueueSizeMethod = getSetResultQueueSizeMethod =
              io.grpc.MethodDescriptor.<org.ericace.grpcserver.ItemCount, org.ericace.grpcserver.ResultCode>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SetResultQueueSize"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.ericace.grpcserver.ItemCount.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.ericace.grpcserver.ResultCode.getDefaultInstance()))
              .setSchemaDescriptor(new NBodyServiceMethodDescriptorSupplier("SetResultQueueSize"))
              .build();
        }
      }
    }
    return getSetResultQueueSizeMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.ericace.grpcserver.Factor,
      org.ericace.grpcserver.ResultCode> getSetSmoothingMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SetSmoothing",
      requestType = org.ericace.grpcserver.Factor.class,
      responseType = org.ericace.grpcserver.ResultCode.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.ericace.grpcserver.Factor,
      org.ericace.grpcserver.ResultCode> getSetSmoothingMethod() {
    io.grpc.MethodDescriptor<org.ericace.grpcserver.Factor, org.ericace.grpcserver.ResultCode> getSetSmoothingMethod;
    if ((getSetSmoothingMethod = NBodyServiceGrpc.getSetSmoothingMethod) == null) {
      synchronized (NBodyServiceGrpc.class) {
        if ((getSetSmoothingMethod = NBodyServiceGrpc.getSetSmoothingMethod) == null) {
          NBodyServiceGrpc.getSetSmoothingMethod = getSetSmoothingMethod =
              io.grpc.MethodDescriptor.<org.ericace.grpcserver.Factor, org.ericace.grpcserver.ResultCode>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SetSmoothing"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.ericace.grpcserver.Factor.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.ericace.grpcserver.ResultCode.getDefaultInstance()))
              .setSchemaDescriptor(new NBodyServiceMethodDescriptorSupplier("SetSmoothing"))
              .build();
        }
      }
    }
    return getSetSmoothingMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.ericace.grpcserver.RestitutionCoefficient,
      org.ericace.grpcserver.ResultCode> getSetRestitutionCoefficientMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SetRestitutionCoefficient",
      requestType = org.ericace.grpcserver.RestitutionCoefficient.class,
      responseType = org.ericace.grpcserver.ResultCode.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.ericace.grpcserver.RestitutionCoefficient,
      org.ericace.grpcserver.ResultCode> getSetRestitutionCoefficientMethod() {
    io.grpc.MethodDescriptor<org.ericace.grpcserver.RestitutionCoefficient, org.ericace.grpcserver.ResultCode> getSetRestitutionCoefficientMethod;
    if ((getSetRestitutionCoefficientMethod = NBodyServiceGrpc.getSetRestitutionCoefficientMethod) == null) {
      synchronized (NBodyServiceGrpc.class) {
        if ((getSetRestitutionCoefficientMethod = NBodyServiceGrpc.getSetRestitutionCoefficientMethod) == null) {
          NBodyServiceGrpc.getSetRestitutionCoefficientMethod = getSetRestitutionCoefficientMethod =
              io.grpc.MethodDescriptor.<org.ericace.grpcserver.RestitutionCoefficient, org.ericace.grpcserver.ResultCode>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SetRestitutionCoefficient"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.ericace.grpcserver.RestitutionCoefficient.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.ericace.grpcserver.ResultCode.getDefaultInstance()))
              .setSchemaDescriptor(new NBodyServiceMethodDescriptorSupplier("SetRestitutionCoefficient"))
              .build();
        }
      }
    }
    return getSetRestitutionCoefficientMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.ericace.grpcserver.ItemCount,
      org.ericace.grpcserver.ResultCode> getRemoveBodiesMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "RemoveBodies",
      requestType = org.ericace.grpcserver.ItemCount.class,
      responseType = org.ericace.grpcserver.ResultCode.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.ericace.grpcserver.ItemCount,
      org.ericace.grpcserver.ResultCode> getRemoveBodiesMethod() {
    io.grpc.MethodDescriptor<org.ericace.grpcserver.ItemCount, org.ericace.grpcserver.ResultCode> getRemoveBodiesMethod;
    if ((getRemoveBodiesMethod = NBodyServiceGrpc.getRemoveBodiesMethod) == null) {
      synchronized (NBodyServiceGrpc.class) {
        if ((getRemoveBodiesMethod = NBodyServiceGrpc.getRemoveBodiesMethod) == null) {
          NBodyServiceGrpc.getRemoveBodiesMethod = getRemoveBodiesMethod =
              io.grpc.MethodDescriptor.<org.ericace.grpcserver.ItemCount, org.ericace.grpcserver.ResultCode>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "RemoveBodies"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.ericace.grpcserver.ItemCount.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.ericace.grpcserver.ResultCode.getDefaultInstance()))
              .setSchemaDescriptor(new NBodyServiceMethodDescriptorSupplier("RemoveBodies"))
              .build();
        }
      }
    }
    return getRemoveBodiesMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.ericace.grpcserver.BodyDescription,
      org.ericace.grpcserver.ResultCode> getAddBodyMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "AddBody",
      requestType = org.ericace.grpcserver.BodyDescription.class,
      responseType = org.ericace.grpcserver.ResultCode.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.ericace.grpcserver.BodyDescription,
      org.ericace.grpcserver.ResultCode> getAddBodyMethod() {
    io.grpc.MethodDescriptor<org.ericace.grpcserver.BodyDescription, org.ericace.grpcserver.ResultCode> getAddBodyMethod;
    if ((getAddBodyMethod = NBodyServiceGrpc.getAddBodyMethod) == null) {
      synchronized (NBodyServiceGrpc.class) {
        if ((getAddBodyMethod = NBodyServiceGrpc.getAddBodyMethod) == null) {
          NBodyServiceGrpc.getAddBodyMethod = getAddBodyMethod =
              io.grpc.MethodDescriptor.<org.ericace.grpcserver.BodyDescription, org.ericace.grpcserver.ResultCode>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "AddBody"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.ericace.grpcserver.BodyDescription.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.ericace.grpcserver.ResultCode.getDefaultInstance()))
              .setSchemaDescriptor(new NBodyServiceMethodDescriptorSupplier("AddBody"))
              .build();
        }
      }
    }
    return getAddBodyMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.ericace.grpcserver.ModBodyMessage,
      org.ericace.grpcserver.ResultCode> getModBodyMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ModBody",
      requestType = org.ericace.grpcserver.ModBodyMessage.class,
      responseType = org.ericace.grpcserver.ResultCode.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.ericace.grpcserver.ModBodyMessage,
      org.ericace.grpcserver.ResultCode> getModBodyMethod() {
    io.grpc.MethodDescriptor<org.ericace.grpcserver.ModBodyMessage, org.ericace.grpcserver.ResultCode> getModBodyMethod;
    if ((getModBodyMethod = NBodyServiceGrpc.getModBodyMethod) == null) {
      synchronized (NBodyServiceGrpc.class) {
        if ((getModBodyMethod = NBodyServiceGrpc.getModBodyMethod) == null) {
          NBodyServiceGrpc.getModBodyMethod = getModBodyMethod =
              io.grpc.MethodDescriptor.<org.ericace.grpcserver.ModBodyMessage, org.ericace.grpcserver.ResultCode>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ModBody"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.ericace.grpcserver.ModBodyMessage.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.ericace.grpcserver.ResultCode.getDefaultInstance()))
              .setSchemaDescriptor(new NBodyServiceMethodDescriptorSupplier("ModBody"))
              .build();
        }
      }
    }
    return getModBodyMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.google.protobuf.Empty,
      org.ericace.grpcserver.CurrentConfig> getGetCurrentConfigMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetCurrentConfig",
      requestType = com.google.protobuf.Empty.class,
      responseType = org.ericace.grpcserver.CurrentConfig.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.google.protobuf.Empty,
      org.ericace.grpcserver.CurrentConfig> getGetCurrentConfigMethod() {
    io.grpc.MethodDescriptor<com.google.protobuf.Empty, org.ericace.grpcserver.CurrentConfig> getGetCurrentConfigMethod;
    if ((getGetCurrentConfigMethod = NBodyServiceGrpc.getGetCurrentConfigMethod) == null) {
      synchronized (NBodyServiceGrpc.class) {
        if ((getGetCurrentConfigMethod = NBodyServiceGrpc.getGetCurrentConfigMethod) == null) {
          NBodyServiceGrpc.getGetCurrentConfigMethod = getGetCurrentConfigMethod =
              io.grpc.MethodDescriptor.<com.google.protobuf.Empty, org.ericace.grpcserver.CurrentConfig>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetCurrentConfig"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.ericace.grpcserver.CurrentConfig.getDefaultInstance()))
              .setSchemaDescriptor(new NBodyServiceMethodDescriptorSupplier("GetCurrentConfig"))
              .build();
        }
      }
    }
    return getGetCurrentConfigMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static NBodyServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<NBodyServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<NBodyServiceStub>() {
        @java.lang.Override
        public NBodyServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new NBodyServiceStub(channel, callOptions);
        }
      };
    return NBodyServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static NBodyServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<NBodyServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<NBodyServiceBlockingStub>() {
        @java.lang.Override
        public NBodyServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new NBodyServiceBlockingStub(channel, callOptions);
        }
      };
    return NBodyServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static NBodyServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<NBodyServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<NBodyServiceFutureStub>() {
        @java.lang.Override
        public NBodyServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new NBodyServiceFutureStub(channel, callOptions);
        }
      };
    return NBodyServiceFutureStub.newStub(factory, channel);
  }

  /**
   * <pre>
   * Defines a gRPC Service that enables entities external to the running JVM to view / modify
   * simulation configurables, thus changing the behavior of the simulation on the fly
   * </pre>
   */
  public static abstract class NBodyServiceImplBase implements io.grpc.BindableService {

    /**
     * <pre>
     * Sets the number of threads allocated to computing the body positions
     * (The render engine threading model is not modifiable at this time)
     * </pre>
     */
    public void setComputationThreads(org.ericace.grpcserver.ItemCount request,
        io.grpc.stub.StreamObserver<org.ericace.grpcserver.ResultCode> responseObserver) {
      asyncUnimplementedUnaryCall(getSetComputationThreadsMethod(), responseObserver);
    }

    /**
     * <pre>
     * Sets the number of compute-ahead results allowed, in cases where the computation
     * thread outruns the render thread
     * </pre>
     */
    public void setResultQueueSize(org.ericace.grpcserver.ItemCount request,
        io.grpc.stub.StreamObserver<org.ericace.grpcserver.ResultCode> responseObserver) {
      asyncUnimplementedUnaryCall(getSetResultQueueSizeMethod(), responseObserver);
    }

    /**
     * <pre>
     * Changes the smoothing factor. When the body force and position computation runs
     * during each compute cycle, the force and resulting motion of the bodies is
     * smoothed by a factor which can be changed using this RPC method. The result is
     * that the apparent motion of the simulation is faster or slower
     * </pre>
     */
    public void setSmoothing(org.ericace.grpcserver.Factor request,
        io.grpc.stub.StreamObserver<org.ericace.grpcserver.ResultCode> responseObserver) {
      asyncUnimplementedUnaryCall(getSetSmoothingMethod(), responseObserver);
    }

    /**
     * <pre>
     * Sets the coefficient of restitution for elastic collisions
     * </pre>
     */
    public void setRestitutionCoefficient(org.ericace.grpcserver.RestitutionCoefficient request,
        io.grpc.stub.StreamObserver<org.ericace.grpcserver.ResultCode> responseObserver) {
      asyncUnimplementedUnaryCall(getSetRestitutionCoefficientMethod(), responseObserver);
    }

    /**
     * <pre>
     * Removes the specified number of bodies from the sim
     * </pre>
     */
    public void removeBodies(org.ericace.grpcserver.ItemCount request,
        io.grpc.stub.StreamObserver<org.ericace.grpcserver.ResultCode> responseObserver) {
      asyncUnimplementedUnaryCall(getRemoveBodiesMethod(), responseObserver);
    }

    /**
     * <pre>
     * Adds a body into the simulation
     * </pre>
     */
    public void addBody(org.ericace.grpcserver.BodyDescription request,
        io.grpc.stub.StreamObserver<org.ericace.grpcserver.ResultCode> responseObserver) {
      asyncUnimplementedUnaryCall(getAddBodyMethod(), responseObserver);
    }

    /**
     * <pre>
     * Modifies body properties
     * </pre>
     */
    public void modBody(org.ericace.grpcserver.ModBodyMessage request,
        io.grpc.stub.StreamObserver<org.ericace.grpcserver.ResultCode> responseObserver) {
      asyncUnimplementedUnaryCall(getModBodyMethod(), responseObserver);
    }

    /**
     * <pre>
     * Gets the current values of sim configurables
     * </pre>
     */
    public void getCurrentConfig(com.google.protobuf.Empty request,
        io.grpc.stub.StreamObserver<org.ericace.grpcserver.CurrentConfig> responseObserver) {
      asyncUnimplementedUnaryCall(getGetCurrentConfigMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getSetComputationThreadsMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.ericace.grpcserver.ItemCount,
                org.ericace.grpcserver.ResultCode>(
                  this, METHODID_SET_COMPUTATION_THREADS)))
          .addMethod(
            getSetResultQueueSizeMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.ericace.grpcserver.ItemCount,
                org.ericace.grpcserver.ResultCode>(
                  this, METHODID_SET_RESULT_QUEUE_SIZE)))
          .addMethod(
            getSetSmoothingMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.ericace.grpcserver.Factor,
                org.ericace.grpcserver.ResultCode>(
                  this, METHODID_SET_SMOOTHING)))
          .addMethod(
            getSetRestitutionCoefficientMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.ericace.grpcserver.RestitutionCoefficient,
                org.ericace.grpcserver.ResultCode>(
                  this, METHODID_SET_RESTITUTION_COEFFICIENT)))
          .addMethod(
            getRemoveBodiesMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.ericace.grpcserver.ItemCount,
                org.ericace.grpcserver.ResultCode>(
                  this, METHODID_REMOVE_BODIES)))
          .addMethod(
            getAddBodyMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.ericace.grpcserver.BodyDescription,
                org.ericace.grpcserver.ResultCode>(
                  this, METHODID_ADD_BODY)))
          .addMethod(
            getModBodyMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.ericace.grpcserver.ModBodyMessage,
                org.ericace.grpcserver.ResultCode>(
                  this, METHODID_MOD_BODY)))
          .addMethod(
            getGetCurrentConfigMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                com.google.protobuf.Empty,
                org.ericace.grpcserver.CurrentConfig>(
                  this, METHODID_GET_CURRENT_CONFIG)))
          .build();
    }
  }

  /**
   * <pre>
   * Defines a gRPC Service that enables entities external to the running JVM to view / modify
   * simulation configurables, thus changing the behavior of the simulation on the fly
   * </pre>
   */
  public static final class NBodyServiceStub extends io.grpc.stub.AbstractAsyncStub<NBodyServiceStub> {
    private NBodyServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected NBodyServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new NBodyServiceStub(channel, callOptions);
    }

    /**
     * <pre>
     * Sets the number of threads allocated to computing the body positions
     * (The render engine threading model is not modifiable at this time)
     * </pre>
     */
    public void setComputationThreads(org.ericace.grpcserver.ItemCount request,
        io.grpc.stub.StreamObserver<org.ericace.grpcserver.ResultCode> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getSetComputationThreadsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Sets the number of compute-ahead results allowed, in cases where the computation
     * thread outruns the render thread
     * </pre>
     */
    public void setResultQueueSize(org.ericace.grpcserver.ItemCount request,
        io.grpc.stub.StreamObserver<org.ericace.grpcserver.ResultCode> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getSetResultQueueSizeMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Changes the smoothing factor. When the body force and position computation runs
     * during each compute cycle, the force and resulting motion of the bodies is
     * smoothed by a factor which can be changed using this RPC method. The result is
     * that the apparent motion of the simulation is faster or slower
     * </pre>
     */
    public void setSmoothing(org.ericace.grpcserver.Factor request,
        io.grpc.stub.StreamObserver<org.ericace.grpcserver.ResultCode> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getSetSmoothingMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Sets the coefficient of restitution for elastic collisions
     * </pre>
     */
    public void setRestitutionCoefficient(org.ericace.grpcserver.RestitutionCoefficient request,
        io.grpc.stub.StreamObserver<org.ericace.grpcserver.ResultCode> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getSetRestitutionCoefficientMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Removes the specified number of bodies from the sim
     * </pre>
     */
    public void removeBodies(org.ericace.grpcserver.ItemCount request,
        io.grpc.stub.StreamObserver<org.ericace.grpcserver.ResultCode> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getRemoveBodiesMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Adds a body into the simulation
     * </pre>
     */
    public void addBody(org.ericace.grpcserver.BodyDescription request,
        io.grpc.stub.StreamObserver<org.ericace.grpcserver.ResultCode> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getAddBodyMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Modifies body properties
     * </pre>
     */
    public void modBody(org.ericace.grpcserver.ModBodyMessage request,
        io.grpc.stub.StreamObserver<org.ericace.grpcserver.ResultCode> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getModBodyMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Gets the current values of sim configurables
     * </pre>
     */
    public void getCurrentConfig(com.google.protobuf.Empty request,
        io.grpc.stub.StreamObserver<org.ericace.grpcserver.CurrentConfig> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetCurrentConfigMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * <pre>
   * Defines a gRPC Service that enables entities external to the running JVM to view / modify
   * simulation configurables, thus changing the behavior of the simulation on the fly
   * </pre>
   */
  public static final class NBodyServiceBlockingStub extends io.grpc.stub.AbstractBlockingStub<NBodyServiceBlockingStub> {
    private NBodyServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected NBodyServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new NBodyServiceBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * Sets the number of threads allocated to computing the body positions
     * (The render engine threading model is not modifiable at this time)
     * </pre>
     */
    public org.ericace.grpcserver.ResultCode setComputationThreads(org.ericace.grpcserver.ItemCount request) {
      return blockingUnaryCall(
          getChannel(), getSetComputationThreadsMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Sets the number of compute-ahead results allowed, in cases where the computation
     * thread outruns the render thread
     * </pre>
     */
    public org.ericace.grpcserver.ResultCode setResultQueueSize(org.ericace.grpcserver.ItemCount request) {
      return blockingUnaryCall(
          getChannel(), getSetResultQueueSizeMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Changes the smoothing factor. When the body force and position computation runs
     * during each compute cycle, the force and resulting motion of the bodies is
     * smoothed by a factor which can be changed using this RPC method. The result is
     * that the apparent motion of the simulation is faster or slower
     * </pre>
     */
    public org.ericace.grpcserver.ResultCode setSmoothing(org.ericace.grpcserver.Factor request) {
      return blockingUnaryCall(
          getChannel(), getSetSmoothingMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Sets the coefficient of restitution for elastic collisions
     * </pre>
     */
    public org.ericace.grpcserver.ResultCode setRestitutionCoefficient(org.ericace.grpcserver.RestitutionCoefficient request) {
      return blockingUnaryCall(
          getChannel(), getSetRestitutionCoefficientMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Removes the specified number of bodies from the sim
     * </pre>
     */
    public org.ericace.grpcserver.ResultCode removeBodies(org.ericace.grpcserver.ItemCount request) {
      return blockingUnaryCall(
          getChannel(), getRemoveBodiesMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Adds a body into the simulation
     * </pre>
     */
    public org.ericace.grpcserver.ResultCode addBody(org.ericace.grpcserver.BodyDescription request) {
      return blockingUnaryCall(
          getChannel(), getAddBodyMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Modifies body properties
     * </pre>
     */
    public org.ericace.grpcserver.ResultCode modBody(org.ericace.grpcserver.ModBodyMessage request) {
      return blockingUnaryCall(
          getChannel(), getModBodyMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Gets the current values of sim configurables
     * </pre>
     */
    public org.ericace.grpcserver.CurrentConfig getCurrentConfig(com.google.protobuf.Empty request) {
      return blockingUnaryCall(
          getChannel(), getGetCurrentConfigMethod(), getCallOptions(), request);
    }
  }

  /**
   * <pre>
   * Defines a gRPC Service that enables entities external to the running JVM to view / modify
   * simulation configurables, thus changing the behavior of the simulation on the fly
   * </pre>
   */
  public static final class NBodyServiceFutureStub extends io.grpc.stub.AbstractFutureStub<NBodyServiceFutureStub> {
    private NBodyServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected NBodyServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new NBodyServiceFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * Sets the number of threads allocated to computing the body positions
     * (The render engine threading model is not modifiable at this time)
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<org.ericace.grpcserver.ResultCode> setComputationThreads(
        org.ericace.grpcserver.ItemCount request) {
      return futureUnaryCall(
          getChannel().newCall(getSetComputationThreadsMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Sets the number of compute-ahead results allowed, in cases where the computation
     * thread outruns the render thread
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<org.ericace.grpcserver.ResultCode> setResultQueueSize(
        org.ericace.grpcserver.ItemCount request) {
      return futureUnaryCall(
          getChannel().newCall(getSetResultQueueSizeMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Changes the smoothing factor. When the body force and position computation runs
     * during each compute cycle, the force and resulting motion of the bodies is
     * smoothed by a factor which can be changed using this RPC method. The result is
     * that the apparent motion of the simulation is faster or slower
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<org.ericace.grpcserver.ResultCode> setSmoothing(
        org.ericace.grpcserver.Factor request) {
      return futureUnaryCall(
          getChannel().newCall(getSetSmoothingMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Sets the coefficient of restitution for elastic collisions
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<org.ericace.grpcserver.ResultCode> setRestitutionCoefficient(
        org.ericace.grpcserver.RestitutionCoefficient request) {
      return futureUnaryCall(
          getChannel().newCall(getSetRestitutionCoefficientMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Removes the specified number of bodies from the sim
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<org.ericace.grpcserver.ResultCode> removeBodies(
        org.ericace.grpcserver.ItemCount request) {
      return futureUnaryCall(
          getChannel().newCall(getRemoveBodiesMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Adds a body into the simulation
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<org.ericace.grpcserver.ResultCode> addBody(
        org.ericace.grpcserver.BodyDescription request) {
      return futureUnaryCall(
          getChannel().newCall(getAddBodyMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Modifies body properties
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<org.ericace.grpcserver.ResultCode> modBody(
        org.ericace.grpcserver.ModBodyMessage request) {
      return futureUnaryCall(
          getChannel().newCall(getModBodyMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Gets the current values of sim configurables
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<org.ericace.grpcserver.CurrentConfig> getCurrentConfig(
        com.google.protobuf.Empty request) {
      return futureUnaryCall(
          getChannel().newCall(getGetCurrentConfigMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_SET_COMPUTATION_THREADS = 0;
  private static final int METHODID_SET_RESULT_QUEUE_SIZE = 1;
  private static final int METHODID_SET_SMOOTHING = 2;
  private static final int METHODID_SET_RESTITUTION_COEFFICIENT = 3;
  private static final int METHODID_REMOVE_BODIES = 4;
  private static final int METHODID_ADD_BODY = 5;
  private static final int METHODID_MOD_BODY = 6;
  private static final int METHODID_GET_CURRENT_CONFIG = 7;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final NBodyServiceImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(NBodyServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_SET_COMPUTATION_THREADS:
          serviceImpl.setComputationThreads((org.ericace.grpcserver.ItemCount) request,
              (io.grpc.stub.StreamObserver<org.ericace.grpcserver.ResultCode>) responseObserver);
          break;
        case METHODID_SET_RESULT_QUEUE_SIZE:
          serviceImpl.setResultQueueSize((org.ericace.grpcserver.ItemCount) request,
              (io.grpc.stub.StreamObserver<org.ericace.grpcserver.ResultCode>) responseObserver);
          break;
        case METHODID_SET_SMOOTHING:
          serviceImpl.setSmoothing((org.ericace.grpcserver.Factor) request,
              (io.grpc.stub.StreamObserver<org.ericace.grpcserver.ResultCode>) responseObserver);
          break;
        case METHODID_SET_RESTITUTION_COEFFICIENT:
          serviceImpl.setRestitutionCoefficient((org.ericace.grpcserver.RestitutionCoefficient) request,
              (io.grpc.stub.StreamObserver<org.ericace.grpcserver.ResultCode>) responseObserver);
          break;
        case METHODID_REMOVE_BODIES:
          serviceImpl.removeBodies((org.ericace.grpcserver.ItemCount) request,
              (io.grpc.stub.StreamObserver<org.ericace.grpcserver.ResultCode>) responseObserver);
          break;
        case METHODID_ADD_BODY:
          serviceImpl.addBody((org.ericace.grpcserver.BodyDescription) request,
              (io.grpc.stub.StreamObserver<org.ericace.grpcserver.ResultCode>) responseObserver);
          break;
        case METHODID_MOD_BODY:
          serviceImpl.modBody((org.ericace.grpcserver.ModBodyMessage) request,
              (io.grpc.stub.StreamObserver<org.ericace.grpcserver.ResultCode>) responseObserver);
          break;
        case METHODID_GET_CURRENT_CONFIG:
          serviceImpl.getCurrentConfig((com.google.protobuf.Empty) request,
              (io.grpc.stub.StreamObserver<org.ericace.grpcserver.CurrentConfig>) responseObserver);
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

  private static abstract class NBodyServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    NBodyServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return org.ericace.grpcserver.Nbodyservice.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("NBodyService");
    }
  }

  private static final class NBodyServiceFileDescriptorSupplier
      extends NBodyServiceBaseDescriptorSupplier {
    NBodyServiceFileDescriptorSupplier() {}
  }

  private static final class NBodyServiceMethodDescriptorSupplier
      extends NBodyServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    NBodyServiceMethodDescriptorSupplier(String methodName) {
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
      synchronized (NBodyServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new NBodyServiceFileDescriptorSupplier())
              .addMethod(getSetComputationThreadsMethod())
              .addMethod(getSetResultQueueSizeMethod())
              .addMethod(getSetSmoothingMethod())
              .addMethod(getSetRestitutionCoefficientMethod())
              .addMethod(getRemoveBodiesMethod())
              .addMethod(getAddBodyMethod())
              .addMethod(getModBodyMethod())
              .addMethod(getGetCurrentConfigMethod())
              .build();
        }
      }
    }
    return result;
  }
}
