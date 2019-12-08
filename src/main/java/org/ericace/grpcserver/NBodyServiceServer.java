package org.ericace.grpcserver;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;

import java.io.IOException;

import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

public class NBodyServiceServer {
    private Server server;
    private Configurables configurables;

    public NBodyServiceServer(Configurables configurables) {
        this.configurables = configurables;
    }

    public void start() throws IOException {
        int port = 50051;
        server = ServerBuilder.forPort(port)
                .addService(new NBodyServiceServer.NBodyService())
                .addService(ProtoReflectionService.newInstance())
                .build()
                .start();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                NBodyServiceServer.this.stop();
                System.err.println("*** server shut down");
            }
        });
    }

    public void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    // for testing
    public static void main(String[] args) throws IOException, InterruptedException {
        final NBodyServiceServer server = new NBodyServiceServer(new dummyConfigurables());
        server.start();
        server.blockUntilShutdown();
    }

    class NBodyService extends NBodyServiceGrpc.NBodyServiceImplBase {

        @Override
        public void getCurrentConfig(com.google.protobuf.Empty request,
                                     io.grpc.stub.StreamObserver<org.ericace.grpcserver.CurrentConfig> responseObserver) {
            CurrentConfig currentConfig = CurrentConfig.newBuilder()
                    .setBodies(configurables.getBodyCount())
                    .setComputationThreads(configurables.getComputationThreads())
                    .setResultQueueSize(configurables.getResultQueueSize())
                    .setSmoothingFactor(configurables.getSmoothing())
                    .setCollisionBehaviorValue(configurables.getCollisionBehavior().value())
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
            configurables.addBody(mass, x, y, z, vx, vy, vz, radius);
            ResultCode resultCode = ResultCode.newBuilder().setResultCode(ResultCode.ResultCodeEnum.OK).build();
            responseObserver.onNext(resultCode);
            responseObserver.onCompleted();
        }
    }

    // A stub class to support limited testing of the gRPC server
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
        public void removeBodies(int bodyCount)  {}

        @Override
        public int getBodyCount() {
            return 400;
        }

        @Override
        public void addBody(double mass, double x, double y, double z, double vx, double vy, double vz,
                            double radius)  {}
    }
}
