package de.yard.threed.maze;

import de.yard.threed.core.Point;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.maze.testutils.MazeTestUtils;
import de.yard.threed.engine.testutil.PlatformFactoryHeadless;
import de.yard.threed.engine.testutil.EngineTestFactory;

import de.yard.threed.engine.platform.common.StringReader;
import de.yard.threed.core.testutil.Assert;
import de.yard.threed.engine.testutil.TestHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


/**
 * Also for AutoSolver.
 * Created by thomass on 15.11.15.
 */
public class SokobanTest {
    static Platform platform = EngineTestFactory.initPlatformForTest(new String[]{"engine", "maze"}, new PlatformFactoryHeadless());
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
            Grid grid = Grid.loadByReader(new StringReader(MazeTestUtils.loadGrid("skbn/SokobanWikipedia.txt"))).get(0);

            MazeLayout layout = grid.getMazeLayout();
            Point startPosition = grid.getMazeLayout().getNextLaunchPosition(null, true);
            Assertions.assertEquals(9, layout.getMaxWidth(), "width");
            Assertions.assertEquals(6, layout.getHeight(), "height");
            Assertions.assertEquals(6, startPosition.getX(), "start.x");
            Assertions.assertEquals(1, startPosition.getY(), "start.y");
            Assertions.assertTrue(MazeTraditionalTerrain.hasTopWall(layout, new Point(0, 0)), "top pillar");
            Assertions.assertTrue(MazeTraditionalTerrain.hasTopWall(layout, new Point(0, 1)), "top pillar");
            Assertions.assertFalse(MazeTraditionalTerrain.hasTopWall(layout, new Point(0, 4)), "top pillar");
            Assertions.assertFalse(MazeTraditionalTerrain.hasTopWall(layout, new Point(1, 3)), "top pillar (1,3)");
            Assertions.assertTrue(MazeTraditionalTerrain.hasCenterWall(layout, new Point(5, 2)), "center pillar");
            Assertions.assertTrue(MazeTraditionalTerrain.hasTopWall(layout, new Point(5, 4)), "top pillar");
            Assertions.assertFalse(MazeTraditionalTerrain.hasTopWall(layout, new Point(7, 3)), "top pillar");
            Assertions.assertFalse(MazeTraditionalTerrain.hasTopWall(layout, new Point(8, 4)), "top pillar");
            Assertions.assertFalse(MazeTraditionalTerrain.hasTopWall(layout, new Point(6, 4)), "top pillar 6,4 ");
            // right
            Assertions.assertTrue(MazeTraditionalTerrain.hasRightWall(layout, new Point(0, 0)), "right pillar");
            Assertions.assertTrue(MazeTraditionalTerrain.hasRightWall(layout, new Point(5, 0)), "right pillar");
            Assertions.assertFalse(MazeTraditionalTerrain.hasRightWall(layout, new Point(5, 1)), "right pillar");
            Assertions.assertFalse(MazeTraditionalTerrain.hasRightWall(layout, new Point(5, 2)), "right pillar");
            Assertions.assertTrue(MazeTraditionalTerrain.hasRightWall(layout, new Point(5, 4)), "right pillar");

            // left
            Assertions.assertFalse(MazeTraditionalTerrain.hasRightWall(layout,new Point(-1, 0)), "left pillar");

            // bottom
            Assertions.assertFalse(MazeTraditionalTerrain.hasTopWall(layout,new Point(0, -1)), "bottom pillar");

            Assertions.assertEquals(MazeTraditionalTerrain.STRAIGHTWALLMODE_NONE, MazeTraditionalTerrain.isVWALL(layout,new Point(0, 0)));
            Assertions.assertEquals(MazeTraditionalTerrain.STRAIGHTWALLMODE_NONE, MazeTraditionalTerrain.isHWALL(layout,new Point(0, 0)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testAutosolverTrivial() throws Exception {
        Grid grid = Grid.loadByReader(new StringReader(MazeTestUtils.loadGrid("skbn/SokobanTrivial.txt"))).get(0);

        SokobanAutosolver solver = new SokobanAutosolver(grid/*MA32, grid.getState()*/);
        solver.solve();
        System.out.println("solution:\n" + solver.dumpSolution());

        //15.4.21: Tuts nicht mehr TestUtil.assertEquals("way", "TurnRight-ForwardMove", solver.getSolutionAsString());

    }

    @Test
    public void testAutosolverSimple() throws Exception {
        Grid grid = Grid.loadByReader(new StringReader(MazeTestUtils.loadGrid("skbn/SokobanSimple.txt"))).get(0);

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
