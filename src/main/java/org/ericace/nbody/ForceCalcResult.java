package org.ericace.nbody;

/**
 * Helper that holds values associated with calculating gravitational force between two bodies.
 */
class ForceCalcResult {
    /**
     * Distance between bodies
     */
    final float dist;

    /**
     * True if force calculation determined that the bodies are colliding (radii overlap), else False
     */
    final boolean collided;

    private ForceCalcResult(float dist, boolean collided) {
        this.dist = dist;
        this.collided = collided;
    }
    static ForceCalcResult noCollision() {
        return new ForceCalcResult(0, false);
    }
    static ForceCalcResult collision(float dist) {
        return new ForceCalcResult(dist, true);
    }
}
