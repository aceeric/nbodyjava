package org.ericace.nbody;

/**
 * Helper that holds values associated with calculating fragmentation during body collision
 */
class FragmentationCalcResult {
    final boolean shouldFragment;
    final float thisFactor;
    final float otherFactor;
    private FragmentationCalcResult(boolean shouldFragment, float thisFactor, float otherFactor) {
        this.shouldFragment = shouldFragment;
        this.thisFactor     = thisFactor;
        this.otherFactor    = otherFactor;
    }
    static FragmentationCalcResult noFragmentation() {
        return new FragmentationCalcResult(false, 0, 0);
    }
    static FragmentationCalcResult fragmentation(float thisFactor, float otherFactor) {
        return new FragmentationCalcResult(true, thisFactor, otherFactor);
    }
}
