package org.ericace.nbody;

import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsAppState;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Sphere;
import com.jme3.system.AppSettings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ericace.instrumentation.InstrumentationManager;
import org.ericace.instrumentation.Metric;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Integrates with the JMonkeyEngine game engine
 */
public class JMEApp extends SimpleApplication {
    private static final Logger logger = LogManager.getLogger(JMEApp.class);
    private static final Metric metricRenderCount = InstrumentationManager.getInstrumentation()
            .registerCounter("nbody_render_count");
    private static final Metric metricBodyCountGauge = InstrumentationManager.getInstrumentation()
            .registerGauge("nbody_rendered_bodies_gauge");
    private static final Metric metricNoQueuesCount = InstrumentationManager.getInstrumentation()
            .registerCounter("nbody_no_queues_to_render_count");
    private static final Metric metricComputationMillisRendererSummary = InstrumentationManager.getInstrumentation()
            .registerSummary("nbody_computation_millis/processor", "renderer");
    private static final Metric metricComputationMillisJmeSummary = InstrumentationManager.getInstrumentation()
            .registerSummary("nbody_computation_millis/processor", "jme");

    /**
     * Camera speed:
     * W     = Forward
     * A     = Left
     * S     = Back
     * D     = Right
     * Q     = Up
     * Z     = Down
     * Mouse = Look
     * ESC   = exit simulation
     */
    private static final int CAM_SPEED = 200;

    /**
     * Defines the far side of the frustrum
     */
    private static final float FRUSTRUM_FAR = 400000F;

    /**
     * The key mappings used by the class
     */
    private static final String F12MappingName = "F12";

    /**
     * True if the fly cam is currently attached, else false
     */
    private boolean flyCamAttached = true;

    /**
     * Holds the bodies in the simulation - the map key is the ID of the body
     */
    private Map<Integer, Geometry> geos;

    /**
     * Provides lists of bodies to render each cycle as their positions are re-computed in a separate
     * thread
     */
    private final ResultQueueHolder resultQueueHolder;

    /**
     * Used to initialize the geos. JME didn't allow that to be done in the constructor so a ref
     * to the list is saved here, used in the {@link #simpleInitApp} method, and then discarded
     * there after the {@link #geos} are initialized
     */
    private ArrayList<BodyRenderInfo> bodies;

    /**
     * The initial camera location
     */
    private final Vector initialCam;

    /**
     * For performance benchmarking - records the last time the render method was invoked by
     * the engine on each render invocation
     */
    private long lastRenderTime;

    /**
     * Initializes the instance
     *
     * @param bodies            Used once to init the geos. See {@link #bodies}
     * @param resultQueueHolder provides the updated list of bodies to render each cycle. See {@link #resultQueueHolder}
     * @param initialCam        Initial cam position. See {@link #initialCam}
     */
    JMEApp(ArrayList<BodyRenderInfo> bodies, ResultQueueHolder resultQueueHolder, Vector initialCam) {
        super();

        AppSettings settings = new AppSettings(true);
        settings.setUseInput(true);
        settings.setResolution(2560, 1380);
        settings.setFrequency(60);
        settings.setBitsPerPixel(24);
        settings.setFullscreen(false);
        settings.setResizable(false);
        setSettings(settings);
        setShowSettings(false);
        setPauseOnLostFocus(false);

        this.resultQueueHolder = resultQueueHolder;
        this.bodies = bodies;
        this.initialCam = initialCam;
        geos = new HashMap<>(bodies.size());
    }

    /**
     * Performs JMonkeyEngine initialization: creates Spheres for all the bodies and
     * places them into the scene graph. Sets camera, frustrum, and light source
     */
    @Override
    public void simpleInitApp() {

        // set initial cam position and look at the sun - which is at 0,0,0
        cam.setLocation(new Vector3f(initialCam.x, initialCam.y, initialCam.z));
        cam.lookAt(new Vector3f(0,0,0), new Vector3f(0,1,0));

        // a little faster fly speed
        flyCam.setMoveSpeed(CAM_SPEED);

        // place all the bodies into JME
        for (BodyRenderInfo b : bodies) {
            addBody(b);
        }
        bodies = null; // no longer needed

        // set a larger depth because the sim takes up a fair bit of space
        cam.setFrustumFar(FRUSTRUM_FAR);

        // connect the F12 key to handle engaging/disengaging the fly cam
        getInputManager().addMapping(F12MappingName, new KeyTrigger(KeyInput.KEY_F12));
        getInputManager().addListener(f12Listener, F12MappingName);
        // turn off debug stats initially
        stateManager.getState(StatsAppState.class).toggleStats();

        lastRenderTime = System.currentTimeMillis();
    }

    /**
     * Adds the passed BodyRenderInfo to the JME scene graph, and also to the local
     * array of Geometry instances. Each Geometry instance holds the JME-specific info
     * corresponding to a {@code Body} in the simulation.
     *
     * @param b the instance to add
     */
    private Geometry addBody(BodyRenderInfo b) {
        Material mat;
        Sphere sphere;
        if (b.sun) {
            sphere = new Sphere(40, 50, (float) b.radius);
            mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            PointLight pl = new PointLight();
            pl.setPosition(new Vector3f(0, 0, 0));
            pl.setColor(ColorRGBA.White);
            pl.setRadius(0f);
            rootNode.addLight(pl);
        } else {
            sphere = new Sphere(20, 20, (float) b.radius);
            mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
            mat.setFloat("Shininess", 25);
            mat.setBoolean("UseMaterialColors", true);
            mat.setColor("Ambient", ColorRGBA.Black);
            mat.setColor("Diffuse", ColorRGBA.randomColor());
            mat.setColor("Specular", ColorRGBA.Yellow);
        }
        Geometry geo = new Geometry(String.valueOf(b.id), sphere);
        geo.setLocalScale(1f);
        geo.setLocalTranslation((float)b.x, (float)b.y, (float)b.z);
        geo.setMaterial(mat);
        rootNode.attachChild(geo);
        geos.put(b.id, geo);
        return geo;
    }

    /**
     *  Handles the F12 key.
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
     * Updates the positions of all the bodies in the scene graph
     *
     * @param tpf unused
     */
    @Override
    public void simpleUpdate(float tpf) {
        metricComputationMillisJmeSummary.setValue(System.currentTimeMillis() - lastRenderTime);
        lastRenderTime = System.currentTimeMillis();

        ResultQueueHolder.ResultQueue rq = resultQueueHolder.nextComputedQueue();
        if (rq == null) {
            metricNoQueuesCount.incValue();
            return;
        }
        long startTime = System.currentTimeMillis();
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
                if (s.radius < b.radius) {
                    // then this body subsumed another and got larger so gradually increase its size
                    // so it doesn't suddenly pop in size
                    s.updateGeometry(s.getZSamples(), s.getRadialSamples(),
                            (float) Math.min(s.radius + .1F, b.radius));
                }
                // update this body's position
                g.setLocalTranslation((float) b.x, (float) b.y, (float) b.z);
            }
        }
        if (countDetached > 0) {
            logger.info("Detached {} bodies from the root node", countDetached);
        }
        metricRenderCount.incValue();
        metricBodyCountGauge.setValue(rootNode.getChildren().size());
        metricComputationMillisRendererSummary.setValue(System.currentTimeMillis() - startTime);
    }
}