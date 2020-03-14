package org.ericace.nbody;

public class Scratch {
    public static void main(String [] args) {
        final long cnt = 10_000_000_000L;
        FloatBody fb1 = new FloatBody(1.2F,2.3F,3.4F,4.5F,5.6F,6.7F,7.8F,9E12F);
        FloatBody fb2 = new FloatBody(10.5F,20.6F,30.3F,40.3F,50.9F,60.2F,70.5F,9E30F);
        long start = System.currentTimeMillis();
        for (long i = 0; i < cnt; ++i) {
            fb1.calcForceFrom(fb2, .01F);
        }
        long elapsed = System.currentTimeMillis() - start;
        System.out.println("float: count=" + cnt + " elapsed=" + elapsed + "ms");
        // float: count=10000000000 elapsed=215,117ms
        System.out.println(fb1);

        DoubleBody db1 = new DoubleBody(1.2D,2.3D,3.4D,4.5D,5.6D,6.7D,7.8D,9E12D);
        DoubleBody db2 = new DoubleBody(10.5D,20.6D,30.3D,40.3D,50.9D,60.2D,70.5D,9E30D);
        start = System.currentTimeMillis();
        for (long i = 0; i < cnt; ++i) {
            db1.calcForceFrom(db2, .01F);
        }
        elapsed = System.currentTimeMillis() - start;
        System.out.println("double: count=" + cnt + " elapsed=" + elapsed + "ms");
        // double: count=10000000000 elapsed=255,773ms
        System.out.println(db1);
    }

    static class FloatBody {
        private static final float G = 6.673e-11F;
        float x, y, z, vx, vy, vz, radius, mass, fx, fy, fz;
        FloatBody(float x, float y, float z, float vx, float vy, float vz, float radius, float mass) {
            this.x = x; this.y = y; this.z = z; this.vx = vx; this.vy = vy; this.vz = vz; this.radius = radius; this.mass = mass;
        }
        @Override
        public String toString() {
            return String.format("x:%f y:%f z:%f vx:%f vy:%f vz:%f fx:%f fy:%f fz:%f m:%f r:%f", x, y, z, vx, vy, vz,
                    fx, fy, fz, mass, radius);
        }
        private void calcForceFrom(FloatBody otherBody, float timeScaling) {
            float dx = otherBody.x - x;
            float dy = otherBody.y - y;
            float dz = otherBody.z - z;
            float dist = (float) Math.sqrt(dx*dx + dy*dy + dz*dz);
            if (dist > (radius + otherBody.radius)) {
                float force = (G * mass * otherBody.mass) / (dist * dist);
                fx += force * dx / dist;
                fy += force * dy / dist;
                fz += force * dz / dist;
            }
            vx += timeScaling * fx / mass;
            vy += timeScaling * fy / mass;
            vz += timeScaling * fz / mass;
            x += timeScaling * vx;
            y += timeScaling * vy;
            z += timeScaling * vz;
        }
    }
    static class DoubleBody {
        private static final double G = 6.673e-11D;
        double x, y, z, vx, vy, vz, radius, mass, fx, fy, fz;
        DoubleBody(double x, double y, double z, double vx, double vy, double vz, double radius, double mass) {
            this.x = x; this.y = y; this.z = z; this.vx = vx; this.vy = vy; this.vz = vz; this.radius = radius; this.mass = mass;
        }
        @Override
        public String toString() {
            return String.format("x:%f y:%f z:%f vx:%f vy:%f vz:%f fx:%f fy:%f fz:%f m:%f r:%f", x, y, z, vx, vy, vz,
                    fx, fy, fz, mass, radius);
        }
        private void calcForceFrom(DoubleBody otherBody, double timeScaling) {
            double dx = otherBody.x - x;
            double dy = otherBody.y - y;
            double dz = otherBody.z - z;
            double dist = Math.sqrt(dx*dx + dy*dy + dz*dz);
            if (dist > (radius + otherBody.radius)) {
                double force = (G * mass * otherBody.mass) / (dist * dist);
                fx += force * dx / dist;
                fy += force * dy / dist;
                fz += force * dz / dist;
            }
            vx += timeScaling * fx / mass;
            vy += timeScaling * fy / mass;
            vz += timeScaling * fz / mass;
            x += timeScaling * vx;
            y += timeScaling * vy;
            z += timeScaling * vz;
        }
    }
}
