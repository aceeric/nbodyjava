package org.ericace.nbody;

/**
 * Holds info about a body that is fragmenting across computation cycles
 */
class FragInfo {
    /**
     * The radius within which to generate fragments
     */
    final float radius;

    /**
     * The radius of each fragment
     */
    final float newRadius;

    /**
     * The mass of each fragment
     */
    final float mass;

    /**
     * The number of fragments
     */
    int fragments;

    /**
     * The position within which to generate fragments
     */
    final SimpleVector curPos;

    FragInfo(float radius, float newRadius, float mass, int fragments, SimpleVector curPos) {
        this.radius = radius;
        this.newRadius = newRadius;
        this.mass = mass;
        this.fragments = fragments;
        this.curPos = curPos;
    }
}
