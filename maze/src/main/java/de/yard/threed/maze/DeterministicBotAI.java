package de.yard.threed.maze;

import de.yard.threed.core.Point;
import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.engine.util.IntProvider;

import java.util.List;

/**
 * For testing
 */
public class DeterministicBotAI implements BotAI {

    Request[] requests;
    public int index;

    public DeterministicBotAI(Request[] requests){
        this.requests=requests;
        index =0;
    }

    @Override
    public Request getNextRequest(GridMover mover, GridState gridState, MazeLayout layout, IntProvider rand) {

        return requests[index++];
    }

    @Override
    public String getName() {
        return "DeterministicBotAI";
    }
}
