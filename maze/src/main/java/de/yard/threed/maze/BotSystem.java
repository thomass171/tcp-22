package de.yard.threed.maze;

import de.yard.threed.core.Event;
import de.yard.threed.core.EventType;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.ecs.DefaultEcsSystem;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.EcsGroup;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.ecs.UserSystem;
import de.yard.threed.engine.platform.common.*;

import static de.yard.threed.engine.ecs.UserSystem.buildJoinRequest;

/**
 * <p>
 * Created by thomass on 09.04.21.
 */

public class BotSystem extends DefaultEcsSystem {
    private static Log logger = Platform.getInstance().getLog(BotSystem.class);

    private boolean botsystemdebuglog = true;
    private int botsNeeded = 0;

    /**
     *
     */
    public BotSystem() {
        super(new String[]{BotComponent.TAG},
                new RequestType[]{},
                new EventType[]{EventRegistry.EVENT_MAZE_LOADED, UserSystem.USER_EVENT_JOINED});
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

    @Override
    public void process(Event evt) {

        if (botsystemdebuglog) {
            logger.debug("got event " + evt.getType());
        }

        if (evt.getType().equals(EventRegistry.EVENT_MAZE_LOADED)) {
            Grid grid = (Grid) evt.getPayloadByIndex(0);
            botsNeeded = grid.getMazeLayout().getNumberOfTeams() - 1;
        }
        if (evt.getType().equals(UserSystem.USER_EVENT_JOINED)) {
            // Start a bot for remaining players. But only once, the login request fired here will als trigger a JOINED event again.
            // Be prepared for inconsistent (negative) botNeeded.
            for (int i = 0; botsNeeded > 0 && i < botsNeeded; i++) {
                // A bot is no logged in user, thus will only join
                EcsEntity user = new EcsEntity(new BotComponent());
                user.setName("Bot" + i);
                SystemManager.putRequest(buildJoinRequest(user.getId(), false));
            }
            botsNeeded = 0;
        }
    }
}
