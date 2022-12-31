package de.yard.threed.maze;

import de.yard.threed.core.Point;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.TestUtil;
import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.engine.testutil.DeterministicIntProvider;
import de.yard.threed.engine.testutil.PlatformFactoryHeadless;
import de.yard.threed.engine.testutil.TestFactory;
import de.yard.threed.engine.util.IntProvider;
import de.yard.threed.maze.testutils.TestUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


/**
 *
 */
public class SimpleBotAITest {
    static Platform platform = TestFactory.initPlatformForTest(new String[]{"engine", "maze"}, new PlatformFactoryHeadless());

    @Test
    public void testNotFiringSameTeam() throws Exception {

        Grid grid = GridTest.loadGridAndTerrainFromString("##########\n" +
                "#  MM    #\n" +
                "#    @   #\n" +
                "##########", 2);

        GridState gridState = GridTest.initContent(grid, new Point[]{new Point(5, 1), new Point(3, 2), new Point(4, 2)}, new Point[]{}, new Point[]{});
        GridMover player = gridState.players.get(0);
        GridMover leftBot = gridState.players.get(1);
        GridMover rightBot = gridState.players.get(2);
        TestUtil.assertPoint(new Point(3, 2), leftBot.getLocation(), "left bot location");
        assertEquals("E", leftBot.getOrientation().getDirectionCode());
        TestUtil.assertPoint(new Point(4, 2), rightBot.getLocation(), "right bot location");

        // cannot fire from home, so leave home. No need to update gridstate in between
        TestUtils.move(rightBot, GridMovement.Forward, gridState, grid.getMazeLayout(), new Point(5, 2));
        TestUtils.move(rightBot, GridMovement.Forward, gridState, grid.getMazeLayout(), new Point(6, 2));
        TestUtils.move(leftBot, GridMovement.Forward, gridState, grid.getMazeLayout(), new Point(4, 2));
        TestUtils.move(leftBot, GridMovement.Forward, gridState, grid.getMazeLayout(), new Point(5, 2));

        SimpleBotAI botAI = new SimpleBotAI();
        IntProvider intProvider = new DeterministicIntProvider(new int[]{1,1});
        Request request = botAI.getNextRequest(leftBot, gridState, grid.getMazeLayout(), intProvider);
        // should not fire at own team member but turn
        assertEquals(RequestRegistry.TRIGGER_REQUEST_TURNRIGHT.getLabel(), request.getType().getLabel());
        TestUtils.rotatePlayer(leftBot, false,  new Point(5, 2));

        request = botAI.getNextRequest(leftBot, gridState, grid.getMazeLayout(), intProvider);
        // should now fire player
        assertEquals(BulletSystem.TRIGGER_REQUEST_FIRE.getLabel(), request.getType().getLabel());
    }
}
