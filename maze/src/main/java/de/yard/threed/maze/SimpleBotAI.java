package de.yard.threed.maze;

import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.engine.util.IntProvider;

public class SimpleBotAI implements BotAI {

    @Override
    public Request getNextRequest(GridMover mover, GridState gridState, MazeLayout layout, IntProvider rand) {
        if (gridState.canWalk(mover.getLocation(), GridMovement.Forward, mover.getOrientation(), mover.getTeam(), layout)) {
            return new Request(RequestRegistry.TRIGGER_REQUEST_FORWARD);
        }
        // just turn left or right
        return new Request((rand.nextInt() % 2 == 0) ? RequestRegistry.TRIGGER_REQUEST_TURNLEFT : RequestRegistry.TRIGGER_REQUEST_TURNRIGHT);
    }
}
