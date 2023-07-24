package de.yard.threed.maze.testutils;

import de.yard.threed.core.Point;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.engine.util.IntProvider;
import de.yard.threed.maze.BotAI;
import de.yard.threed.maze.GridMovement;
import de.yard.threed.maze.GridMover;
import de.yard.threed.maze.GridState;
import de.yard.threed.maze.MazeLayout;
import de.yard.threed.maze.MazeRequestRegistry;
import de.yard.threed.maze.MazeUtils;

public class TestingBotAI implements BotAI {

    public Request nextRequest = null;

    @Override
    public boolean isReadyToAct() {
        return nextRequest != null;
    }

    @Override
    public Request getNextRequest(GridMover mover, GridState gridState, MazeLayout layout, IntProvider rand) {

        Request r = nextRequest;
        nextRequest = null;
        return r;
    }

    @Override
    public String getName() {
        return "TestingBotAI";
    }
}
