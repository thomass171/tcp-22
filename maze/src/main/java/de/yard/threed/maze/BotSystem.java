package de.yard.threed.maze;

import de.yard.threed.core.EventType;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.ecs.DefaultEcsSystem;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.EcsGroup;
import de.yard.threed.engine.platform.common.*;

/**
 * <p>
 * Created by thomass on 09.04.21.
 */

public class BotSystem extends DefaultEcsSystem {
    private static Log logger = Platform.getInstance().getLog(BotSystem.class);

    boolean botsystemdebuglog = false;

    /**
     *
     */
    public BotSystem() {
        super(new String[]{BotComponent.TAG}, new RequestType[]{}, new EventType[]{});
    }

    @Override
    public void update(EcsEntity entity, EcsGroup group, double tpf) {
        BotComponent bc = BotComponent.getBotComponent(entity);


        if (botsystemdebuglog) {
        }
    }

    @Override
    public boolean processRequest(Request request) {
        if (botsystemdebuglog) {
            logger.debug("got request " + request.getType());
        }

        return false;
    }



}
