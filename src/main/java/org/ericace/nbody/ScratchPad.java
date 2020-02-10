package org.ericace.nbody;

public class ScratchPad {
    public static void main(String [] args) {
        Body b2 = new Body(Body.nextID(),-10,0,0, 50000,0,0, 9E5, 10F, Body.CollisionBehavior.FRAGMENT, null, 1);
        double mass = 9E5;
        double vx = -50000;
        for (int i = 1; i < 20; ++i) {
            Body b1 = new Body(Body.nextID(), 10,0,0,vx,0,0, mass, 10F, Body.CollisionBehavior.FRAGMENT, null, 1);
            Body.CollisionCalcResult r = b1.calcElasticCollision(b2);
            Body.FragmentationCalcResult fr = b1.shouldFragment(b2, r);
            System.out.println(String.format("thisMass=%f thisFactor=%f otherMass=%f otherFactor=%f", mass,
                    fr.thisFactor, 9E5, fr.otherFactor));
            mass *= 10;
            vx *= 10;
        }
    }
}
