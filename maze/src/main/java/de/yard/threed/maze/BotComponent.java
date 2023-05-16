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
    // dont' set default Bot AI. In tests a deterministic might be better.
    BotAI botAI;
    public boolean monster;

    public BotComponent(BotAI botAI) {
        this.botAI = botAI;
    }

    public boolean isReadyToAct() {
        return (botAI == null) ? false : botAI.isReadyToAct();
    }

    public Request getNextRequest(GridMover mover, GridState gridState, MazeLayout layout, IntProvider rand) {
        return (botAI == null) ? null : botAI.getNextRequest(mover, gridState, layout, rand);
    }

    @Override
    public String getTag() {
        return TAG;
    }

    public static BotComponent getBotComponent(EcsEntity e) {
        BotComponent m = (BotComponent) e.getComponent(BotComponent.TAG);
        return m;
    }

    public static BotComponent buildFromGridDefinition(StartPosition startPosition, BotAI botAI) {
        BotComponent bc = new BotComponent(botAI);
        bc.monster = startPosition.isMonster;
        return bc;
    }

    public boolean isMonster() {
        return monster;
    }

    public void setBotAI(BotAI botAI) {
        this.botAI = botAI;
    }
}
