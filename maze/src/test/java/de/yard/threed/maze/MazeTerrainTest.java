package de.yard.threed.maze;

import de.yard.threed.core.Point;
import de.yard.threed.core.Util;
import de.yard.threed.core.Vector2;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.SimpleEventBusForTesting;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.Shape;
import de.yard.threed.engine.platform.common.StringReader;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.engine.testutil.PlatformFactoryHeadless;
import de.yard.threed.engine.testutil.TestHelper;
import de.yard.threed.javacommon.SimpleHeadlessPlatformFactory;
import de.yard.threed.maze.testutils.MazeTestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static de.yard.threed.maze.MazeTheme.THEME_TRADITIONAL;
import static org.junit.jupiter.api.Assertions.*;


/**
 * Extracted from GridTest on 18.7.23.
 * Tests without ECS (See MazeTest for tests with ECS) and without visuals.
 * Just for testing the terrain (no maze logic).
 * <p>
 * Created by thomass on 15.07.15.
 */
public class MazeTerrainTest {
    static Platform platform = EngineTestFactory.initPlatformForTest(new String[]{"engine", "maze"}, new SimpleHeadlessPlatformFactory(new SimpleEventBusForTesting()));
    MazeTheme mazeTheme;

    //no good idea to define statics here because it restarts platform init

    @BeforeEach
    public void setup() {
        mazeTheme = MazeTheme.buildFromIdentifier(THEME_TRADITIONAL);
    }

    /**
     * +---------+
     * |         |
     * |O -| | - |
     * |   | |   |
     * | --+ +-+ |
     * |    x    |
     * +---------+
     */
    @Test
    public void testGrid1() throws Exception {

        Grid grid = Grid.loadByReader(new StringReader(TestHelper.getDataBundleString("maze", "maze/grid1.txt"))).get(0);

        MazeLayout layout = grid.getMazeLayout();
        Point startPosition = grid.getMazeLayout().getNextLaunchPosition(null);
        Assertions.assertEquals(11, layout.getMaxWidth(), "width");
        Assertions.assertEquals(7, layout.getHeight(), "height");
        Assertions.assertEquals(5, startPosition.getX(), "start.x");
        Assertions.assertEquals(1, startPosition.getY(), "start.y");
        Assertions.assertTrue(MazeTraditionalTerrain.hasTopWall(layout, new Point(0, 0)), "top pillar");
        Assertions.assertTrue(MazeTraditionalTerrain.hasTopWall(layout, new Point(0, 1)), "top pillar");
        Assertions.assertFalse(MazeTraditionalTerrain.hasTopWall(layout, new Point(1, 0)), "top pillar");
        Assertions.assertFalse(MazeTraditionalTerrain.hasTopWall(layout, new Point(1, 1)), "top pillar");
        Assertions.assertTrue(MazeTraditionalTerrain.hasRightWall(layout, new Point(0, 0)), "right pillar");
        Assertions.assertFalse(MazeTraditionalTerrain.hasRightWall(layout, new Point(0, 1)), "right pillar");
        Assertions.assertTrue(MazeTraditionalTerrain.hasRightWall(layout, new Point(1, 0)), "right pillar");
        Assertions.assertFalse(MazeTraditionalTerrain.hasRightWall(layout, new Point(1, 1)), "right pillar");


    }

    @Test
    public void testWallShape() {
        boolean istop = true;
        boolean iscorner = true;
        float distance = 0.8f;
        float d2 = distance / 2;
        MazeTheme st = MazeTheme.buildFromIdentifier(null);
        MazeTraditionalModelFactory mf = (MazeTraditionalModelFactory) st.getMazeModelFactory();
        Shape shape = mf.buildWallShape(6, istop, false, distance);
        List<Vector2> points = shape.getPoints();
        TestUtils.assertVector2(new Vector2(-3, 0), points.get(0), "p0");
        TestUtils.assertVector2(new Vector2(0, 0), points.get(1), "p1");
        TestUtils.assertVector2(new Vector2(3, 0), points.get(2), "p2");

        shape = mf.buildWallShape(6, false, false, distance);
        points = shape.getPoints();
        TestUtils.assertVector2(new Vector2(-3, 0), points.get(0), "p0");
        TestUtils.assertVector2(new Vector2(0, 0), points.get(1), "p1");
        TestUtils.assertVector2(new Vector2(3, 0), points.get(2), "p2");

        shape = mf.buildWallShape(6, istop, iscorner, distance);
        points = shape.getPoints();
        TestUtils.assertVector2(new Vector2(-3 - d2, 0), points.get(0), "p0");
        TestUtils.assertVector2(new Vector2(0, 0), points.get(1), "p1");
        TestUtils.assertVector2(new Vector2(0, -3 - d2), points.get(2), "p2");

        shape = mf.buildWallShape(6, false, iscorner, distance);
        points = shape.getPoints();
        TestUtils.assertVector2(new Vector2(0, 3 - d2), points.get(0), "p0");
        TestUtils.assertVector2(new Vector2(0, 0), points.get(1), "p1");
        TestUtils.assertVector2(new Vector2(3 - d2, 0), points.get(2), "p2");

    }

    /**
     * __####
     * ###  ####
     * #     $ #
     * # #  #$ #
     * # . .#@ #
     * #########
     *
     * @throws Exception
     */
    @Test
    public void testTerrainWikipedia() throws Exception {

        Grid grid = GridTest.loadGridAndTerrain("skbn/SokobanWikipedia.txt", 1);
        MazeLayout layout = grid.getMazeLayout();
        MazeTraditionalTerrain terrain = (MazeTraditionalTerrain) mazeTheme.buildTerrain(layout);
        terrain.visualizeGrid();
        Assertions.assertEquals(layout.getMaxWidth() * layout.getHeight(), terrain.getTiles().values().size(), "tiles");
    }

    /**
     * ###############
     * #     @       #
     * #  #        # #
     * #  ## D # D## #
     * #  #   ###  # #
     * #     @ #     #
     * #    #D ####  #
     * #   ###   #D  #
     * #             #
     * ###############
     */
    @Test
    public void testArea15x10() throws Exception {

        Grid grid = GridTest.loadGridAndTerrain("maze/Area15x10.txt", 2);
        MazeLayout layout = grid.getMazeLayout();
        MazeTraditionalTerrain terrain = (MazeTraditionalTerrain) mazeTheme.buildTerrain(layout);
        terrain.visualizeGrid();

        SceneNode[] pillar = terrain.getPillar(new Point(5, 3));
        Assertions.assertNull(pillar[0], "top of 5,3");
        Assertions.assertNull(pillar[1], "right of 5,3");
        Assertions.assertNotNull(pillar[2], "center of 5,3");

        pillar = terrain.getPillar(new Point(3, 2));
        Assertions.assertNull(pillar[0], "top of 3,2");
        Assertions.assertNull(pillar[1], "right of 3,2");
        Assertions.assertNull(pillar[2], "center of 3,2");

        pillar = terrain.getPillar(new Point(4, 2));
        Assertions.assertNull(pillar[0], "top of 4,2");
        Assertions.assertNotNull(pillar[1], "right of 4,2");
        Assertions.assertNotNull(pillar[2], "center of 4,2");

        pillar = terrain.getPillar(new Point(5, 2));
        Assertions.assertNotNull(pillar[0], "top of 5,2");
        Assertions.assertNotNull(pillar[1], "right of 5,2");
        Assertions.assertNull(pillar[2], "center of 5,2");

        pillar = terrain.getPillar(new Point(6, 2));
        Assertions.assertNull(pillar[0], "top of 6,2");
        Assertions.assertNull(pillar[1], "right of 6,2");
        Assertions.assertNotNull(pillar[2], "center of 6,2");


    }

    @Test
    public void testTerrain() {
        MazeTraditionalModelFactory mf = (MazeTraditionalModelFactory) mazeTheme.getMazeModelFactory();
        MazeTraditionalTerrain terrain = new MazeTraditionalTerrain(new MazeLayout(new ArrayList<Point>(), new ArrayList<Point>(), null, 3, 6, new ArrayList<Point>()),mf);
        terrain.addGridElement(mf.buildWall(1, MazeTraditionalTerrain.STRAIGHTWALLMODE_FULL), 0, 0, 0);
        SceneNode wall = terrain.getNode().getTransform().getChild(0).getSceneNode();
        TestUtils.assertVector3(new Vector3(-MazeDimensions.GRIDSEGMENTSIZE, 0, 2.5f * MazeDimensions.GRIDSEGMENTSIZE), wall.getTransform().getPosition(), "wallpos");
    }
}
