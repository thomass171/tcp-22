package de.yard.threed.maze;

import de.yard.threed.core.Point;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.testutil.PlatformFactoryHeadless;
import de.yard.threed.engine.testutil.TestFactory;

import de.yard.threed.core.testutil.TestUtil;
import org.junit.Test;


/**
 * Created by thomass on 07.10.15.
 */
public class MazeDimensionsTest {
    static Platform platform = TestFactory.initPlatformForTest( new String[] {"engine","maze"}, new PlatformFactoryHeadless());

    @Test
    public void testMazeDimensions() {
        Vector3 v = MazeDimensions.getWorldElementCoordinates(0, 0);
        Point p;
        /*p = MazeDimensions.getCoordinatesOfElement(v);
        TestUtil.assertPoint(new Point(0, 0), p);*/

        TestUtil.assertVector3("0,0", new Vector3(0, 0, 0), v);
        v = MazeDimensions.getTopOffset();
        TestUtil.assertVector3("topoffset 0,0", new Vector3(0, 0, -MazeDimensions.GRIDSEGMENTSIZE / 2), v);
        v = MazeDimensions.getRightOffset();
        TestUtil.assertVector3("rightoffset 0,0", new Vector3(MazeDimensions.GRIDSEGMENTSIZE / 2, 0, 0), v);
        // Das Center von grid1
        v = MazeDimensions.getWorldElementCoordinates(5, 3);
        TestUtil.assertVector3("center ", new Vector3(5f * MazeDimensions.GRIDSEGMENTSIZE, 0, -3f * MazeDimensions.GRIDSEGMENTSIZE), v);
        /*p = MazeDimensions.getCoordinatesOfElement(v);
        TestUtil.assertPoint(new Point(5, 3), p);*/

        v = MazeDimensions.getCenterCoordinates(2, 3);
        TestUtil.assertVector3("center grid1", new Vector3((2f - 1f) * MazeDimensions.GRIDSEGMENTSIZE / 2, 0, -(3f - 1f) * MazeDimensions.GRIDSEGMENTSIZE / 2), v);

        v = MazeDimensions.getCenterCoordinates(11, 7);
        TestUtil.assertVector3("center grid1", new Vector3((11f - 1f) * MazeDimensions.GRIDSEGMENTSIZE / 2, 0, -(7f - 1f) * MazeDimensions.GRIDSEGMENTSIZE / 2), v);

    }
}
