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
        MoverComponent mc = MoverComponent.getMoverComponent(entity);

        if (botsystemdebuglog) {
        }

        // only spend time for grid state collection if bot is really willing to act
        // join might not have been completely processed, so check for 'mc'.
        if (mc != null && !mc.isMoving() && bc.isReadyToAct()) {
            GridState currentstate = MazeUtils.buildGridStateFromEcs();

            // For simplicity pass complete grid state, even this is not quite 'fair'
            // more fair will be to pass only visible fields
            // getVisibleFields(mc.getLocation(),currentstate));
            Request request = bc.getNextRequest(mc, currentstate, Grid.getInstance().getMazeLayout());

            if (request != null) {
                request.setUserEntityId(entity.getId());
                SystemManager.putRequest(request);
            }
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
            // Start a bot for remaining players. But only once, the login request fired here will also trigger a JOINED event again.
            // Be prepared for inconsistent (negative) botNeeded.
            for (int i = 0; botsNeeded > 0 && i < botsNeeded; i++) {
                // A bot is no logged in user, thus will only join
                EcsEntity user = new EcsEntity(new BotComponent());
                user.setName("Bot" + i);
                SystemManager.putRequest(UserSystem.buildJoinRequest(user.getId(), false));
            }
            botsNeeded = 0;
        }
    }
}
