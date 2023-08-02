package de.yard.threed.maze;

import de.yard.threed.core.Point;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.engine.util.DeterministicIntProvider;
import de.yard.threed.engine.testutil.PlatformFactoryHeadless;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.engine.util.IntProvider;
import de.yard.threed.maze.testutils.ExpectedGridData;
import de.yard.threed.maze.testutils.MazeTestUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


/**
 *
 */
public class SimpleBotAITest {
    static Platform platform = EngineTestFactory.initPlatformForTest(new String[]{"engine", "maze"}, new PlatformFactoryHeadless());

    @Test
    public void testNotFiringSameTeamAndNotSolving() throws Exception {

        Grid grid = GridTest.loadGridAndTerrainFromString("##########\n" +
                "#  MM   .#\n" +
                "#    @   #\n" +
                "##########", 2, null);

        ExpectedGridData expectedGridData = new ExpectedGridData(
                new GridTeam[]{
                        new GridTeam(new StartPosition[]{new StartPosition(5, 1, GridOrientation.N)}, false),
                        new GridTeam(new StartPosition[]{new StartPosition(3, 2, GridOrientation.S), new StartPosition(4, 2, GridOrientation.E)}, true),
                },
                new Point[]{},
                new Point[]{}
        );

        GridState gridState = GridTest.initAndValidateGrid(grid, expectedGridData);
        GridMover player = gridState.players.get(0);
        GridMover leftBot = gridState.players.get(1);
        GridMover rightBot = gridState.players.get(2);
        TestUtils.assertPoint(new Point(3, 2), leftBot.getLocation(), "left bot location");
        assertEquals("E", leftBot.getOrientation().getDirectionCode());
        TestUtils.assertPoint(new Point(4, 2), rightBot.getLocation(), "right bot location");

        // cannot fire from home, so leave home. No need to update gridstate in between
        MazeTestUtils.move(rightBot, GridMovement.Forward, gridState, grid.getMazeLayout(), new Point(5, 2));
        MazeTestUtils.move(rightBot, GridMovement.Forward, gridState, grid.getMazeLayout(), new Point(6, 2));
        MazeTestUtils.move(leftBot, GridMovement.Forward, gridState, grid.getMazeLayout(), new Point(4, 2));
        MazeTestUtils.move(leftBot, GridMovement.Forward, gridState, grid.getMazeLayout(), new Point(5, 2));

        SimpleBotAI botAI = new SimpleBotAI();
        IntProvider intProvider = new DeterministicIntProvider(new int[]{1, 1});
        Request request = botAI.getNextRequest(leftBot, gridState, grid.getMazeLayout(), intProvider);
        // should not fire at own team member but turn
        assertEquals(MazeRequestRegistry.TRIGGER_REQUEST_TURNRIGHT.getLabel(), request.getType().getLabel());
        MazeTestUtils.rotatePlayer(leftBot, false, new Point(5, 2));

        request = botAI.getNextRequest(leftBot, gridState, grid.getMazeLayout(), intProvider);
        // should now fire player
        assertEquals(MazeRequestRegistry.TRIGGER_REQUEST_FIRE.getLabel(), request.getType().getLabel());

        // right bot should not solve
        MazeTestUtils.move(rightBot, GridMovement.Forward, gridState, grid.getMazeLayout(), new Point(7, 2));
        request = botAI.getNextRequest(rightBot, gridState, grid.getMazeLayout(), intProvider);
        assertEquals(MazeRequestRegistry.TRIGGER_REQUEST_TURNRIGHT.getLabel(), request.getType().getLabel());

    }
}
