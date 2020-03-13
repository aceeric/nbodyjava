package org.ericace.nbody;

/**
 * Helper that holds values associated with the elastic collision calculation
 */
class CollisionCalcResult {
    final boolean collided;
    final double vx1;
    final double vy1;
    final double vz1;
    final double vx2;
    final double vy2;
    final double vz2;
    final double vx_cm;
    final double vy_cm;
    final double vz_cm;
    private CollisionCalcResult() {
        collided = false;
        vx1 = vy1 = vz1 = vx2 = vy2 = vz2 = vx_cm = vy_cm = vz_cm = 0;
    }
    private CollisionCalcResult(double vx1, double vy1, double vz1, double vx2, double vy2, double vz2,
                                double vx_cm, double vy_cm, double vz_cm) {
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
    static CollisionCalcResult collision(double vx1, double vy1, double vz1, double vx2, double vy2, double vz2,
                                         double vx_cm, double vy_cm, double vz_cm) {
        return new CollisionCalcResult(vx1, vy1, vz1, vx2, vy2, vz2, vx_cm, vy_cm, vz_cm);
    }
}
