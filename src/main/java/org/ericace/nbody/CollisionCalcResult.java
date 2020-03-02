package org.ericace.nbody;

/**
 * Helper that holds values associated with the elastic collision calculation
 */
class CollisionCalcResult {
    final boolean collided;
    final float vx1;
    final float vy1;
    final float vz1;
    final float vx2;
    final float vy2;
    final float vz2;
    final float vx_cm;
    final float vy_cm;
    final float vz_cm;
    private CollisionCalcResult() {
        collided = false;
        vx1 = vy1 = vz1 = vx2 = vy2 = vz2 = vx_cm = vy_cm = vz_cm = 0;
    }
    private CollisionCalcResult(float vx1, float vy1, float vz1, float vx2, float vy2, float vz2,
                                float vx_cm, float vy_cm, float vz_cm) {
        collided = true;
        this.vx1   = vx1;
        this.vy1   = vy1;
        this.vz1   = vz1;
        this.vx2   = vx2;
        this.vy2   = vy2;
        this.vz2   = vz2;
        this.vx_cm = vx_cm;
        this.vy_cm = vy_cm;
        this.vz_cm = vz_cm;
    }
    static CollisionCalcResult noCollision() {
        return new CollisionCalcResult();
    }
    static CollisionCalcResult collision(float vx1, float vy1, float vz1, float vx2, float vy2, float vz2,
                                         float vx_cm, float vy_cm, float vz_cm) {
        return new CollisionCalcResult(vx1, vy1, vz1, vx2, vy2, vz2, vx_cm, vy_cm, vz_cm);
    }
}
