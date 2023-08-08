package de.yard.threed.engine.ecs;

import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.avatar.TeamColor;

/**
 * For users/entites that joined.
 *
 * <p>
 * Created by thomass on 8.8.23.
 */
public class JoinComponent extends EcsComponent {
    Log logger = Platform.getInstance().getLog(JoinComponent.class);

    public static String TAG = "JoinComponent";

    public TeamColor teamColor;

    public JoinComponent() {
    }

    @Override
    public String getTag() {
        return "JoinComponent";
    }

    public static JoinComponent getJoinComponent(EcsEntity e) {
        JoinComponent jc = (JoinComponent) e.getComponent(JoinComponent.TAG);
        return jc;
    }
}
