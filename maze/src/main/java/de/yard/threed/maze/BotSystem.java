package de.yard.threed.maze;

import de.yard.threed.core.Event;
import de.yard.threed.core.EventType;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.BaseEventRegistry;
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
 * Creates bots for remaining player after the first player joined.
 *
 * <p>
 * Created by thomass on 09.04.21.
 */

public class BotSystem extends DefaultEcsSystem {
    private static Log logger = Platform.getInstance().getLog(BotSystem.class);
    public static String TAG = "BotSystem";
    private boolean botsystemdebuglog = true;
    private List<List<StartPosition>> startPositions;
    private IntProvider rand = new RandomIntProvider();
    private boolean serverMode;
    private BotAiBuilder botAiBuilder;

    /**
     * Isn't a bot system always in server?
     */
    public BotSystem(boolean serverMode, BotAiBuilder botAiBuilder) {
        super(new String[]{BotComponent.TAG},
                new RequestType[]{},
                new EventType[]{MazeEventRegistry.EVENT_MAZE_LOADED, BaseEventRegistry.USER_EVENT_JOINED});
        this.serverMode = serverMode;
        this.botAiBuilder = botAiBuilder;
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

        if (evt.getType().equals(MazeEventRegistry.EVENT_MAZE_LOADED)) {
            // take grid from payload because dataprovider are not available in pure client mode (which probably doesn't apply for BotSystem.
            // but it makes it more consistent.
            String rawGrid = (String) evt.getPayload().get("grid");
            Grid grid = Grid.loadFromRaw(rawGrid);
            if (grid == null) {
                logger.error("no or invalid grid in payload");
            } else {
                startPositions = grid.getMazeLayout().getStartPositions();
            }
        }
        if (evt.getType().equals(BaseEventRegistry.USER_EVENT_JOINED) && startPositions != null) {
            // The latest joined user isn't really interesting but the total of already joined user.
            // currentPlaver will only contain the login user, no bots exist yet.
            List<EcsEntity> currentPlayer = MazeUtils.getPlayer();

            // Start a bot for remaining players (including monster) when running standalone.
            // But only once, the login request fired here will also trigger a JOINED event again.
            if (serverMode) {
                // In server mode we should wait for additional user joining.
                if (currentPlayer.size() >= Grid.getInstance().getMazeLayout().getStartPositionCount(true)) {
                    startRemainingPlayer(currentPlayer);
                }
            } else {
                // In non server mode the joined user is alone, so we need to start bots for remaining player and start monster
                startRemainingPlayer(currentPlayer);
            }
            startPositions = null;
        }
    }

    @Override
    public String getTag() {
        return TAG;
    }

    public void setIntProvider(IntProvider intProvider) {
        this.rand = intProvider;
    }

    /**
     * Start player/monster bots. Both don't need a login but just join.
     */
    private void startRemainingPlayer(List<EcsEntity> currentPlayer) {

        // find team/start position of current user for ignoring this. No longer, in multi player a bot might complete a team.
        // In fact, the start position isn't really used here, just for identifying player/monster
        // int startTeam = MoverComponent.getMoverComponent(startUser).getGridMover().getTeam();
        // logger.debug("Launching bots ignoring positions of team " + startTeam);
        int botIndex = 0;
        int skipped = 0;
        for (int i = 0; i < startPositions.size(); i++) {
            // A bot is no logged in user, thus will only join
            for (StartPosition startPosition : startPositions.get(i)) {
                EcsEntity user = null;
                if (startPosition.isMonster) {
                    user = new EcsEntity(BotComponent.buildFromGridDefinition(true, botAiBuilder.build()));
                } else {
                    // skip already logged in user
                    if (skipped >= currentPlayer.size()) {
                        user = new EcsEntity(BotComponent.buildFromGridDefinition(false, botAiBuilder.build()));
                    } else {
                        skipped++;
                    }
                }
                if (user != null) {
                    //TODO improve unique naming
                    user.setName("Bot" + (botIndex));
                    SystemManager.putRequest(UserSystem.buildJoinRequest(user.getId()/*, false*/));
                    botIndex++;
                }
            }
        }
        logger.debug("Launched " + botIndex + " bots. skipped " + skipped);
    }
}
