package org.ericace.nbody;

import java.util.Arrays;
import java.util.List;

/**
 * A utility class that enables properties of a body to be altered via the gRPC interface while the simulation
 * is running.
 */
public class BodyMod {
    /**
     * Body properties that are allowed to be modified
     */
    enum Mod {
        NOP,X,Y,Z,VX,VY,VZ,MASS,RADIUS,SUN,COLLISION,COLOR,FRAG_FACTOR,FRAG_STEP,TELEMETRY
    }

    /**
     * Defines the property mods that require a float argument
     */
    private static final List<Mod> floats = Arrays.asList(Mod.Y, Mod.X, Mod.Z, Mod.VX, Mod.VY, Mod.VZ, Mod.MASS,
            Mod.RADIUS, Mod.FRAG_FACTOR, Mod.FRAG_STEP);

    private final Mod mod;
    private final String value;

    private BodyMod(Mod mod, String value) {
        this.mod = mod;
        this.value = value;
    }

    public Mod getMod() {
        return mod;
    }

    /**
     * Called by the gRPC server to create an instance from a body mod param received via gRPC. This is useful
     * for things like changing the mass of an object to illustrate the effect that has on the simulation,
     * changing the collision behavior, etc.
     *
     * @param modStr a modification request nave/value pair separated by equals sign. E.g.:
     *              "radius=12", or "collision=elastic", etc.
     *
     * @return a BodyMod representing the requested change or null if anything about the {@code modStr}
     * param is invalid
     */
    public static BodyMod makeMod(String modStr) {
        String [] nvp = modStr.split("=");
        if (nvp.length != 2) {
            return null;
        }
        try {
            Mod m = Mod.valueOf(nvp[0].toUpperCase());
            if (floats.contains(m)) {
                // make sure the value can be parsed - if not - throws and method returns null
                Float.parseFloat(nvp[1]);
            }
            return new BodyMod(m, nvp[1]);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    float getFloat() {
        return Float.parseFloat(value);
    }

    boolean getBoolean() {
        return Boolean.parseBoolean(value);
    }

    Body.CollisionBehavior getCollision() {
        switch (value.toUpperCase()) {
            case "SUBSUME": return Body.CollisionBehavior.SUBSUME;
            case "ELASTIC": return Body.CollisionBehavior.ELASTIC;
            case "FRAGMENT": return Body.CollisionBehavior.FRAGMENT;
            case "NONE": default: return Body.CollisionBehavior.NONE;
        }
    }

    Body.Color getColor() {
        switch (value.toUpperCase()) {
            case "BLACK": return Body.Color.BLACK;
            case "WHITE": return Body.Color.WHITE;
            case "DARKGRAY": return Body.Color.DARKGRAY;
            case "GRAY": return Body.Color.GRAY;
            case "LIGHTGRAY": return Body.Color.LIGHTGRAY;
            case "RED": return Body.Color.RED;
            case "GREEN": return Body.Color.GREEN;
            case "BLUE": return Body.Color.BLUE;
            case "YELLOW": return Body.Color.YELLOW;
            case "MAGENTA": return Body.Color.MAGENTA;
            case "CYAN": return Body.Color.CYAN;
            case "ORANGE": return Body.Color.ORANGE;
            case "BROWN": return Body.Color.BROWN;
            case "PINK": return Body.Color.PINK;
            case "RANDOM": default: return Body.Color.RANDOM;
        }
    }
}
