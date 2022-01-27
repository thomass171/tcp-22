package de.yard.threed.engine.avatar;

import de.yard.threed.core.Vector3;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.Observer;
import de.yard.threed.engine.ecs.EcsComponent;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.core.platform.Log;

/**
 * 24.1.22: VR (vrdown,vrOffsetPosition) is no longer an avatar issue but an {@link Observer} issue.
 * Currently this component isn't really needed.
 * <p>
 * Created by thomass on 27.11.20.
 */
public class AvatarComponent extends EcsComponent {
    Log logger = Platform.getInstance().getLog(AvatarComponent.class);

    public static String TAG = "AvatarComponent";

    public AvatarComponent() {
    }

    @Override
    public String getTag() {
        return "AvatarComponent";
    }

    public static AvatarComponent getAvatarComponent(EcsEntity e) {
        AvatarComponent gmc = (AvatarComponent) e.getComponent(AvatarComponent.TAG);
        return gmc;
    }
}
