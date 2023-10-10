package de.yard.threed.engine.ecs;


import de.yard.threed.core.LocalTransform;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.FirstPersonTransformer;
import de.yard.threed.engine.Transform;

/**
 * .
 * <p>
 * Created by thomass on 24.08.23.
 */
public class FirstPersonMovingComponent extends EcsComponent {
    static Log logger = Platform.getInstance().getLog(FirstPersonMovingComponent.class);
    public static String TAG = "FirstPersonMovingComponent";
    public boolean firstpersonmovementdebuglog = true;
    private FirstPersonTransformer firstPersonTransformer;
    private boolean autoForward, autoBack, autoTurnleft, autoTurnright, autoTurnup, autoTurndown, autoRollleft, autoRollright;

    /**
     *
     */
    public FirstPersonMovingComponent(Transform transform) {
        firstPersonTransformer = new FirstPersonTransformer(transform, FirstPersonTransformer.ROTATE_MODE_PERAXIS);
    }

    @Override
    public String getTag() {
        return TAG;
    }


    /**
     * @param amount
     */
    public void moveForward(double amount) {
        if (firstpersonmovementdebuglog) {
            logger.debug("moveForward: completed,amount=" + amount);
        }
        //return pathcompleted;
        firstPersonTransformer.moveForwardAsCamera(amount);
    }

    public void moveForwardByDelta(double delta) {

        if (firstpersonmovementdebuglog) {
            logger.debug("moveForward: completed,delta=" + delta);
        }
        //return pathcompleted;
        firstPersonTransformer.moveForwardAsCameraByDelta(delta);
    }

    public void autoMoveByDelta(double delta) {

        if (autoForward) {
            firstPersonTransformer.moveForwardAsCameraByDelta(delta);
        }
        if (autoBack) {
            firstPersonTransformer.moveForwardAsCameraByDelta(-delta);
        }
        if (autoTurnleft) {
            firstPersonTransformer.incHeadingByDelta(delta);
        }
        if (autoTurnright) {
            firstPersonTransformer.incHeadingByDelta(-delta);
        }
        if (autoTurnup) {
            firstPersonTransformer.incPitchByDelta(delta);
        }
        if (autoTurndown) {
            firstPersonTransformer.incPitchByDelta(-delta);
        }
        if (autoRollleft) {
            firstPersonTransformer.incRollByDelta(delta);
        }
        if (autoRollright) {
            firstPersonTransformer.incRollByDelta(-delta);
        }
    }

    public static FirstPersonMovingComponent getFirstPersonMovingComponent(EcsEntity e) {
        return (FirstPersonMovingComponent) e.getComponent(FirstPersonMovingComponent.TAG);
    }

    public FirstPersonTransformer getFirstPersonTransformer() {
        return firstPersonTransformer;
    }

    public boolean hasAutoForward() {
        return autoForward;
    }

    public void toggleAutoForward() {
        autoForward = !autoForward;
    }

    public void toggleAutoBack() {
        autoBack = !autoBack;
    }

    public void toggleAutoTurnleft() {
        autoTurnleft = !autoTurnleft;
    }

    public void toggleAutoTurnright() {
        autoTurnright = !autoTurnright;
    }

    public void toggleAutoTurnup() {
        autoTurnup = !autoTurnup;
    }

    public void toggleAutoTurndown() {
        autoTurndown = !autoTurndown;
    }

    public void toggleAutoRollleft() {
        autoRollleft = !autoRollleft;
    }

    public void toggleAutoRollright() {
        autoRollright = !autoRollright;
    }
}
