package org.ericace.grpcserver;

public interface Configurables {
    enum CollisionBehavior {
        UNDEF(0),
        SUBSUME(1),
        BOUNCE(2),
        FRAG(3),
        NOOP(99);
        private final int value;
        CollisionBehavior(int value) {
            this.value = value;
        }
        public int value() {
            return this.value;
        }
    }
    void setResultQueueSize(int queueSize);
    int getResultQueueSize();
    void setSmoothing(double smoothing);
    double getSmoothing();
    void setComputationThreads(int threads);
    int getComputationThreads();
    void setCollisionBehavior(CollisionBehavior behavior);
    CollisionBehavior getCollisionBehavior();
    void removeBodies(int bodyCount);
    int getBodyCount();
    void addBody(double mass, double x, double y, double z, double vx, double vy, double vz, double radius);
}
