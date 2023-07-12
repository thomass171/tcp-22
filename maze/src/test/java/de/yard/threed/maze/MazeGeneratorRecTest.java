package de.yard.threed.maze;

import de.yard.threed.core.Point;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.SimpleEventBusForTesting;
import de.yard.threed.engine.util.DeterministicIntProvider;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.javacommon.SimpleHeadlessPlatformFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MazeGeneratorRecTest {

    static Platform platform = EngineTestFactory.initPlatformForTest(new String[]{"engine", "maze"}, new SimpleHeadlessPlatformFactory(new SimpleEventBusForTesting()));

    @Test
    public void test80x25() throws InvalidMazeException {

        MazeGeneratorRect layoutRect = new MazeGeneratorRect(80, 25, new DeterministicIntProvider(new int[]{4, 1, 5, 276, 6}));

        layoutRect.addRoom(5, 9);
        layoutRect.addRoom(7, 4);
        layoutRect.addCorridor(0, 1);
        dump(layoutRect);
    }

    @Test
    public void testCorridor() throws InvalidMazeException {

        MazeGeneratorRect layoutRect = MazeGeneratorRect.buildSampleV1();

        assertEquals(15, layoutRect.getCorridor(0).size());
        assertEquals(16/*14*/, layoutRect.getCorridor(1).size());
        assertEquals(5/*5*/, layoutRect.getCorridor(2).size());
        assertEquals(2, layoutRect.getCorridor(3).size());
        assertEquals(20, layoutRect.getCorridor(4).size());
        assertEquals(34, layoutRect.getCorridor(5).size());

        dump(layoutRect);

        // now field allowed on outside wall
        for (Point p : layoutRect.fields) {
            if (p.getX() == 0) {
                fail("field on wall " + p);
            }
            if (p.getY() == 0) {
                fail("field on wall " + p);
            }
            //TODO upper and right
        }
    }

    private void dump(MazeGeneratorRect layoutRect) {
        Log log = platform.getLog(MazeGeneratorRecTest.class);
        log.debug("--");
        for (int y = layoutRect.height - 1; y >= 0; y--) {
            String l = "";
            for (int x = 0; x < layoutRect.width; x++) {
                l += layoutRect.fields.contains(new Point(x, y)) ? " " : "#";
            }
            log.debug(l);
        }
        log.debug("--");
    }

}
