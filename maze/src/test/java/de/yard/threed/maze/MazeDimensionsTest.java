package de.yard.threed.maze;

import de.yard.threed.core.Point;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.engine.testutil.PlatformFactoryHeadless;
import de.yard.threed.engine.testutil.EngineTestFactory;

import org.junit.jupiter.api.Test;


/**
 * Created by thomass on 07.10.15.
 */
public class MazeDimensionsTest {
    static Platform platform = EngineTestFactory.initPlatformForTest(new String[]{"engine", "maze"}, new PlatformFactoryHeadless());

    @Test
    public void testMazeDimensions() {
        Vector3 v = MazeDimensions.getWorldElementCoordinates(0, 0);
        Point p;
        /*p = MazeDimensions.getCoordinatesOfElement(v);
        TestUtil.assertPoint(new Point(0, 0), p);*/

        TestUtils.assertVector3(new Vector3(0, 0, 0), v, "0,0");
        v = MazeDimensions.getTopOffset();
        TestUtils.assertVector3(new Vector3(0, 0, -MazeDimensions.GRIDSEGMENTSIZE / 2), v, "topoffset 0,0");
        v = MazeDimensions.getRightOffset();
        TestUtils.assertVector3(new Vector3(MazeDimensions.GRIDSEGMENTSIZE / 2, 0, 0), v, "rightoffset 0,0");
        // Das Center von grid1
        v = MazeDimensions.getWorldElementCoordinates(5, 3);
        TestUtils.assertVector3(new Vector3(5f * MazeDimensions.GRIDSEGMENTSIZE, 0, -3f * MazeDimensions.GRIDSEGMENTSIZE), v, "center ");
        /*p = MazeDimensions.getCoordinatesOfElement(v);
        TestUtil.assertPoint(new Point(5, 3), p);*/

        v = MazeDimensions.getCenterCoordinates(2, 3);
        TestUtils.assertVector3(new Vector3((2f - 1f) * MazeDimensions.GRIDSEGMENTSIZE / 2, 0, -(3f - 1f) * MazeDimensions.GRIDSEGMENTSIZE / 2), v, "center grid1");

        v = MazeDimensions.getCenterCoordinates(11, 7);
        TestUtils.assertVector3(new Vector3((11f - 1f) * MazeDimensions.GRIDSEGMENTSIZE / 2, 0, -(7f - 1f) * MazeDimensions.GRIDSEGMENTSIZE / 2), v, "center grid1");

    }
}
