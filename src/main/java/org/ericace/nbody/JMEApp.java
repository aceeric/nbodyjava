package org.ericace.nbody;

import com.jme3.app.FlyCamAppState;
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

import java.util.ArrayList;

/**
 * Integrates with the JMonkeyEngine game engine
 */
public class JMEApp extends SimpleApplication {
    private static final Logger logger = LogManager.getLogger(JMEApp.class);

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
    private static final String F12 = "F12";

    /**
     * True if the fly cam is currently attached, else false
     */
    private boolean flyCamAttached = true;

    /**
     * Holds the bodies in the simulation - they are indexed by the ID of the {@link BodyRenderInfo}
     * passed to the class for rendering
     */
    private Geometry [] geos;

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
        geos = new Geometry[bodies.size()];
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
            Material mat;
            Sphere sphere;
            if (b.sun) {
                sphere = new Sphere(40, 50, (float) b.radius);
                mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
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
            geos[b.id] = geo;
        }
        bodies = null; // no longer needed

        // set a larger depth because the sim takes up a fair bit of space
        cam.setFrustumFar(FRUSTRUM_FAR);

        // shines from the sun position
        PointLight pl = new PointLight();
        pl.setPosition(new Vector3f(0, 0, 0));
        pl.setColor(ColorRGBA.White);
        pl.setRadius(0f);
        rootNode.addLight(pl);

        // connect the F12 key to handle engaging/disengaging the fly cam
        getInputManager().addMapping(F12, new KeyTrigger(KeyInput.KEY_F12));
        getInputManager().addListener(f12Listener, F12);
        stateManager.getState(StatsAppState.class).toggleStats();
    }

    /**
     *  Handles the F12 key.
     *  <p>
     *  If the fly cam is currently connected, then the F12 key disconnects the flycam from the JME window so
     *  the user can tab into other windows while the sim is running.</p>
     *  <p>
     *  If the fly cam is currently <i>not</i> connected, then the F12 key connects the flycam so
     *  the user can use it to navigate within the sim</p>
     */
    private ActionListener f12Listener = new ActionListener() {
        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            if (name.equals(F12) && isPressed) {
                if (flyCamAttached) {
                    stateManager.detach(stateManager.getState(FlyCamAppState.class));
                } else {
                    FlyCamAppState state = new FlyCamAppState();
                    state.initialize(getStateManager(), JMEApp.this);
                    stateManager.attach(state);
                    flyCam = state.getCamera();
                    flyCam.setMoveSpeed(CAM_SPEED);
                }
                flyCamAttached = !flyCamAttached;
            }
        }
    };

    /**
     * Updates the positions of all the bodies
     *
     * @param tpf unused
     */
    @Override
    public void simpleUpdate(float tpf) {
        ResultQueueHolder.ResultQueue rq = resultQueueHolder.nextComputedQueue();
        if (rq == null) {
            logger.warn("No available result queue");
            return;
        }
        logger.info("Starting render cycle with rq.queNum={}", rq.getQueNum());
        for (BodyRenderInfo b : rq.getQueue()) {
            if (!b.exists) {
                if (geos[b.id] != null) {
                    rootNode.detachChild(geos[b.id]); // remove from the scene graph
                    geos[b.id] = null;
                    logger.info("Setting ID {} to null", b.id);
                }
            } else {
                if (geos[b.id] == null) {
                    logger.error("ERROR: ID {} is null in geos", b.id);
                    throw new RuntimeException("Null element in geos");
                }
                Sphere s = (Sphere) geos[b.id].getMesh();
                if (s.radius < b.radius) {
                    // then this body subsumed another and got larger so gradually increase its size
                    // so it doesn't suddenly pop in size
                    s.updateGeometry(s.getZSamples(), s.getRadialSamples(),
                            (float) Math.min(s.radius + .1F, b.radius));
                }
                // update this body's position
                geos[b.id].setLocalTranslation((float) b.x, (float) b.y, (float) b.z);
            }
        }
    }
}