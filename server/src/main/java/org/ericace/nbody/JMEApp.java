package org.ericace.nbody;

import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsAppState;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.PointLight;
import com.jme3.material.MatParam;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Sphere;
import com.jme3.system.AppSettings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ericace.globals.Globals;
import org.ericace.instrumentation.InstrumentationManager;
import org.ericace.instrumentation.Metric;

import java.util.HashMap;
import java.util.Map;

/**
 * Integrates with the JMonkeyEngine game engine to render the simulation
 */
public final class JMEApp extends SimpleApplication {
    private static final Logger logger = LogManager.getLogger(JMEApp.class);
    private static final Metric metricComputationCount = InstrumentationManager.getInstrumentation()
            .registerLabeledCounter("nbody_computation_count/thread", "renderer", "Simulation cycles");
    private static final Metric metricBodyCountGauge = InstrumentationManager.getInstrumentation()
            .registerLabeledGauge("nbody_body_count_gauge/thread", "renderer", "Number of rendered bodies");
    private static final Metric metricNoQueuesCount = InstrumentationManager.getInstrumentation()
            .registerCounter("nbody_no_queues_to_render_count",
                    "Count of rendering engine outrunning computation runner");

    /**
     * Camera/Keyboard functionality:
     * W       = Cam Forward
     * A       = Left
     * S       = Back
     * D       = Right
     * Q       = Up
     * Z       = Down
     * Mouse   = Look
     * keypad+ = increase cam speed
     * keypad- = decrease cam speed
     * F12     = unbind/bind keyboard from/to the sim window
     * ESC     = exit simulation
     */
    private static final int CAM_SPEED = 200;

    /**
     * When incrementing or decrementing the cam speed via the keypad plus/minus, use
     * this increment
     */
    private static final int CAM_SPEED_STEP = 100;

    /**
     * Defines the far side of the frustrum
     */
    private static final float FRUSTRUM_FAR = 400000F;

    /**
     * The key mappings used by the class
     */
    private static final String F12MappingName = "F12";

    /**
     * Supports increasing cam speed
     */
    private static final String increaseCamSpeedMappingName = "KEYPAD+";

    /**
     * Supports decreasing cam speed
     */
    private static final String decreaseCamSpeedMappingName = "KEYPAD-";

    /**
     * True if the fly cam is currently attached, else false
     */
    private boolean flyCamAttached = true;

    /**
     * Holds the bodies in the simulation for JMonkey - the map key is the ID of the body
     */
    private Map<Integer, Geometry> geos;

    /**
     * Holds the light sources (suns) in the simulation - the map key is the ID of the body
     */
    private Map<Integer, PointLight> lightSources;

    /**
     * Provides lists of bodies to render each cycle as their positions are re-computed in a separate
     * thread
     */
    private final ResultQueueHolder resultQueueHolder;

    /**
     * The initial camera location
     */
    private final SimpleVector initialCam;

    /**
     * Initializes the instance
     *
     * @param bodySize          Initial number of bodies in the simulation - initializes the internal Map that
     *                          the class uses to manage the bodies in the scene graph
     * @param resultQueueHolder provides the updated list of bodies to render each cycle. See {@link #resultQueueHolder}
     * @param initialCam        Initial cam position. See {@link #initialCam}
     * @param resolution        Screen resolution {x,y}
     */
    private JMEApp(int bodySize, ResultQueueHolder resultQueueHolder, SimpleVector initialCam, int [] resolution) {
        super();

        AppSettings settings = new AppSettings(true);
        settings.setUseInput(true);
        settings.setResolution(resolution[0], resolution[1]);
        settings.setFullscreen(false);
        settings.setResizable(false);
        settings.setTitle("N-Body Java Simulation");
        setSettings(settings);
        setShowSettings(false);
        setPauseOnLostFocus(false);

        this.resultQueueHolder = resultQueueHolder;
        this.initialCam = initialCam;
        geos = new HashMap<>(bodySize);
        lightSources = new HashMap<>();
    }

    /**
     * Starts the JME app (which in turn starts a thread). Refer to constructor - {@link #JMEApp} - for
     * param explanation. Note - inclusion of jme3-lwjgl3 caused the threading behavior to change. Calling
     * 'start' on the superclass used to start a thread, but after lwjgl3 it waited - hence the explicit
     * Thread creation.
     */
    public static void start(int bodySize, ResultQueueHolder resultQueueHolder, SimpleVector initialCam,
                             int [] resolution, String threadName) {
        new Thread(() -> {
            new JMEApp(bodySize, resultQueueHolder, initialCam, resolution).start();
        }, threadName).start();
    }

    /**
     * Performs JMonkeyEngine initialization: Initializes fly camera, frustrum, and creates an input
     * key mapping that supports detaching / and re-attaching the mouse and keyboard to the
     * simulation window.
     */
    @Override
    public void simpleInitApp() {

        // set initial cam position and look at the sun - which is at 0,0,0
        cam.setLocation(new Vector3f(initialCam.x, initialCam.y, initialCam.z));
        cam.lookAt(new Vector3f(0,0,0), new Vector3f(0,1,0));

        // a little faster fly speed
        flyCam.setMoveSpeed(CAM_SPEED);

        // set a larger depth because the sim takes up a fair bit of space
        cam.setFrustumFar(FRUSTRUM_FAR);

        // connect the F12 key to handle engaging/disengaging the fly cam
        getInputManager().addMapping(F12MappingName, new KeyTrigger(KeyInput.KEY_F12));
        getInputManager().addListener(f12Listener, F12MappingName);

        // connect keypad +- to vary the cam speed
        getInputManager().addMapping(increaseCamSpeedMappingName, new KeyTrigger(KeyInput.KEY_ADD));
        getInputManager().addMapping(decreaseCamSpeedMappingName, new KeyTrigger(KeyInput.KEY_SUBTRACT));
        getInputManager().addListener(camSpeed, increaseCamSpeedMappingName, decreaseCamSpeedMappingName);

        // turn off debug stats initially
        stateManager.getState(StatsAppState.class).toggleStats();
    }

    /**
     * Adds the passed BodyRenderInfo to the JME scene graph, and also to the local
     * {@code Map} of Geometry instances. Each Geometry instance holds the JME-specific info
     * corresponding to a {@code Body} in the simulation.
     *
     * @param b the instance to add
     */
    private Geometry addBody(BodyRenderInfo b) {
        Material mat;
        Sphere sphere;
        if (b.isSun) {
            sphere = new Sphere(40, 50, b.radius);
            mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            PointLight pl = new PointLight();
            pl.setPosition(new Vector3f(b.x, b.y, b.z));
            pl.setColor(ColorRGBA.White);
            pl.setRadius(0f);
            rootNode.addLight(pl);
            lightSources.put(b.id, pl);
        } else {
            sphere = new Sphere(20, 20, b.radius);
            mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
            mat.setFloat("Shininess", 25);
            mat.setBoolean("UseMaterialColors", true);
            mat.setColor("Ambient", ColorRGBA.Black);
            mat.setColor("Diffuse", xlatColor(b.color));
            mat.setColor("Specular", ColorRGBA.Yellow);
        }
        Geometry geo = new Geometry(String.valueOf(b.id), sphere);
        geo.setLocalScale(1f);
        geo.setLocalTranslation(b.x, b.y, b.z);
        geo.setMaterial(mat);
        rootNode.attachChild(geo);
        geos.put(b.id, geo);
        return geo;
    }

    /**
     * Translate {@link Body} color to JME {@link ColorRGBA}
     *
     * @param color the {@code Body} color
     *
     * @return the JMonkeyEngine color
     */
    private static ColorRGBA xlatColor(Globals.Color color) {
        if (color == null) {
            return ColorRGBA.randomColor();
        }
        switch (color) {
            case BLACK: return ColorRGBA.Black;
            case WHITE: return ColorRGBA.White;
            case DARKGRAY: return ColorRGBA.DarkGray;
            case GRAY: return ColorRGBA.Gray;
            case LIGHTGRAY: return ColorRGBA.LightGray;
            case RED: return ColorRGBA.Red;
            case GREEN: return ColorRGBA.Green;
            case BLUE: return ColorRGBA.Blue;
            case YELLOW: return ColorRGBA.Yellow;
            case MAGENTA: return ColorRGBA.Magenta;
            case CYAN: return ColorRGBA.Cyan;
            case ORANGE: return ColorRGBA.Orange;
            case BROWN: return ColorRGBA.Brown;
            case PINK: return ColorRGBA.Pink;
            case RANDOM: default: return ColorRGBA.randomColor();
        }
    }

    /**
     *  Handles the F12 key. (The JME window has to have focus to receive the keycode from the OS.)
     *  <p>
     *  If the fly cam is currently connected, then the F12 key disconnects the fly cam from the JME window so
     *  the user can tab into other windows while the sim is running.</p>
     *  <p>
     *  If the fly cam is currently <i>not</i> connected, then the F12 key re-connects the fly cam so
     *  the user can again use it to navigate within the sim (and press ESC to end the sim)</p>
     */
    private ActionListener f12Listener = (name, isPressed, tpf) -> {
        if (!(name.equals(F12MappingName) && isPressed)) {
            return;
        }
        if (flyCamAttached) {
            flyCam.unregisterInput();
        } else {
            flyCam.registerWithInput(getInputManager());
        }
        flyCamAttached = !flyCamAttached;
    };

    /**
     * Increases of decreases the cam speed by increments of {@link #CAM_SPEED_STEP}
     */
    private ActionListener camSpeed = (name, isPressed, tpf) -> {
        if (name.equals(increaseCamSpeedMappingName) && isPressed) {
            flyCam.setMoveSpeed(flyCam.getMoveSpeed() + CAM_SPEED_STEP);
        } else if (name.equals(decreaseCamSpeedMappingName) && isPressed) {
            float curSpeed = flyCam.getMoveSpeed();
            if (curSpeed > 0) {
                curSpeed = curSpeed < CAM_SPEED_STEP ? 0 : curSpeed - CAM_SPEED_STEP;
                flyCam.setMoveSpeed(curSpeed);
            }
        }
        logger.debug("New cam speed: {}", flyCam.getMoveSpeed());
    };

    /**
     * Updates the positions of all the bodies in the scene graph. This class subclasses the {@link SimpleApplication}
     * class. Therefore, when this class's {@code start} method is invoked, it delegates to the base class method
     * of the same name. The result is the creation of a thread which, in turn, invokes this method according to the
     * base class's frame rate.
     *
     * <p>This method uses the instance field {@link #resultQueueHolder} to get a queue of bodies whose positions
     * and other attributes have been computed by another thread. Assuming such a queue is available, it walks the
     * queue and renders the bodies according to their updated positions and attributes.</p>
     *
     * <p>In some cases, a body is designated as no longer existing. In this case, the method removes the
     * body from the scene graph.</p>
     *
     * @param tpf unused
     */
    @Override
public void simpleUpdate(float tpf) {

        ResultQueueHolder.ResultQueue rq = resultQueueHolder.nextComputedQueue();
        if (rq == null) {
            metricNoQueuesCount.incValue();
            return;
        }
        int countDetached = 0;

        for (BodyRenderInfo b : rq.getQueue()) {
            if (!b.exists) {
                if (geos.containsKey(b.id)) {
                    rootNode.detachChild(geos.get(b.id)); // remove from the scene graph
                    geos.remove(b.id);
                    ++countDetached;
                }
            } else {
                Geometry g = geos.get(b.id);
                if (g == null) {
                    g = addBody(b);
                }
                Sphere s = (Sphere) g.getMesh();
                // allow a body's radius to change
                if (s.radius != b.radius) {
                    s.updateGeometry(s.getZSamples(), s.getRadialSamples(), b.radius);
                }
                // allow a body's color to change
                MatParam mp = g.getMaterial().getParam("Diffuse");
                if (b.color != null && b.color != Globals.Color.RANDOM && mp != null) {
                    ColorRGBA c = (ColorRGBA) mp.getValue();
                    if (! c.equals(xlatColor(b.color))) {
                        g.getMaterial().setColor("Diffuse", xlatColor(b.color));
                    }
                }
                // update this body's position and if the body has a light source, also update that
                g.setLocalTranslation(b.x, b.y, b.z);
                PointLight pl = lightSources.get(b.id);
                if (pl != null) {
                    pl.setPosition(new Vector3f(b.x, b.y, b.z));
                }
            }
        }
        if (countDetached > 0) {
            logger.info("Detached {} bodies from the root node", countDetached);
        }
        metricComputationCount.incValue();
        metricBodyCountGauge.setValue(rootNode.getChildren().size());
    }
}