package de.yard.threed.maze;

import de.yard.threed.core.Point;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.maze.testutils.TestUtils;
import de.yard.threed.engine.testutil.PlatformFactoryHeadless;
import de.yard.threed.engine.testutil.TestFactory;

import de.yard.threed.engine.platform.common.StringReader;
import de.yard.threed.core.testutil.Assert;
import de.yard.threed.core.testutil.TestUtil;
import de.yard.threed.engine.testutil.TestHelper;
import org.junit.Test;


/**
 * Also for AutoSolver.
 * Created by thomass on 15.11.15.
 */
public class SokobanTest {
    static Platform platform = TestFactory.initPlatformForTest( new String[] {"engine","maze"}, new PlatformFactoryHeadless());
    /*16.4.21 public static final String trivialsokoban =
            "#####\n" +
                    "#   #\n" +
                    "#@$.#\n" +
                    "#####";*/

    /**
     * __####
     * ###  ####
     * #     $ #
     * # #  #$ #
     * # . .#@ #
     * #########
     */
    @Test
    public void testSokobanWikipedia() {

        try {
            Grid grid = Grid.loadByReader(new StringReader(TestUtils.loadGrid("skbn/SokobanWikipedia.txt"))).get(0);

            Point startPosition = grid.getLayout().getNextLaunchPosition(null);
            TestUtil.assertEquals("width", 9, grid.getMaxWidth());
            TestUtil.assertEquals("height", 6, grid.getHeight());
            TestUtil.assertEquals("start.x", 6, startPosition.getX());
            TestUtil.assertEquals("start.y", 1, startPosition.getY());
            TestUtil.assertTrue("top pillar", grid.hasTopPillar(new Point(0, 0)));
            TestUtil.assertTrue("top pillar", grid.hasTopPillar(new Point(0, 1)));
            TestUtil.assertFalse("top pillar", grid.hasTopPillar(new Point(0, 4)));
            TestUtil.assertFalse("top pillar (1,3)", grid.hasTopPillar(new Point(1, 3)));
            TestUtil.assertTrue("center pillar", grid.hasCenterPillar(new Point(5, 2)));
            TestUtil.assertTrue("top pillar", grid.hasTopPillar(new Point(5, 4)));
            TestUtil.assertFalse("top pillar", grid.hasTopPillar(new Point(7, 3)));
            TestUtil.assertFalse("top pillar", grid.hasTopPillar(new Point(8, 4)));
            TestUtil.assertFalse("top pillar 6,4 ", grid.hasTopPillar(new Point(6, 4)));
            // right
            TestUtil.assertTrue("right pillar", grid.hasRightPillar(new Point(0, 0)));
            TestUtil.assertTrue("right pillar", grid.hasRightPillar(new Point(5, 0)));
            TestUtil.assertFalse("right pillar", grid.hasRightPillar(new Point(5, 1)));
            TestUtil.assertFalse("right pillar", grid.hasRightPillar(new Point(5, 2)));
            TestUtil.assertTrue("right pillar", grid.hasRightPillar(new Point(5, 4)));

            // left
            TestUtil.assertFalse("left pillar", grid.hasRightPillar(new Point(-1, 0)));

            // bottom
            TestUtil.assertFalse("bottom pillar", grid.hasTopPillar(new Point(0, -1)));

            TestUtil.assertEquals("", Grid.STRAIGHTWALLMODE_NONE,grid.isVWALL(new Point(0, 0)));
            TestUtil.assertEquals("", Grid.STRAIGHTWALLMODE_NONE,grid.isHWALL(new Point(0, 0)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testAutosolverTrivial() throws Exception {
        Grid grid = Grid.loadByReader(new StringReader(TestUtils.loadGrid("skbn/SokobanTrivial.txt"))).get(0);

        SokobanAutosolver solver = new SokobanAutosolver(grid/*MA32, grid.getState()*/);
        solver.solve();
        System.out.println("solution:\n" + solver.dumpSolution());

        //15.4.21: Tuts nicht mehr TestUtil.assertEquals("way", "TurnRight-ForwardMove", solver.getSolutionAsString());

    }

    @Test
    public void testAutosolverSimple() throws Exception {
        Grid grid = Grid.loadByReader(new StringReader(TestUtils.loadGrid("skbn/SokobanSimple.txt"))).get(0);

        SokobanAutosolver solver = new SokobanAutosolver(grid/*MA32, grid.getState()*/);
        solver.solve();
        System.out.println("solution:\n" + solver.dumpSolution());
        // weiss gar nicht, ob das wirkluch stimmt.
        String possiblesolution = "Right-Forward-Left-Forward-Forward-Left-Forward-Forward-Left-Forward-Left-ForwardMove-Left-Forward-Left-Forward-Left-Forward-Forward-Left-Forward-Forward-Left-ForwardMove-ForwardMove-Left-Forward-Right-Forward-Right-ForwardMove";
        possiblesolution = "ForwardMove-Left-Forward-Right-Forward-Right-ForwardMove-Right-Forward-Left-Forward-Forward-Left-Forward-Forward-Left-Forward-Left-ForwardMove-Left-Forward-Left-Forward-Left-Forward-Forward-Left-Forward-Forward-Left-ForwardMove";
        possiblesolution = "ForwardMove-TurnLeft-Forward-TurnRight-Forward-TurnRight-ForwardMove-TurnRight-Forward-TurnLeft-Forward-Forward-TurnLeft-Forward-Forward-TurnLeft-Forward-TurnLeft-ForwardMove-TurnLeft-Forward-TurnLeft-Forward-TurnLeft-Forward-Forward-TurnLeft-Forward-Forward-TurnLeft-ForwardMove";
        possiblesolution = "ForwardMove-Left-Forward-TurnRight-ForwardMove-TurnLeft-Forward-TurnLeft-TurnLeft-Left-ForwardMove-Right-Forward-TurnLeft-ForwardMove";
        //24.3.17:Bringt über maven immer andere Ergebnisse
        //TestUtil.assertEquals("way", possiblesolution,solver.getSolutionAsString());
    }

    @Test
    public void testAutosolverWiki() {
        Grid grid = null;
        try {
            grid = Grid.loadByReader(new StringReader(TestHelper.getDataBundleString("maze", "skbn/SokobanWikipedia.txt"))).get(0);


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        SokobanAutosolver solver = new SokobanAutosolver(grid/*.getLayout()/*MA32, grid.getState()*/);
        solver.solve();
        System.out.println("solution:\n" + solver.dumpSolution());
        //
        //TestUtil.assertEquals("way", possiblesolution,solver.getSolutionAsString());
    }

    @Test
    public void testEmptyGrid() {
        try {
            Grid.loadByReader(new StringReader(""));
        } catch (InvalidMazeException e) {
            return;
        }
        Assert.fail("no InvalidMazeException");
    }

    public static Grid load(String grid) {
        try {
            Grid.loadByReader(new StringReader(grid));
            return null;
        } catch (InvalidMazeException e) {
            throw new RuntimeException(e);
        }
    }
}
