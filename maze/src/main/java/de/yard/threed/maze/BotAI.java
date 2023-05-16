package de.yard.threed.maze;

import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.engine.util.IntProvider;

public interface BotAI {

    boolean isReadyToAct();

    /**
     * For simplicity pass far too much data, even this is not quite 'fair'.
     * Returns null if there is no next request currently.
     */
    Request getNextRequest(GridMover mover, GridState gridState, MazeLayout layout, IntProvider rand);

    String getName();
}
