package de.yard.threed.maze;

import de.yard.threed.engine.platform.common.Request;

public class SimpleBotAI implements BotAI {
    @Override
    public Request getNextRequest(GridMover mover, GridState gridState, MazeLayout layout) {
        if (gridState.canWalk(mover.getLocation(), GridMovement.Forward, mover.getOrientation(), mover.getTeam(), layout)) {
            return new Request(RequestRegistry.TRIGGER_REQUEST_FORWARD);
        }
        return null;
    }
}
