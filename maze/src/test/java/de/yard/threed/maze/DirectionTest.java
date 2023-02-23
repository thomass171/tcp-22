package de.yard.threed.maze;

import de.yard.threed.core.Point;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.testutil.PlatformFactoryHeadless;
import de.yard.threed.engine.testutil.TestFactory;
import de.yard.threed.maze.testutils.MazeTestUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;


/**
 *
 */
public class DirectionTest {
    static Platform platform = TestFactory.initPlatformForTest(new String[]{"engine", "maze"}, new PlatformFactoryHeadless());

    @Test
    public void testDirection() {

        Point p = new Point(2, 2);
        MazeTestUtils.assertDirection(Direction.N, Direction.of(p, new Point(2, 3)));
        MazeTestUtils.assertDirection(Direction.E, Direction.of(p, new Point(3, 2)));
        MazeTestUtils.assertDirection(Direction.S, Direction.of(p, new Point(2, 0)));
        MazeTestUtils.assertDirection(Direction.W, Direction.of(p, new Point(-2, 2)));
        assertNull(Direction.of(p, new Point(-2, -2)));
    }
}
