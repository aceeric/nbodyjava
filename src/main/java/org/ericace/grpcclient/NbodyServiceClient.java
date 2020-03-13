package org.ericace.grpcclient;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.ericace.grpcserver.*;
import org.ericace.nbody.Body;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.ericace.globals.Globals.parseCollisionBehavior;
import static org.ericace.globals.Globals.parseColor;

/**
 * Provides a Java client to the gRPC server
 */
public class NbodyServiceClient {
    private final NBodyServiceGrpc.NBodyServiceBlockingStub blockingStub;
    private final ManagedChannel channel;
    private static final int PORT_NUM = 50051;

    public NbodyServiceClient(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port).usePlaintext().build());
    }
    public NbodyServiceClient(ManagedChannel channel) {
        this.channel = channel;
        blockingStub = NBodyServiceGrpc.newBlockingStub(channel);
    }

    /**
     * set-threads
     */
    private void setComputationThreads(String [] args) {
        int threads = Integer.parseInt(args[0]);
        ItemCount request = ItemCount.newBuilder().setItemCount(threads).build();
        ResultCode resultCode = blockingStub.setComputationThreads(request);
        System.out.println(resultCode.getResultCode() + " " + resultCode.getMessage());

    }

    /**
     * set-queue-size
     */
    private void setResultQueueSize(String [] args) {
        int threads = Integer.parseInt(args[0]);
        ItemCount request = ItemCount.newBuilder().setItemCount(threads).build();
        ResultCode resultCode = blockingStub.setResultQueueSize(request);
        System.out.println(resultCode.getResultCode() + " " + resultCode.getMessage());
    }

    /**
     * set-time-scale
     */
    private void setSmoothing(String [] args) {
        float f = Float.parseFloat(args[0]);
        Factor request = Factor.newBuilder().setFactor(f).build();
        ResultCode resultCode = blockingStub.setSmoothing(request);
        System.out.println(resultCode.getResultCode() + " " + resultCode.getMessage());
    }

    /**
     * set-restitution
     */
    private void setRestitutionCoefficient(String [] args) {
        float f = Float.parseFloat(args[0]);
        RestitutionCoefficient request = RestitutionCoefficient.newBuilder().setRestitutionCoefficient(f).build();
        ResultCode resultCode = blockingStub.setRestitutionCoefficient(request);
        System.out.println(resultCode.getResultCode() + " " + resultCode.getMessage());
    }

    /**
     * remove-bodies
     */
    private void removeBodies(String [] args) {
        int threads = Integer.parseInt(args[0]);
        ItemCount request = ItemCount.newBuilder().setItemCount(threads).build();
        ResultCode resultCode = blockingStub.removeBodies(request);
        System.out.println(resultCode.getResultCode() + " " + resultCode.getMessage());
    }

    /**
     * mod-bodies [id= or name= or class=] [attribute=value] ...
     *
     * E.g.:
     *
     * mod-bodies id=412 mass=9E5 color=BLUE vx=12E6
     */
    private void modBodies(String [] args) {
        String [] cmd = args[1].split("=");
        int id=-1; // funkiness for gRPC
        String name = "", clas = "";
        if ("id".equals(cmd[0])) {
            id = Integer.parseInt(cmd[1]);
        } else if ("name".equals(cmd[0])) {
            name = cmd[1];
        } else if ("class".equals(cmd[0])) {
            clas = cmd[1];
        } else {
            throw new RuntimeException("Unknown cmd for mod-bodies: " + args[1]);
        }
        ModBodyMessage.Builder builder = ModBodyMessage.newBuilder().setId(id).setName(name).setClass_(clas);
        final List<String> validMods = Arrays.asList("x","y","z","vx","vy","vz","mass","radius","sun","collision",
                "color","frag-factor","frag-step","telemetry");
        for (int i = 2; i < args.length; ++i) {
            String [] p = args[i].split("=");
            if (!validMods.contains(p[0])) {
                throw new RuntimeException("Invalid mod: " + args[i]);
            }
            builder.addP(args[i]);
        }
        ModBodyMessage request = builder.build();
        ResultCode resultCode = blockingStub.modBody(request);
        System.out.println(resultCode.getResultCode() + " " + resultCode.getMessage());
    }

    /**
     * Future
     * run-cmds stdin
     * @param args
     */
    private void runCmds(String [] args) {} // read from stdin or file and exec the commands in there

    /**
     * get-config - no args
     */
    private void getCurrentConfig() {
        CurrentConfig config = blockingStub.getCurrentConfig(null);
        String result =
            "Bodies = %d\n" +
            "Result Queue Size = %d\n" +
            "Computation Threads = %d\n" +
            "Smoothing Factor = %e\n" +
            "Restitution Coefficient = %e\n";
        result = String.format(result, config.getBodies(), config.getResultQueueSize(), config.getComputationThreads(),
                config.getSmoothingFactor(), config.getRestitutionCoefficient());
        System.out.println(result);
    }

    /**
     * Adds one body with typed args
     */
    private void addBodies(float x, float y, float z, float vx, float vy, float vz, float mass, float radius,
                           boolean sun, Body.CollisionBehavior collisionBehavior, Body.Color color, float fragFactor,
                           float fragStep, boolean telemetry, String name, String clas, boolean pinned) {
        BodyDescription request = BodyDescription.newBuilder()
                .setX(x).setY(y).setZ(z).setVx(vx).setVy(vy).setVz(vz).setMass(mass).setRadius(radius)
                .setIsSun(sun).setCollisionBehavior(xlatCollisionBehavior(collisionBehavior))
                .setBodyColor(xlatColor(color))
                .setFragFactor(fragFactor).setFragStep(fragStep)
                .setWithTelemetry(telemetry).setPinned(pinned)
                .setName(name == null ? "" : name).setClass_(clas == null ? "" : clas)
                .build();
        ResultCode resultCode = blockingStub.addBody(request);
        System.out.println(resultCode.getResultCode() + " " + resultCode.getMessage());
    }

    /**
     * add-body (or add-bodies) x y z vx vy vz mass radius
     *
     * x thru radius are required positionally. After that, in any order:
     *
     * Booleans - their presence means true, absence means false:
     *   pinned       - don't delete when remove-bodies is invoked unless remove-bodies -1
     *   telemetry    - emanate detailed info about this body to stdout while the sim is running
     *   is-sun       - this body is a sun - it will be white, and have a light source that illuminates the sim
     *
     * These require values in the form name=value:
     *
     *   collision=   - see {@link Body.CollisionBehavior}; e.g. collision=elastic
     *   color=       - see  {@link Body.Color}; e.g. collision=blue
     *   frag-factor= - fragmentation factor; e.g. frag-factor=.1
     *   frag-step=   - fragmentation step; e.g. frag-step=1000
     *   class=       - A class useful for mod-bodies; e.g. class=asteroid
     *   name=        - A name useful for mod-bodies; e.g. name=the-moon
     *
     * These are used only if "add-bodies":
     *
     *   qty=         - A number of bodies to add; e.g. qty=100
     *   delay=       - A delay between the insertion of each body into the sim
     *   posrand=     - A randomizer to randomize x,y,z position; e.g. posrand=10
     *   vrand=       - " for velocity; e.g. vrand=1000
     *   massrand=    - " for mass; e.g. massrand=1E3
     *   rrand=       - " for radius; e.g. rrand=5
     */
    private void addBodies(String [] args) {
        float x = Float.parseFloat(args[1]);
        float y = Float.parseFloat(args[2]);
        float z = Float.parseFloat(args[3]);
        float vx = Float.parseFloat(args[4]);
        float vy = Float.parseFloat(args[5]);
        float vz = Float.parseFloat(args[6]);
        float mass = Float.parseFloat(args[7]);
        float radius = Float.parseFloat(args[8]);
        boolean sun = false, telemetry = false, pinned = false;
        Body.CollisionBehavior collisionBehavior = Body.CollisionBehavior.ELASTIC;
        Body.Color color = Body.Color.RANDOM;
        float fragFactor = 0, fragStep = 0;
        String name = "", clas = "";

        // meaningful only for add bodies
        int qty = 1;
        float delay = 0, positionRandom = 0, velocityRandom = 0, massRandom = 0, radiusRandom = 0;

        for (int i = 9; i < args.length; ++i) {
            String [] nv = args[i].split("=");
            switch (nv[0].toLowerCase()) {
                case "is-sun": sun = true; break;
                case "collision": collisionBehavior = parseCollisionBehavior(nv[1]); break;
                case "color": color = parseColor(nv[1]); break;
                case "frag-factor": fragFactor = Float.parseFloat(nv[1]); break;
                case "frag-step":  fragStep = Float.parseFloat(nv[1]); break;
                case "telemetry": telemetry = true; break;
                case "name": name = nv[1]; break;
                case "class": clas = nv[1]; break;
                case "pinned": pinned = true; break;
                case "qty": qty = Integer.parseInt(nv[1]); break;
                case "delay": delay = Float.parseFloat(nv[1]); break;
                case "posrand": positionRandom = Float.parseFloat(nv[1]); break;
                case "vrand": velocityRandom = Float.parseFloat(nv[1]); break;
                case "massrand": massRandom = Float.parseFloat(nv[1]); break;
                case "rrand": radiusRandom = Float.parseFloat(nv[1]); break;

                default:
                    throw new RuntimeException("Unknown param: " + args[i]);
            }
        }
        if (args[0].equalsIgnoreCase("add-body")) {
            addBodies(x, y, z, vx, vy, vz, mass, radius, sun, collisionBehavior, color, fragFactor, fragStep, telemetry,
                    name, clas, pinned);
        } else {
            addBodies(x, y, z, vx, vy, vz, mass, radius, sun, collisionBehavior, color, fragFactor, fragStep, telemetry,
                    name, clas, pinned, qty, delay, positionRandom, velocityRandom, massRandom, radiusRandom);
        }
    }

    /**
     * Adds a number of bodies
     * @param qty            The number of bodies
     * @param delay          Seconds to delay between the addition of each body
     * @param positionRandom Randomization of the passed position. If "5", then passed x,y,z can vary by +5.
     * @param velocityRandom " velocity
     * @param massRandom     " mass
     * @param radiusRandom   " radius
     */
    private void addBodies(float x, float y, float z, float vx, float vy, float vz, float mass, float radius,
                           boolean sun, Body.CollisionBehavior collisionBehavior, Body.Color color, float fragFactor,
                           float fragStep, boolean telemetry, String name, String clas, boolean pinned,
                           int qty, float delay, float positionRandom, float velocityRandom, float massRandom,
                           float radiusRandom
                           ) {
        for (int i = 0; i < qty; ++i) {
            float wx = positionRandom == 0 ? x : (float) (x + (Math.random() * positionRandom));
            float wy = positionRandom == 0 ? y : (float) (y + (Math.random() * positionRandom));
            float wz = positionRandom == 0 ? z : (float) (z + (Math.random() * positionRandom));
            float wvx = velocityRandom == 0 ? vx : (float) (vx + (Math.random() * velocityRandom));
            float wvy = velocityRandom == 0 ? vy : (float) (vy + (Math.random() * velocityRandom));
            float wvz = velocityRandom == 0 ? vz : (float) (vz + (Math.random() * velocityRandom));
            float wmass = massRandom == 0 ? mass : (float) (mass + (Math.random() * massRandom));
            float wradius = radiusRandom == 0 ? radius : (float) (radius + (Math.random() * massRandom));
            addBodies(wx, wy, wz, wvx, wvy, wvz, wmass, wradius, sun, collisionBehavior, color, fragFactor, fragStep, telemetry,
                    name, clas, pinned);
            try {Thread.sleep((long) (1000 * delay));} catch (InterruptedException e) {/* ignore */}
        }
    }

    /**
     * Main function. Based on the command, calls the handler function
     *
     * @param args from the cmd line
     *
     * @throws InterruptedException if gRPC channel is interrupted awaiting termination
     */
    public static void main(String[] args) throws InterruptedException {
        NbodyServiceClient client = new NbodyServiceClient("localhost", PORT_NUM);
        try {
            switch (args[0].toLowerCase()) {
                case "set-threads": client.setComputationThreads(args); ; break;
                case "set-queue-size": client.setResultQueueSize(args); ; break;
                case "set-time-scale": client.setSmoothing(args); ; break;
                case "set-restitution": client.setRestitutionCoefficient(args); ; break;
                case "remove-bodies": client.removeBodies(args); ; break;
                case "mod-body": case "mod-bodies": client.modBodies(args); ; break;
                case "get-config": client.getCurrentConfig(); break;
                case "add-body": case "add-bodies":
                    client.addBodies(args); break;
                default:
                    System.out.println("Unsupported cmd: args[0]");
                    break;
            }
        } finally {
            client.channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    /**
     * Translates sim body color to gRPC body color since the two are defined by mutually exclusive
     * mechanisms
     *
     * @param color The body color
     *
     * @return the gRPC interface body color
     */
    private org.ericace.grpcserver.BodyColorEnum xlatColor(Body.Color color) {
        switch (color) {
            case BLACK: return BodyColorEnum.BLACK;
            case WHITE:  return BodyColorEnum.WHITE;
            case DARKGRAY:  return BodyColorEnum.DARKGRAY;
            case GRAY:  return BodyColorEnum.GRAY;
            case LIGHTGRAY: return BodyColorEnum.LIGHTGRAY;
            case RED:  return BodyColorEnum.RED;
            case GREEN:  return BodyColorEnum.GREEN;
            case BLUE:  return BodyColorEnum.BLUE;
            case YELLOW: return BodyColorEnum.YELLOW;
            case MAGENTA: return BodyColorEnum.MAGENTA;
            case CYAN: return BodyColorEnum.CYAN;
            case ORANGE: return BodyColorEnum.ORANGE;
            case BROWN: return BodyColorEnum.BROWN;
            case PINK: return BodyColorEnum.PINK;
            default:
            case RANDOM: return BodyColorEnum.RANDOM;
        }
    }

    /**
     * Translates sim collision behavior to gRPC collision behavior  since the two are defined by mutually exclusive
     * mechanisms
     *
     * @param behaviorEnum The body collision behavior
     *
     * @return the gRPC interface collision behavior
     */
    private org.ericace.grpcserver.CollisionBehaviorEnum xlatCollisionBehavior(Body.CollisionBehavior behaviorEnum) {
        switch (behaviorEnum) {
            case NONE:
                return org.ericace.grpcserver.CollisionBehaviorEnum.NONE;
            case SUBSUME:
                return org.ericace.grpcserver.CollisionBehaviorEnum.SUBSUME;
            case FRAGMENT:
                return org.ericace.grpcserver.CollisionBehaviorEnum.FRAGMENT;
            case ELASTIC:
            default:
                return org.ericace.grpcserver.CollisionBehaviorEnum.ELASTIC;
        }
    }
}
