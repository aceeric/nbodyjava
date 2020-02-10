package org.ericace.grpcserver;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;
import org.ericace.nbody.Body;
import org.ericace.nbody.Configurables;

import java.io.IOException;

import static org.ericace.nbody.Body.Color.*;

/**
 * Provides a gRPC server singleton on port 50051. Handles requests from external entities
 * and delegates them to the {@link Configurables} field.
 */
public class NBodyServiceServer {
    private final Server server;
    private final Configurables configurables;
    private static final int PORT_NUM = 50051;
    private static NBodyServiceServer instance;

    /**
     * Creates an instance with a ref to the passed {@link Configurables}. The configurables instance
     * provides info about simulation configuration, and allows the gRPC server to modify sim configs
     *
     * @param configurables gets and sets simulation configuration params
     */
    private NBodyServiceServer(Configurables configurables) {
        this.configurables = configurables;
        server = ServerBuilder.forPort(PORT_NUM)
                .addService(new NBodyServiceServer.NBodyService())
                .addService(ProtoReflectionService.newInstance())
                .build();
    }

    /**
     * Calls the private constructor to instantiate the singleton, and then starts it
     *
     * @param configurables gets and sets simulation configuration params
     *
     * @throws IOException thrown by gRPC
     * @throws IllegalStateException if called twice
     */
    public static void start(Configurables configurables) throws IOException {
        if (instance != null) {
            throw new IllegalStateException("The start method can only be called once");
        }
        instance = new NBodyServiceServer(configurables);
        instance.start();
    }

    /**
     * Starts the gRPC server, and adds a JVM shutdown hook to stop the server thread when the JVM shuts down.
     * This code is cloned from example code from GitHub: https://github.com/grpc/grpc-java
     *
     * @throws IOException per the grpc client libraries
     */
    private void start() throws IOException {
        server.start();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                NBodyServiceServer.stop();
                System.err.println("*** server shut down");
            }
        });
    }

    /**
     * Stops the gRPC server
     */
    public static void stop() {
        if (instance != null && instance.server != null) {
            instance.server.shutdown();
        }
    }

    /**
     * Waits for the gRPC server to stop
     */
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    /**
     * Supports testing
     *
     * @param args per Java
     *
     * @throws IOException          per the grpc client libs
     * @throws InterruptedException per the grpc client libs
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        final NBodyServiceServer server = new NBodyServiceServer(new dummyConfigurables());
        server.start();
        server.blockUntilShutdown();
    }

    /**
     * Provides the gRPC server implementation. The implementation is a facade for the instance
     * {@link NBodyServiceServer#configurables} field.
     */
    class NBodyService extends NBodyServiceGrpc.NBodyServiceImplBase {

        @Override
        public void setComputationThreads(org.ericace.grpcserver.ItemCount request,
                                          io.grpc.stub.StreamObserver<org.ericace.grpcserver.ResultCode> responseObserver) {
            configurables.setComputationThreads((int) request.getItemCount());
            ResultCode resultCode = ResultCode.newBuilder().setResultCode(ResultCode.ResultCodeEnum.OK).build();
            responseObserver.onNext(resultCode);
            responseObserver.onCompleted();
        }

        @Override
        public void setResultQueueSize(org.ericace.grpcserver.ItemCount request,
                                       io.grpc.stub.StreamObserver<org.ericace.grpcserver.ResultCode> responseObserver) {
            configurables.setResultQueueSize((int) request.getItemCount());
            ResultCode resultCode = ResultCode.newBuilder().setResultCode(ResultCode.ResultCodeEnum.OK).build();
            responseObserver.onNext(resultCode);
            responseObserver.onCompleted();
        }

        @Override
        public void setSmoothing(org.ericace.grpcserver.Factor request,
                                 io.grpc.stub.StreamObserver<org.ericace.grpcserver.ResultCode> responseObserver) {
            configurables.setSmoothing(request.getFactor());
            ResultCode resultCode = ResultCode.newBuilder().setResultCode(ResultCode.ResultCodeEnum.OK).build();
            responseObserver.onNext(resultCode);
            responseObserver.onCompleted();
        }

        @Override
        public void setRestitutionCoefficient(org.ericace.grpcserver.RestitutionCoefficient request,
                                 io.grpc.stub.StreamObserver<org.ericace.grpcserver.ResultCode> responseObserver) {
            configurables.setRestitutionCoefficient(request.getRestitutionCoefficient());
            ResultCode resultCode = ResultCode.newBuilder().setResultCode(ResultCode.ResultCodeEnum.OK).build();
            responseObserver.onNext(resultCode);
            responseObserver.onCompleted();
        }

        @Override
        public void getCurrentConfig(com.google.protobuf.Empty request,
                                     io.grpc.stub.StreamObserver<org.ericace.grpcserver.CurrentConfig> responseObserver) {
            CurrentConfig currentConfig = CurrentConfig.newBuilder()
                    .setBodies(configurables.getBodyCount())
                    .setComputationThreads(configurables.getComputationThreads())
                    .setResultQueueSize(configurables.getResultQueueSize())
                    .setSmoothingFactor(configurables.getSmoothing())
                    .setCollisionBehaviorValue(configurables.getCollisionBehavior().value())
                    .setRestitutionCoefficient(configurables.getRestitutionCoefficient())
                    .build();
            responseObserver.onNext(currentConfig);
            responseObserver.onCompleted();
        }

        @Override
        public void removeBodies(org.ericace.grpcserver.ItemCount request,
                                 io.grpc.stub.StreamObserver<org.ericace.grpcserver.ResultCode> responseObserver) {
            configurables.removeBodies((int) request.getItemCount());
            ResultCode resultCode = ResultCode.newBuilder().setResultCode(ResultCode.ResultCodeEnum.OK).build();
            responseObserver.onNext(resultCode);
            responseObserver.onCompleted();
        }

        @Override
        public void addBody(org.ericace.grpcserver.BodyDescription request,
                            io.grpc.stub.StreamObserver<org.ericace.grpcserver.ResultCode> responseObserver) {
            double mass = request.getMass();
            double x = request.getX();
            double y = request.getY();
            double z = request.getZ();
            double vx = request.getVx();
            double vy = request.getVy();
            double vz = request.getVz();
            double radius = request.getRadius();
            boolean isSun = request.getIsSun();
            double fragFactor = request.getFragFactor();
            Body.CollisionBehavior behavior = xlatCollisionBehavior(request.getCollisionBehavior());
            Body.Color bodyColor = xlatColor(request.getBodyColor());

            configurables.addBody(mass, x, y, z, vx, vy, vz, radius, isSun, behavior, bodyColor, fragFactor);
            ResultCode resultCode = ResultCode.newBuilder().setResultCode(ResultCode.ResultCodeEnum.OK).build();
            responseObserver.onNext(resultCode);
            responseObserver.onCompleted();
        }

        private Body.Color xlatColor(org.ericace.grpcserver.BodyColorEnum bodyColorEnum) {
            switch (bodyColorEnum.getNumber()) {
                case 1: return RANDOM;
                case 2: return BLACK;
                case 3: return WHITE;
                case 4: return DARKGRAY;
                case 5: return GRAY;
                case 6: return LIGHTGRAY;
                case 7: return RED;
                case 8: return GREEN;
                case 9: return BLUE;
                case 10: return YELLOW;
                case 11: return MAGENTA;
                case 12: return CYAN;
                case 13: return ORANGE;
                case 14: return BROWN;
                case 15: return PINK;
                case 0: default: return null;
            }
        }

        private Body.CollisionBehavior xlatCollisionBehavior(
                org.ericace.grpcserver.CollisionBehaviorEnum behaviorEnum) {
            switch (behaviorEnum) {
                case NONE:
                    return Body.CollisionBehavior.NONE;
                case SUBSUME:
                    return Body.CollisionBehavior.SUBSUME;
                case FRAGMENT:
                    return Body.CollisionBehavior.FRAGMENT;
                case ELASTIC:
                default:
                    return Body.CollisionBehavior.ELASTIC;
            }
        }
    }

    /**
     * Supports limited testing of the gRPC server
     */
    private static class dummyConfigurables implements Configurables {
        @Override
        public void setResultQueueSize(int queueSize)  {}

        @Override
        public int getResultQueueSize() {
            return 100;
        }

        @Override
        public void setSmoothing(double smoothing)  {}

        @Override
        public double getSmoothing() {
            return 200;
        }

        @Override
        public void setComputationThreads(int threads)  {}

        @Override
        public int getComputationThreads() {
            return 300;
        }

        @Override
        public void setCollisionBehavior(CollisionBehavior behavior)  {}

        @Override
        public CollisionBehavior getCollisionBehavior() {
            return CollisionBehavior.SUBSUME;
        }

        @Override
        public void setRestitutionCoefficient(double R)  {}

        @Override
        public double getRestitutionCoefficient() { return 1;}

        @Override
        public void removeBodies(int countToRemove)  {}

        @Override
        public int getBodyCount() {
            return 400;
        }

        @Override
        public void addBody(double mass, double x, double y, double z, double vx, double vy, double vz, double radius,
                            boolean isSun, Body.CollisionBehavior behavior, Body.Color bodyColor, double fragFactor)  {}
    }
}
