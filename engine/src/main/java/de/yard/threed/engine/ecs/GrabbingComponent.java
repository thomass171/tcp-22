package de.yard.threed.engine.ecs;


import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;

/**
 * For entities that can be grabbed.
 * <p>
 * Created by thomass on 24.01.24.
 */
public class GrabbingComponent extends EcsComponent {
    static Log logger = Platform.getInstance().getLog(GrabbingComponent.class);
    public static String TAG = "GrabbingComponent";
    public int grabbedBy = -1;
    private GrabDetector grabDetector;

    /**
     *
     */
    public GrabbingComponent() {
        grabDetector = (wp) -> {
            // standard detector by distance
            if (getEntity().getSceneNode() != null) {
                var distance = Vector3.getDistance(wp, getEntity().getSceneNode().getTransform().getPosition());
                logger.debug("distance=" + distance);
                if (distance < GrabbingSystem.grabDistance) {
                    return true;
                }
            }
            return false;
        };
    }

    public boolean grabs(Vector3 worldPosition){
        if (grabDetector.canGrap(worldPosition)){
            return true;
        }
        return false;
    }

    @Override
    public String getTag() {
        return TAG;
    }

    public static GrabbingComponent getGrabbingComponent(EcsEntity e) {
        return (GrabbingComponent) e.getComponent(GrabbingComponent.TAG);
    }
}
