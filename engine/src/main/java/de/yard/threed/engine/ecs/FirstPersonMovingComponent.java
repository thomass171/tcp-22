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
    boolean firstpersonmovementdebuglog = true;
    //LocalTransform currentposition;
    FirstPersonTransformer firstPersonTransformer;
    boolean automove;

    /**
     *
     */
    public FirstPersonMovingComponent(Transform transform) {
        firstPersonTransformer=new FirstPersonTransformer(transform);
    }

    @Override
    public String getTag() {
        return TAG;
    }


    /**
     * @param amount
     */
    public void moveForward(double amount) {
        /*if (currentposition == null) {
            logger.error("no currentposition.");
            return;// null;
        }*/
        if (firstpersonmovementdebuglog) {
            logger.debug("moveForward: completed,amount=" + amount);
        }
        //return pathcompleted;
        firstPersonTransformer.moveForward(amount);
    }


    public static FirstPersonMovingComponent getFirstPersonMovingComponent(EcsEntity e) {
        return (FirstPersonMovingComponent) e.getComponent(FirstPersonMovingComponent.TAG);
    }

    /*public LocalTransform getCurrentposition() {
        return currentposition;
    }*/


    public boolean hasAutomove() {
        return automove;
    }
}
