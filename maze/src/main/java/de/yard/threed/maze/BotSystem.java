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
import de.yard.threed.engine.util.IntProvider;
import de.yard.threed.engine.util.RandomIntProvider;

import java.util.List;

/**
 * <p>
 * Created by thomass on 09.04.21.
 */

public class BotSystem extends DefaultEcsSystem {
    private static Log logger = Platform.getInstance().getLog(BotSystem.class);

    private boolean botsystemdebuglog = true;
    private List<List<StartPosition>> startPositions;
    private IntProvider rand = new RandomIntProvider();

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
            Request request = bc.getNextRequest(mc, currentstate, Grid.getInstance().getMazeLayout(), rand);

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
            startPositions = grid.getMazeLayout().getStartPositions();
        }
        if (evt.getType().equals(UserSystem.USER_EVENT_JOINED)) {
            // Start a bot for remaining players. But only once, the login request fired here will also trigger a JOINED event again.
            // Be prepared for inconsistent (negative) botNeeded.
            if (startPositions != null) {
                logger.debug("Launching bots");
                for (int i = 1; i < startPositions.size(); i++) {
                    // A bot is no logged in user, thus will only join
                    for (StartPosition startPosition : startPositions.get(i)) {
                        EcsEntity user = new EcsEntity(BotComponent.buildFromGridDefinition(startPosition));
                        //TODO improve unique naming
                        user.setName("Bot" + (i - 1));
                        SystemManager.putRequest(UserSystem.buildJoinRequest(user.getId(), false));
                    }
                }
            }
            startPositions = null;
        }
    }

    public void setIntProvider(IntProvider intProvider) {
        this.rand = intProvider;
    }
}
