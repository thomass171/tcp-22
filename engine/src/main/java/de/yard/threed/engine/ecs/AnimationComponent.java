package de.yard.threed.engine.ecs;

import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.SceneNode;

/**
 * A component for (entity) model that have animations.
 * Currently its used for highlighting/marking an avatar by scaling it up. Probably will evolve in some direction.
 *
 * Created by thomass on 5.4.22
 */
public class AnimationComponent extends EcsComponent {
    Log logger = Platform.getInstance().getLog(AnimationComponent.class);

    public static String TAG = "AnimationComponent";

    private Vector3 originScale = null;
    private SceneNode coreNode = null;

    public AnimationComponent(SceneNode coreNode) {
        this.coreNode = coreNode;
        this.originScale = coreNode.getTransform().getScale();
    }

    @Override
    public String getTag() {
        return TAG;
    }

    public static AnimationComponent getAnimationComponent(EcsEntity e) {
        AnimationComponent ac = (AnimationComponent) e.getComponent(AnimationComponent.TAG);
        return ac;
    }

    public void setMarkedEnabled(boolean b) {
        if (coreNode != null) {
            coreNode.getTransform().setScale(b ? originScale.multiply(1.2) : originScale);
        }
    }
}
