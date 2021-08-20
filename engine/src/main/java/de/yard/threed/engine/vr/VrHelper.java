package de.yard.threed.engine.vr;

import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;

/**
 * MA33: Ausgelagert von Avatar.
 * <p>
 * Controller no longer attached to avatar model
 */
public class VrHelper {
    static Log logger = Platform.getInstance().getLog(VrHelper.class);

    private static VRController controller0 = null;
    private static VRController controller1 = null;


    private static void probeController() {
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

    /**
     * keep track of VR subsystem.
     * hier muss aber nicht den probe machen. Der reicht wahrscheinlich einmalig.
     */
    public static void update() {

        //probeController();
    }

    public static VRController getController(int i) {
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


}
