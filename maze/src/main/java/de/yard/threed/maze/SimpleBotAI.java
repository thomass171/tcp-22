package de.yard.threed.maze;

import de.yard.threed.core.Point;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.engine.util.IntProvider;

public class SimpleBotAI implements BotAI {

    long lastActionAt = 0;
    static long WAIT_TIME = 1500;

    @Override
    public boolean isReadyToAct() {
        // TODO: move decision
        long current = Platform.getInstance().currentTimeMillis();
        if (current > lastActionAt + WAIT_TIME) {
            return true;
        }
        return false;
    }
    /**
     * Priority of actions:
     * 1) fire at near player ahead (but not from home field)
     * 2) forward if possible. Monster however shouldn't enter destination and solve mazes.
     * 3) turn
     */
    @Override
    public Request getNextRequest(GridMover mover, GridState gridState, MazeLayout layout, IntProvider rand) {

        lastActionAt = Platform.getInstance().currentTimeMillis();

        GridMover nextPlayer;
        if (!MazeUtils.getHomesOfTeam(layout, mover.getTeam()).contains(mover.getLocation()) && (nextPlayer = gridState.findNextPlayer(mover.getLocation(), mover.getOrientation(), layout)) != null) {
            // don't fire own team member.
            if (mover.getTeam() != nextPlayer.getTeam() && Point.getDistance(mover.getLocation(), nextPlayer.getLocation()) < 3) {
                // Different from VR player, for bots firing direction is the same as orientation.
                return BulletSystem.buildFireRequest(mover.getId(), mover.getOrientation().getDirectionForMovement(GridMovement.Forward));
            }
        }

        if (gridState.canWalk(mover.getLocation(), GridMovement.Forward, mover.getOrientation(), mover.getTeam(), layout)) {
            // don't solve
            if (!gridState.isDestinationAhead(mover.getLocation(),  mover.getOrientation(), layout)) {
                return new Request(RequestRegistry.TRIGGER_REQUEST_FORWARD);
            }
        }
        // just turn left or right
        return new Request((rand.nextInt() % 2 == 0) ? RequestRegistry.TRIGGER_REQUEST_TURNLEFT : RequestRegistry.TRIGGER_REQUEST_TURNRIGHT);
    }

    @Override
    public String getName() {
        return "SimpleBotAI";
    }
}
