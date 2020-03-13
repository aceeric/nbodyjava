package org.ericace.globals;

import org.apache.logging.log4j.Level;
import org.ericace.nbody.Body;

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
     * @return the passed string as a {@link Body.CollisionBehavior} enum. Valid values are "elastic", "none",
     * "fragment", and "subsume" (in any case) as defined by the referenced enum. Null parses as
     * Body.CollisionBehavior.ELASTIC.
     */
    public static Body.CollisionBehavior parseCollisionBehavior(String s) {
        return s != null ? Body.CollisionBehavior.valueOf(s.toUpperCase()) : Body.CollisionBehavior.ELASTIC;
    }

    /**
     * @return the passed string parsed as a boolean, as defined by the {@link #TRUES} constant. Null
     * parses as False.
     */
    public static boolean parseBoolean(String s) {
        return s != null && TRUES.contains(s.toLowerCase());
    }

    /**
     * @return the passed string as a {@link Body.Color} enum. Null parses as Body.Color.RANDOM
     */
    public static Body.Color parseColor(String s) {
        return s != null ? Body.Color.valueOf(s.toUpperCase()) : Body.Color.RANDOM;
    }
}
