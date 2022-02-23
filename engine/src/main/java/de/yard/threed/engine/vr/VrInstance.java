package de.yard.threed.engine.vr;

import de.yard.threed.core.Color;
import de.yard.threed.core.DimensionF;
import de.yard.threed.core.Vector2;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.LocalTransform;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.platform.PlatformHelper;
import de.yard.threed.engine.Camera;
import de.yard.threed.engine.Scene;
import de.yard.threed.engine.World;
import de.yard.threed.engine.gui.ControlPanel;
import de.yard.threed.engine.gui.ControlPanelHelper;
import de.yard.threed.engine.gui.Hud;
import de.yard.threed.engine.gui.TextTexture;
import de.yard.threed.engine.platform.EngineHelper;


/**
 * Three possible modes:
 * 1) moves Player/Observer
 * 2) moves world (not yet)
 * 3) emulated
 * <p>
 * VR Controller no longer attached to avatar model
 */
public class VrInstance {
    static Log logger = Platform.getInstance().getLog(VrInstance.class);

    private static int MODE_OBSERVER = 1;
    // VR emulation: Shows controller control panel as hud and make mouse create controller/trigger moves/events
    // not intended for unit testing but for visual testing
    private static int MODE_EMULATED = 3;
    //World mach ich erstmal nicht, denn dann muesste der finetune aus dem Observer generalisiert werden.
    //private static int MODE_WORLD = 2;

    private int mode;
    private double yoffsetVR;
    private static VrInstance instance = null;
    // the transform where to attach a control panel to the controller
    private LocalTransform cpTransform = null;
    private static VRController controller0 = null;
    private static VRController controller1 = null;

    private VrInstance(int mode, double yoffsetVR) {
        this.mode = mode;
        this.yoffsetVR = yoffsetVR;
    }

    /*public static VrInstance getObserverMovingInstance() {
        VrInstance vrInstance = new VrInstance(MODE_OBSERVER, 0);
        return vrInstance;
    }

    public static VrInstance getWorldMovingInstance() {
        return (VrInstance) Util.notyet();
    }*/

    public static VrInstance getInstance() {
        return instance;
    }

    /**
     * Returns null if VR isn't enabled.
     *
     * @return
     */
    public static VrInstance buildFromArguments() {

        boolean emulated = false;
        // For testing VR panel outside VR
        if (EngineHelper.isEnabled("argv.emulateVR")) {
            emulated = true;
        } else {
            if (!isEnabled()) {
                return null;
            }
        }
        Double yoffsetVR = PlatformHelper.getDoubleSystemProperty("argv.yoffsetVR");
        double val = (double) ((yoffsetVR == null) ? 0 : yoffsetVR);
        instance = new VrInstance(emulated ? MODE_EMULATED : MODE_OBSERVER, val);

        String cpposrot = (Platform.getInstance()).getSystemProperty("argv.vr-controlpanel-posrot");
        if (cpposrot != null) {

            if ((instance.cpTransform = LocalTransform.buildFromConfig(cpposrot)) == null) {
                logger.warn("Ignoring invalid vr-controlpanel-posrot " + cpposrot);
            }
        }
        if (emulated) {
            Camera camera = Scene.getCurrent().getDefaultCamera();
            ControlPanel controlPanel = ControlPanelHelper.buildForNearplaneBanner(camera, Scene.getCurrent().getDimension(), Color.GREEN);
            if (controlPanel != null) {
                // headless?
                ControlPanelHelper.addText(controlPanel, "VR emulation", new Vector2(0, 0), controlPanel.getSize());
            }
        }
        return instance;
    }

    private void raiseWorld(double offset) {
        World world = Scene./*((EngineHelper) Platform.getInstance()).*/getWorld();
        Vector3 p = world.getTransform().getPosition();
        logger.info("raising world pos " + p + " by " + offset);
        p = new Vector3(p.getX(), p.getY() + offset, p.getZ());
        world.getTransform().setPosition(p);
    }

    /**
     * Increase the offset between world and observer. Either by raising the world or by lowering carrier.
     */
    public void increaseOffset(Double yoffset) {
        /*erstmal nicht if (mode == MODE_WORLD){
            raiseWorld(yoffset);
        }*/
    }

    public LocalTransform getCpTransform() {
        return cpTransform;
    }

    public double getYoffsetVR() {
        return yoffsetVR;
    }

    /**
     * For tests.
     */
    public static void reset() {
        instance = null;
    }

    public static boolean isEnabled() {
        return EngineHelper.isEnabled("argv.enableVR");
    }

    /**
     * Try to find the controller. Depending on the platform and the state of the VR system, the controller might
     * not be available yet.
     */
    private void probeController() {
        if (isEmulated()) {
            controller0 = VRController.getDummyController();
            controller1 = VRController.getDummyController();
        } else {
            //zu frueh? Eigentlich wohl schon, zumindest ThreeJs liefert aber immer eine Node, die dann erst später befüllt wird.
            controller0 = VRController.getController(0);
            //logger.debug("controller0=" + controller0);
            if (controller0 != null) {
                //attach(controller0);
            }
            controller1 = VRController.getController(1);
            //logger.debug("controller1=" + controller1);
            if (controller1 != null) {
                //attach(controller1);
            }
        }
    }

    /**
     * keep track of VR subsystem.
     * hier muss aber nicht den probe machen. Der reicht wahrscheinlich einmalig.
     */
    public static void update() {

        //probeController();
    }

    public VRController getController(int i) {
        if (controller0 == null || controller1 == null) {
            probeController();
        }
        if (i == 0) {
            return controller0;
        }
        return controller1;
    }

    public static void adjustWorld() {

    }

    public void attachControlPanelToController(VRController vrController, ControlPanel controllerPanel) {

        if (isEmulated()) {
            // make it visible below the "VR emulation" banner. better to observer? TODO calc correct/best y position
            controllerPanel.getTransform().setPosition(new Vector3(0, 0.8, -3));
            controllerPanel.getTransform().setParent(Scene.getCurrent().getDefaultCamera().getCarrierTransform());
        } else {
            LocalTransform lt = getCpTransform();
            if (lt != null) {
                //leftControllerPanel.getTransform().setPosition(new Vector3(-0.5, 1.5, -2.5));
                //200,90,0 are good rotations
                controllerPanel.getTransform().setPosition(lt.position);
                controllerPanel.getTransform().setRotation(lt.rotation);
                controllerPanel.getTransform().setScale(new Vector3(0.4, 0.4, 0.4));
            }
            vrController.attach(controllerPanel);
        }
    }

    public boolean isEmulated() {
        return mode == MODE_EMULATED;
    }
}
