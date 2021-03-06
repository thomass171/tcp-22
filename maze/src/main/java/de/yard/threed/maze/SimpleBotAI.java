package de.yard.threed.maze;

import de.yard.threed.core.Point;
import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.engine.util.IntProvider;

public class SimpleBotAI implements BotAI {

    /**
     * Priority of actions:
     * 1) fire at near player ahead (but not from home field)
     * 2) forward if possible
     * 3) turn
     */
    @Override
    public Request getNextRequest(GridMover mover, GridState gridState, MazeLayout layout, IntProvider rand) {

        GridMover nextPlayer;
        if (!mover.getTeam().homeFields.contains(mover.getLocation()) && (nextPlayer = gridState.findNextPlayer(mover.getLocation(), mover.getOrientation(), layout)) != null) {
            if (Point.getDistance(mover.getLocation(), nextPlayer.getLocation()) < 3) {
                return new Request(BulletSystem.TRIGGER_REQUEST_FIRE);
            }
        }

        if (gridState.canWalk(mover.getLocation(), GridMovement.Forward, mover.getOrientation(), mover.getTeam(), layout)) {
            return new Request(RequestRegistry.TRIGGER_REQUEST_FORWARD);
        }
        // just turn left or right
        return new Request((rand.nextInt() % 2 == 0) ? RequestRegistry.TRIGGER_REQUEST_TURNLEFT : RequestRegistry.TRIGGER_REQUEST_TURNRIGHT);
    }
}
