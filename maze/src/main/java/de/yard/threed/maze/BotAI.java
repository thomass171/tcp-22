package de.yard.threed.maze;

import de.yard.threed.engine.platform.common.Request;

public interface BotAI {

    /**
     * For simplicity pass far too much data, even this is not quite 'fair'
     */
    Request getNextRequest(GridMover mover, GridState gridState, MazeLayout layout);
}
