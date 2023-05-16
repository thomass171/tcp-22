package de.yard.threed.maze;

import de.yard.threed.core.Point;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.engine.util.IntProvider;

import java.util.List;

/**
 * For testing
 */
public class DeterministicBotAI implements BotAI {
    Log logger = Platform.getInstance().getLog(DeterministicBotAI.class);

    Request[] requests;
    public int index;

    public DeterministicBotAI(Request[] requests) {
        this.requests = requests;
        index = 0;
    }

    @Override
    public boolean isReadyToAct() {
        return true;
    }

    @Override
    public Request getNextRequest(GridMover mover, GridState gridState, MazeLayout layout, IntProvider rand) {
        if (index >= requests.length) {
            logger.debug("no more request available");
            return null;
        }
        return requests[index++];
    }

    @Override
    public String getName() {
        return "DeterministicBotAI";
    }
}
