package de.yard.threed.engine.vr;

import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.LocalTransform;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.Scene;
import de.yard.threed.engine.World;
import de.yard.threed.engine.platform.EngineHelper;


/**
 * Es gibt zwei:
 * 1) bewegt Player/Observer
 * 2) bewegt world
 */
public class VrInstance {
    static Log logger = Platform.getInstance().getLog(VrInstance.class);

    private static int MODE_OBSERVER = 1;
    //World mach ich erstmal nicht, denn dann muesste der finetune aus dem Observer generalisiert werden.
    //private static int MODE_WORLD = 2;

    private int mode;
    private double yoffsetVR;
    private static VrInstance instance = null;
    private LocalTransform cpTransform = null;

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
        boolean enableLoweredAvatar = false;

        if (!isEnabled()) {
            return null;
        }
        /*deprecated if (EngineHelper.isEnabled("argv.enableLoweredAvatar")) {
            enableLoweredAvatar = true;
        }*/
        Double yoffsetVR = EngineHelper.getDoubleSystemProperty("argv.yoffsetVR");
        double val = (double) ((yoffsetVR == null) ? 0 : yoffsetVR);
        instance = new VrInstance(MODE_OBSERVER, val);

        String cpposrot = (Platform.getInstance()).getSystemProperty("argv.vr-controlpanel-posrot");
        if (cpposrot != null) {

            if ((instance.cpTransform = LocalTransform.buildFromConfig(cpposrot)) == null) {
                logger.warn("Ignoring invalid vr-controlpanel-posrot " + cpposrot);
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
}
