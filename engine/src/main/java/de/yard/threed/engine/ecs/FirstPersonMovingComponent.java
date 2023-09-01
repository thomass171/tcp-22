package de.yard.threed.engine.ecs;


import de.yard.threed.core.LocalTransform;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;

/**
 * .
 * <p>
 * Created by thomass on 24.08.23.
 */
public class FirstPersonMovingComponent extends EcsComponent {
    static Log logger = Platform.getInstance().getLog(FirstPersonMovingComponent.class);
    public static String TAG = "FirstPersonMovingComponent";
    boolean firstpersonmovementdebuglog = false;
    LocalTransform currentposition;
    boolean automove;

    /**
     *
     */
    public FirstPersonMovingComponent() {
    }

    @Override
    public String getTag() {
        return TAG;
    }


    /**
     * @param amount
     */
    public void moveForward(double amount) {
        if (currentposition == null) {
            logger.error("no currentposition.");
            return;// null;
        }
        if (firstpersonmovementdebuglog) {
            logger.debug("moveForward: completed,currentposition=" + currentposition);
        }
        //return pathcompleted;
    }


    public static FirstPersonMovingComponent getFirstPersonMovingComponent(EcsEntity e) {
        return (FirstPersonMovingComponent) e.getComponent(FirstPersonMovingComponent.TAG);
    }

    public LocalTransform getCurrentposition() {
        return currentposition;
    }


    public boolean hasAutomove() {
        return automove;
    }
}
