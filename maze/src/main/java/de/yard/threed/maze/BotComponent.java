package de.yard.threed.maze;

import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.ecs.EcsComponent;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.engine.util.IntProvider;

/**
 * 14.3.22: Not needed because a bot is just a regular user?
 * A bot is not a regular user but a bot. For example a bot doesn't log in.
 * <p>
 * Created by thomass on 08.04.21.
 */
public class BotComponent extends EcsComponent {
    Log logger = Platform.getInstance().getLog(BotComponent.class);

    static String TAG = "BotComponent";
    BotAI botAI = new SimpleBotAI();
    long lastActionAt = 0;
    static long WAIT_TIME = 1500;
    public boolean monster;

    public BotComponent() {
    }

    public boolean isReadyToAct() {
        long current = Platform.getInstance().currentTimeMillis();
        if (current > lastActionAt + WAIT_TIME) {
            return true;
        }
        return false;
    }

    public Request getNextRequest(GridMover mover, GridState gridState, MazeLayout layout, IntProvider rand) {
        lastActionAt = Platform.getInstance().currentTimeMillis();
        return botAI.getNextRequest(mover, gridState, layout, rand);
    }

    @Override
    public String getTag() {
        return TAG;
    }

    public static BotComponent getBotComponent(EcsEntity e) {
        BotComponent m = (BotComponent) e.getComponent(BotComponent.TAG);
        return m;
    }

    public static BotComponent buildFromGridDefinition(StartPosition startPosition) {
        BotComponent bc = new BotComponent();
        bc.monster = startPosition.isMonster;
        return bc;
    }

    public boolean isMonster() {
        return monster;
    }
}
