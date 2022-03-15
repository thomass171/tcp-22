package de.yard.threed.maze;

import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.ecs.EcsComponent;
import de.yard.threed.engine.ecs.EcsEntity;

/**
 * 14.3.22: Not needed because a bot is just a regular user?
 *
 * Created by thomass on 08.04.21.
 */
public class BotComponent extends EcsComponent {
    Log logger = Platform.getInstance().getLog(BotComponent.class);

    static String TAG = "BotComponent";

    public BotComponent() {

    }

    @Override
    public String getTag() {
        return TAG;
    }

    public static BotComponent getBotComponent(EcsEntity e) {
        BotComponent m = (BotComponent) e.getComponent(BotComponent.TAG);
        return m;
    }

}
