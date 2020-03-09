package org.ericace.nbody;

import java.util.List;

/**
 * Defines all the supported ways to inspect - and modify - the behavior of the simulation while the
 * simulation is running. This interface is also expressed in the gRPC service definition. (Refer to:
 * {@code src/main/resources/proto/nbodyservice.proto} for the service definition.)
 */
public interface Configurables {
    /**
     * Defines what happens when two bodies in the simulation collide
     */
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

    /**
     * Sets the result queue size, which enables the computation thread to outrun the render thread
     *
     * @param queueSize the size to set
     */
    void setResultQueueSize(int queueSize);

    /**
     * @return the current result queue size
     */
    int getResultQueueSize();

    /**
     * Sets a smoothing factor. All force and velocity calculations are scaled by this value
     *
     * @param smoothing larger is faster, smaller is slower
     */
    void setSmoothing(float smoothing);

    /**
     * @return the current smoothing factoer
     */
    float getSmoothing();

    /**
     * Sets the number of threads dedicated to computing force/position of all bodies in the simulation
     *
     * @param threads the number of threads
     */
    void setComputationThreads(int threads);

    /**
     * @return the current number of threads
     */
    int getComputationThreads();

    /**
     * Sets the collision behavior
     *
     * @param behavior the behavior to set
     */
    void setCollisionBehavior(CollisionBehavior behavior);

    /**
     * @return the current collision behavior
     */
    CollisionBehavior getCollisionBehavior();

    /**
     * Sets the coefficient of restitution for elastic collisions
     *
     * @param R the value to set
     */
    void setRestitutionCoefficient(float R);

    /**
     * @return the current coefficient of restition
     */
    float getRestitutionCoefficient();

    /**
     * Removes bodies from the simulation. The interface does not attempt to specify how bodies are selected
     * for removal
     *
     * @param countToRemove the number of bodies to remove
     */
    void removeBodies(int countToRemove);

    /**
     * @return the current number of bodies in the simulation
     */
    int getBodyCount();

    /**
     * Adds a body to the simulation. Params are not documented, as they appear to be relatively self-explanatory
     *
     * @return the ID of the body added
     */
    int addBody(float mass, float x, float y, float z, float vx, float vy, float vz, float radius,
                boolean isSun, Body.CollisionBehavior behavior, Body.Color bodyColor, float fragFactor,
                float fragStep, boolean withTelemetry, String name, String clas, boolean pinned);

    enum ModBodyResult {
        NO_MATCH("No matching bodies"),
        MOD_ALL("All matching bodies were modified"),
        MOD_SOME("Some matching bodies were modified"),
        MOD_NONE("No matching bodies were modified");
        private String result;
        private ModBodyResult(String result) {
            this.result = result;
        }
        public String getResult() {return result;}
    }
    /**
     * Modifies body properties
     */
    ModBodyResult modBody(int id, String name, String clas, List<BodyMod> bodyMods);
}
