package de.yard.threed.engine.vr;

import de.yard.threed.core.Color;
import de.yard.threed.core.Util;
import de.yard.threed.core.Vector2;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.LocalTransform;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.Camera;
import de.yard.threed.engine.Observer;
import de.yard.threed.engine.Scene;
import de.yard.threed.engine.Transform;
import de.yard.threed.engine.World;
import de.yard.threed.engine.gui.ControlPanel;
import de.yard.threed.engine.gui.ControlPanelHelper;
import de.yard.threed.engine.platform.EngineHelper;


/**
 * Three possible modes:
 * 1) moves Player/Observer
 * 2) moves world (not yet)
 * 3) emulated
 * <p>
 * VR Controller no longer attached to avatar model
 * 31.1.24: boolean "vrEnabled/enableVR" replaced by "vrMode"
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
    private Vector3 offsetVR;
    private static VrInstance instance = null;
    // the transform where to attach a control panel to the controller
    private LocalTransform cpTransform = null;
    private static VRController controller0 = null;
    private static VRController controller1 = null;
    private Vector3 emulatedControllerPosition = new Vector3(0, 0, 0);
    String vrMode = null;

    private VrInstance(int mode, Vector3 offsetVR) {
        this.mode = mode;
        this.offsetVR = offsetVR;
        vrMode = Platform.getInstance().getConfiguration().getString("vrMode");
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
     * AR is also just VR inside this context.
     *
     * @return
     */
    public static VrInstance buildFromArguments() {

        boolean emulated = false;
        // For testing VR panel outside VR
        if (EngineHelper.isEnabled("emulateVR")) {
            emulated = true;
        } else {
            if (!isEnabled()) {
                return null;
            }
        }

        Vector3 val = new Vector3();
        String s_offsetVR = Platform.getInstance().getConfiguration().getString("offsetVR");
        if (s_offsetVR != null) {
            val = Util.parseVector3(s_offsetVR);
        }
        instance = new VrInstance(emulated ? MODE_EMULATED : MODE_OBSERVER, val);

        String cpposrot = (Platform.getInstance()).getConfiguration().getString("vr-controlpanel-posrot");
        if (cpposrot != null) {

            if ((instance.cpTransform = LocalTransform.buildFromConfig(cpposrot)) == null) {
                logger.warn("Ignoring invalid vr-controlpanel-posrot " + cpposrot);
            }
        }
        if (emulated) {
            Camera camera = Scene.getCurrent().getDefaultCamera();
            // put banner centered at top screen/display/window border
            ControlPanel controlPanel = ControlPanelHelper.buildForNearplaneBanner(camera, Scene.getCurrent().getDimension(), Color.GREEN);
            if (controlPanel != null) {
                // headless?
                ControlPanelHelper.addText(controlPanel, "VR emulation", new Vector2(0, 0), controlPanel.getSize());
            }
        }
        return instance;
    }

    private void raiseWorld(double offset) {
        World world = Scene./*((EngineHelper) Platform.getInstance()).*/getCurrent().getWorld();
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

    public Vector3 getOffsetVR() {
        return offsetVR;
    }

    /**
     * For tests.
     */
    public static void reset() {
        instance = null;
    }

    public static boolean isEnabled() {
        return Platform.getInstance().getConfiguration().getString("vrMode") != null;
    }

    /**
     * Try to find the controller. Depending on the platform and the state of the VR system, the controller might
     * not be available yet.
     */
    private void probeController() {
        if (isEmulated()) {
            controller0 = VRController.getEmulatedController(emulatedControllerPosition);
            controller1 = VRController.getEmulatedController(emulatedControllerPosition);
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
            // make it visible below the "emulateVR" banner. better to observer? That might effectively be the same.
            // control panels in this case typical are too large to fit on z-plane. -3 is acceptable for all TravelScene, MazeScene and VrScene.
            // Thus y cannot also not be calculated from z-plane size.
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

    public void dumpDebugInfo() {
        Camera camera = Scene.getCurrent().getDefaultCamera();
        logger.info("offsetVR=" + ((VrInstance.getInstance() != null) ? ("" + VrInstance.getInstance().getOffsetVR()) : ""));
        logger.info("cam vr pos=" + camera.getVrPosition(true));
        logger.info("world pos=" + Scene.getCurrent().getWorld().getTransform().getPosition());
        Transform carrier = camera.getCarrier().getTransform();
        logger.info("observer set pos=" + Observer.getInstance().getPosition());
        logger.info("observer finetune=" + Observer.getInstance().getFinetune());
        logger.info("observer real pos=" + Observer.getInstance().getTransform().getPosition());
        logger.info("carrier pos=" + carrier.getPosition());
        while (carrier.getParent() != null) {
            carrier = carrier.getParent();
            String name = "<unknwon>";
            if (carrier.getSceneNode() != null && carrier.getSceneNode().getName() != null) {
                name = carrier.getSceneNode().getName();
            }
            logger.info("carrier parent pos=" + carrier.getPosition() + ", name=" + name);
        }
    }

    public void setEmulatedControllerPosition(Vector3 v) {
        emulatedControllerPosition = v;
    }

    public static boolean isAR(VrInstance vrInstance) {
        return vrInstance != null && vrInstance.isAR();
    }

    public boolean isAR() {
        return vrMode != null && vrMode.equals("AR");
    }
}
