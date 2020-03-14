package org.ericace.globals;

import org.apache.logging.log4j.Level;

import java.util.Arrays;
import java.util.List;

public final class Globals {
    /**
     * A custom log4j level
     */
    public static final Level CUSTOM = Level.getLevel("CUSTOM");

    /**
     * defines String values to parse as True
     */
    private static final List<String> TRUES = Arrays.asList("t", "true", "1", "y", "yes");

    /**
     * Defines the supported collision responses. SUBSUME means that two bodies merge into one
     * upon collision: the larger radius body subsumes the smaller. ELASTIC_COLLISION means that the
     * bodies bounce off each other. FRAGMENT means a body breaks into smaller bodies upon collision
     * NONE means no collisions - bodies pass through each other
     */
    public enum CollisionBehavior {
        NONE, SUBSUME, ELASTIC, FRAGMENT
    }

    /**
     * Defines supported colors
     */
    public enum Color {
        RANDOM, BLACK, WHITE, DARKGRAY, GRAY, LIGHTGRAY, RED, GREEN, BLUE, YELLOW, MAGENTA, CYAN, ORANGE, BROWN, PINK
    }

    /**
     * @return the passed string as a {@link CollisionBehavior} enum. Valid values are "elastic", "none",
     * "fragment", and "subsume" (in any case) as defined by the referenced enum. Null parses as
     * Body.CollisionBehavior.ELASTIC.
     */
    public static CollisionBehavior parseCollisionBehavior(String s) {
        return s != null ? CollisionBehavior.valueOf(s.toUpperCase()) : CollisionBehavior.ELASTIC;
    }

    /**
     * @return the passed string parsed as a boolean, as defined by the {@link #TRUES} constant. Null
     * parses as False.
     */
    public static boolean parseBoolean(String s) {
        return s != null && TRUES.contains(s.toLowerCase());
    }

    /**
     * @return the passed string as a {@link Color} enum. Null parses as Body.Color.RANDOM
     */
    public static Color parseColor(String s) {
        return s != null ? Color.valueOf(s.toUpperCase()) : Color.RANDOM;
    }
}
