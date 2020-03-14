package org.ericace.nbody;

import org.ericace.globals.Globals;

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

    Globals.CollisionBehavior getCollision() {
        switch (value.toUpperCase()) {
            case "SUBSUME": return Globals.CollisionBehavior.SUBSUME;
            case "ELASTIC": return Globals.CollisionBehavior.ELASTIC;
            case "FRAGMENT": return Globals.CollisionBehavior.FRAGMENT;
            case "NONE": default: return Globals.CollisionBehavior.NONE;
        }
    }

    Globals.Color getColor() {
        switch (value.toUpperCase()) {
            case "BLACK": return Globals.Color.BLACK;
            case "WHITE": return Globals.Color.WHITE;
            case "DARKGRAY": return Globals.Color.DARKGRAY;
            case "GRAY": return Globals.Color.GRAY;
            case "LIGHTGRAY": return Globals.Color.LIGHTGRAY;
            case "RED": return Globals.Color.RED;
            case "GREEN": return Globals.Color.GREEN;
            case "BLUE": return Globals.Color.BLUE;
            case "YELLOW": return Globals.Color.YELLOW;
            case "MAGENTA": return Globals.Color.MAGENTA;
            case "CYAN": return Globals.Color.CYAN;
            case "ORANGE": return Globals.Color.ORANGE;
            case "BROWN": return Globals.Color.BROWN;
            case "PINK": return Globals.Color.PINK;
            case "RANDOM": default: return Globals.Color.RANDOM;
        }
    }
}
