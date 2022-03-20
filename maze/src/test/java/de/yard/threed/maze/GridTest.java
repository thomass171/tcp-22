package de.yard.threed.maze;

import de.yard.threed.core.Point;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.*;
import de.yard.threed.core.Vector2;
import de.yard.threed.core.Vector3;
import de.yard.threed.maze.testutils.TestUtils;
import de.yard.threed.engine.testutil.PlatformFactoryHeadless;
import de.yard.threed.engine.testutil.TestFactory;

import de.yard.threed.engine.platform.common.StringReader;
import de.yard.threed.core.testutil.TestUtil;
import de.yard.threed.engine.testutil.TestHelper;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


/**
 * Tests without ECS (See MazeTest for tests with ECS).
 * Auch fuer GridState, MazeTerrain, Direction und GridOrientation.
 * <p>
 * <p>
 * Created by thomass on 15.07.15.
 */
public class GridTest {
    static Platform platform = TestFactory.initPlatformForTest(new String[]{"engine", "maze"}, new PlatformFactoryHeadless());

    Grid grid;
    // not the interface. Really tests the traditional implementation.
    MazeTerrain terrain;

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

        Point startPosition = grid.getMazeLayout().getNextLaunchPosition(null);
        TestUtil.assertEquals("width", 11, grid.getMaxWidth());
        TestUtil.assertEquals("height", 7, grid.getHeight());
        TestUtil.assertEquals("start.x", 5, startPosition.getX());
        TestUtil.assertEquals("start.y", 1, startPosition.getY());
        TestUtil.assertTrue("top pillar", grid.hasTopPillar(new Point(0, 0)));
        TestUtil.assertTrue("top pillar", grid.hasTopPillar(new Point(0, 1)));
        TestUtil.assertFalse("top pillar", grid.hasTopPillar(new Point(1, 0)));
        TestUtil.assertFalse("top pillar", grid.hasTopPillar(new Point(1, 1)));
        TestUtil.assertTrue("right pillar", grid.hasRightPillar(new Point(0, 0)));
        TestUtil.assertFalse("right pillar", grid.hasRightPillar(new Point(0, 1)));
        TestUtil.assertTrue("right pillar", grid.hasRightPillar(new Point(1, 0)));
        TestUtil.assertFalse("right pillar", grid.hasRightPillar(new Point(1, 1)));

        TestUtil.assertEquals("start.y", 2, startPosition.add(Direction.N.getPoint()).getY());

        GridMover player = MazeFactory.buildMover(startPosition);

        TestUtil.assertPoint("current location", new Point(5, 1), player.getLocation());
        GridState state = new GridState(Arrays.asList(player), new ArrayList<GridMover>(), new ArrayList<GridItem>());
        List<GridMovement> moveOptions = player.getMoveOptions(state, grid.getMazeLayout());
        // 2 rotate, 3 simple move, 0 push, 8+4+8 relocates
        TestUtil.assertEquals("moveOptions", 2 + 3 + 0 + 8 + 4 + 8, moveOptions.size());
    }

    @Test
    public void testWallShape() {
        boolean istop = true;
        boolean iscorner = true;
        float distance = 0.8f;
        float d2 = distance / 2;
        MazeModelFactory mf = new MazeModelFactory();
        Shape shape = mf.buildWallShape(6, istop, false, distance);
        List<Vector2> points = shape.getPoints();
        TestUtil.assertVector2("p0", new Vector2(-3, 0), points.get(0));
        TestUtil.assertVector2("p1", new Vector2(0, 0), points.get(1));
        TestUtil.assertVector2("p2", new Vector2(3, 0), points.get(2));

        shape = mf.buildWallShape(6, false, false, distance);
        points = shape.getPoints();
        TestUtil.assertVector2("p0", new Vector2(-3, 0), points.get(0));
        TestUtil.assertVector2("p1", new Vector2(0, 0), points.get(1));
        TestUtil.assertVector2("p2", new Vector2(3, 0), points.get(2));

        shape = mf.buildWallShape(6, istop, iscorner, distance);
        points = shape.getPoints();
        TestUtil.assertVector2("p0", new Vector2(-3 - d2, 0), points.get(0));
        TestUtil.assertVector2("p1", new Vector2(0, 0), points.get(1));
        TestUtil.assertVector2("p2", new Vector2(0, -3 - d2), points.get(2));

        shape = mf.buildWallShape(6, false, iscorner, distance);
        points = shape.getPoints();
        TestUtil.assertVector2("p0", new Vector2(0, 3 - d2), points.get(0));
        TestUtil.assertVector2("p1", new Vector2(0, 0), points.get(1));
        TestUtil.assertVector2("p2", new Vector2(3 - d2, 0), points.get(2));

    }

    /**
     * #####
     * #  .#
     * # $ #
     * # @ #
     * #####
     */
    @Test
    public void testGridStateSimple() throws Exception {

        Grid grid = Grid.loadByReader(new StringReader(TestUtils.loadGrid("skbn/SokobanSimple.txt"))).get(0);

        Point startPosition = grid.getMazeLayout().getNextLaunchPosition(null);
        GridMover player = MazeFactory.buildMover(startPosition);
        List<GridMover> boxes = MazeFactory.buildMovers(grid.getBoxes());
        TestUtil.assertEquals("boxes", 1, boxes.size());
        GridMover box = boxes.get(0);

        TestUtil.assertPoint("current location", new Point(2, 1), player.getLocation());
        GridState state = new GridState(Arrays.asList(player), boxes, new ArrayList<GridItem>());
        List<GridMovement> moveOptions = player.getMoveOptions(state, grid.getMazeLayout());
        // 2 rotate, 2 simple move, 1 push, 6 relocates (not behind box)
        TestUtil.assertEquals("moveOptions", 2 + 2 + 1 + 6, moveOptions.size());

        TestUtils.rotatePlayer(player, true, new Point(2, 1));

        state = new GridState(Arrays.asList(player), boxes, new ArrayList<GridItem>());
        moveOptions = player.getMoveOptions(state, grid.getMazeLayout());
        // 2 rotate, 2 simple move, 0 push, 6 relocates (not behind box)
        TestUtil.assertEquals("moveOptions", 2 + 2 + 6, moveOptions.size());

        TestUtils.move(player, GridMovement.Forward, state, grid.getMazeLayout(), new Point(1, 1));
        TestUtils.rotatePlayer(player, false, new Point(1, 1));
        TestUtils.move(player, GridMovement.Forward, state, grid.getMazeLayout(), new Point(1, 2));
        TestUtils.rotatePlayer(player, false, new Point(1, 2));
        // forward should fail because of box
        TestUtils.move(player, GridMovement.Forward, state, grid.getMazeLayout(), new Point(1, 2));
        // but push should be possible
        TestUtils.move(player, GridMovement.ForwardMove, state, grid.getMazeLayout(), new Point(2, 2));
        TestUtils.move(player, GridMovement.Right, state, grid.getMazeLayout(), new Point(2, 1));
        TestUtils.move(player, GridMovement.Forward, state, grid.getMazeLayout(), new Point(3, 1));
        TestUtils.rotatePlayer(player, true, new Point(3, 1));

        TestUtil.assertFalse("solved", GridState.isSolved(boxes, grid.getMazeLayout()));
        TestUtils.move(player, GridMovement.ForwardMove, state, grid.getMazeLayout(), new Point(3, 2));
        TestUtil.assertTrue("solved", GridState.isSolved(boxes, grid.getMazeLayout()));

    }

    /**
     * __####
     * ###  ####
     * #     $ #
     * # #  #$ #
     * # . .#@ #
     * #########
     */
    @Test
    public void testGridStateWikipedia() throws Exception {

        Grid grid = Grid.loadByReader(new StringReader(TestHelper.getDataBundleString("maze", "skbn/SokobanWikipedia.txt"))).get(0);

        GridMover player = MazeFactory.buildMover(grid.getMazeLayout().getNextLaunchPosition(null));
        List<GridMover> players = Arrays.asList(player);
        List<GridMover> boxes = MazeFactory.buildMovers(grid.getBoxes());
        GridMover bottombox = boxes.get(0);
        GridMover topbox = boxes.get(1);

        GridState gridState = new GridState(players, boxes);
        TestUtil.assertPoint("start location", new Point(6, 1), player.getLocation());
        TestUtil.assertPoint("top box start location", new Point(6, 3), topbox.getLocation());
        TestUtil.assertPoint("bottom box start location", new Point(6, 2), bottombox.getLocation());
        TestUtil.assertPoint("box push location", new Point(6, 3), gridState.findNextBox(new Point(2, 3), GridOrientation.fromDirection('E'), grid.getMazeLayout()).getLocation());
        TestUtil.assertNotNull("can push box ", ((SimpleGridMover) player).canPushFrom(new Point(5, 3), GridOrientation.fromDirection('E'), gridState, grid.getMazeLayout()));

        // forward should fail because of unmovable box. Same for kick
        TestUtils.move(player, players, boxes, GridMovement.Forward, grid.getMazeLayout(), new Point(6, 1), null);
        TestUtils.move(player, players, boxes, GridMovement.Kick, grid.getMazeLayout(), new Point(6, 1), null);
        TestUtil.assertPoint("bottombox box location", new Point(6, 2), bottombox.getLocation());

        // also pull should fail
        TestUtils.move(player, players, boxes, GridMovement.Pull, grid.getMazeLayout(), new Point(6, 1), null);
        TestUtil.assertPoint("bottom box location", new Point(6, 2), bottombox.getLocation());

        TestUtils.move(player, players, boxes, GridMovement.Right, grid.getMazeLayout(), new Point(7, 1));
        TestUtils.move(player, players, boxes, GridMovement.Forward, grid.getMazeLayout(), new Point(7, 2));
        TestUtils.move(player, players, boxes, GridMovement.Forward, grid.getMazeLayout(), new Point(7, 3));

        // forward should fail because of wall
        TestUtils.move(player, players, boxes, GridMovement.Forward, grid.getMazeLayout(), new Point(7, 3));

        TestUtils.rotatePlayer(player, true, new Point(7, 3));

        // push box
        TestUtils.move(player, players, boxes, GridMovement.ForwardMove, grid.getMazeLayout(), new Point(6, 3), GridMovement.ForwardMove);
        TestUtil.assertPoint("top box location", new Point(5, 3), topbox.getLocation());
        TestUtil.assertPoint("player location", new Point(6, 3), player.getLocation());

        // kick (keeps position, only moves box)
        TestUtils.move(player, players, boxes, GridMovement.Kick, grid.getMazeLayout(), new Point(6, 3), GridMovement.Kick);
        TestUtil.assertPoint("top box location", new Point(4, 3), topbox.getLocation());
        TestUtil.assertPoint("player location", new Point(6, 3), player.getLocation());

        // kick again from same position
        TestUtils.move(player, players, boxes, GridMovement.Kick, grid.getMazeLayout(), new Point(6, 3), GridMovement.Kick);
        TestUtil.assertPoint("top box location", new Point(3, 3), topbox.getLocation());

        // pull it back
        TestUtils.move(player, players, boxes, GridMovement.Pull, grid.getMazeLayout(), new Point(6, 3), GridMovement.Pull);
        TestUtil.assertPoint("top box location", new Point(4, 3), topbox.getLocation());
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

        loadGridAndTerrain("skbn/SokobanWikipedia.txt");

        TestUtil.assertEquals("tiles", grid.getMaxWidth() * grid.getHeight(), terrain.getTiles().values().size());
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

        loadGridAndTerrain("maze/Area15x10.txt");

        SceneNode[] pillar = terrain.getPillar(new Point(5, 3));
        TestUtil.assertNull("top of 5,3", pillar[0]);
        TestUtil.assertNull("right of 5,3", pillar[1]);
        TestUtil.assertNotNull("center of 5,3", pillar[2]);

        pillar = terrain.getPillar(new Point(3, 2));
        TestUtil.assertNull("top of 3,2", pillar[0]);
        TestUtil.assertNull("right of 3,2", pillar[1]);
        TestUtil.assertNull("center of 3,2", pillar[2]);

        pillar = terrain.getPillar(new Point(4, 2));
        TestUtil.assertNull("top of 4,2", pillar[0]);
        TestUtil.assertNotNull("right of 4,2", pillar[1]);
        TestUtil.assertNotNull("center of 4,2", pillar[2]);

        pillar = terrain.getPillar(new Point(5, 2));
        TestUtil.assertNotNull("top of 5,2", pillar[0]);
        TestUtil.assertNotNull("right of 5,2", pillar[1]);
        TestUtil.assertNull("center of 5,2", pillar[2]);

        pillar = terrain.getPillar(new Point(6, 2));
        TestUtil.assertNull("top of 6,2", pillar[0]);
        TestUtil.assertNull("right of 6,2", pillar[1]);
        TestUtil.assertNotNull("center of 6,2", pillar[2]);

        GridMover player = MazeFactory.buildMover(grid.getMazeLayout().getNextLaunchPosition(null));
        List<GridMover> players = Arrays.asList(player);
        List<GridMover> boxes = MazeFactory.buildMovers(grid.getBoxes());
        TestUtil.assertEquals("boxes", 0, boxes.size());


        TestUtil.assertPoint("current location", new Point(6, 4), player.getLocation());

        TestUtils.move(player, players, boxes, GridMovement.Forward, grid.getMazeLayout(), new Point(6, 5));
        TestUtils.move(player, players, boxes, GridMovement.Forward, grid.getMazeLayout(), new Point(6, 6));

    }

    @Test
    public void testTerrain() {
        MazeModelFactory mf = new MazeModelFactory();
        MazeTerrain terrain = new MazeTerrain(3, 6);
        terrain.addGridElement(mf.buildWall(1, Grid.STRAIGHTWALLMODE_FULL), 0, 0, 0);
        SceneNode wall = terrain.getNode().getTransform().getChild(0).getSceneNode();
        TestUtil.assertVector3("wallpos", new Vector3(-MazeDimensions.GRIDSEGMENTSIZE, 0, 2.5f * MazeDimensions.GRIDSEGMENTSIZE), wall.getTransform().getPosition());
    }

    @Test
    public void testMultiple() throws Exception {

        List<Grid> grids = Grid.loadByReader(new StringReader(TestHelper.getDataBundleString("maze", "skbn/DavidJoffe.txt")));

        TestUtil.assertEquals("grids", 90, grids.size());

        Grid g = Grid.findByTitle(grids, "2");

        TestUtil.assertNotNull("", g);
    }

    /**
     * ##########
     * #   @    #
     * #   # #  #
     * #   # #  #
     * #    @   #
     * ##########
     */
    @Test
    public void testP_Simple() throws Exception {

        loadGridAndTerrain("maze/Maze-P-Simple.txt");

        List<Point> usedLaunchPositions = new ArrayList<Point>();
        Point startPosition = grid.getMazeLayout().getNextLaunchPosition(usedLaunchPositions);
        GridMover firstPlayer = MazeFactory.buildMover(startPosition);
        usedLaunchPositions.add(startPosition);
        TestUtil.assertPoint("current location", new Point(5, 1), firstPlayer.getLocation());

        startPosition = grid.getMazeLayout().getNextLaunchPosition(usedLaunchPositions);
        GridMover secondPlayer = MazeFactory.buildMover(startPosition);
        usedLaunchPositions.add(startPosition);
        TestUtil.assertPoint("current location", new Point(4, 4), secondPlayer.getLocation());

        assertNull(grid.getMazeLayout().getNextLaunchPosition(usedLaunchPositions));
    }

    @Test
    public void shouldNotWalkOnPlayer() throws Exception {

        loadGridAndTerrainFromString("##########\n" +
                "#   @    #\n" +
                "#   @    #\n" +
                "##########");

        List<GridMover> players = initPlayer(new Point(4, 1), new Point(4, 2));

        TestUtils.move(players.get(0), players, Collections.emptyList(), GridMovement.Forward, grid.getMazeLayout(), new Point(4, 1));
    }

    @Test
    public void shouldNotPushPlayer() throws Exception {

        loadGridAndTerrainFromString("##########\n" +
                "#        #\n" +
                "#   @    #\n" +
                "#   @    #\n" +
                "##########");

        List<GridMover> players = initPlayer(new Point(4, 1), new Point(4, 2));

        // Neither move ...
        TestUtils.move(players.get(0), players, Collections.emptyList(), GridMovement.Forward, grid.getMazeLayout(), new Point(4, 1));
        // nor push should be possible
        TestUtils.move(players.get(0), players, Collections.emptyList(), GridMovement.ForwardMove, grid.getMazeLayout(), new Point(4, 1));
    }

    @Test
    public void shouldNotPushOnPlayer() throws Exception {

        loadGridAndTerrainFromString("##########\n" +
                "#   @    #\n" +
                "#   $    #\n" +
                "#   @    #\n" +
                "##########");

        List<GridMover> players = initPlayer(new Point(4, 1), new Point(4, 3));
        List<GridMover> boxes = MazeFactory.buildMovers(grid.getBoxes());

        // Neither move ...
        TestUtils.move(players.get(0), players, boxes, GridMovement.Forward, grid.getMazeLayout(), new Point(4, 1));
        // nor push should be possible
        TestUtils.move(players.get(0), players, boxes, GridMovement.ForwardMove, grid.getMazeLayout(), new Point(4, 1));
    }

    @Test
    public void shouldNotPullOnPlayer() throws Exception {

        loadGridAndTerrainFromString("##########\n" +
                "#   $    #\n" +
                "#   @    #\n" +
                "#   @    #\n" +
                "##########");

        List<GridMover> players = initPlayer(new Point(4, 1), new Point(4, 2));
        List<GridMover> boxes = MazeFactory.buildMovers(grid.getBoxes());

        // Neither move ...
        TestUtils.move(players.get(1), players, boxes, GridMovement.Forward, grid.getMazeLayout(), new Point(4, 2));
        // nor push should be possible
        TestUtils.move(players.get(1), players, boxes, GridMovement.ForwardMove, grid.getMazeLayout(), new Point(4, 2));
        // and no pull
        TestUtils.move(players.get(1), players, boxes, GridMovement.Pull, grid.getMazeLayout(), new Point(4, 2), null);
        // move player1 aside. Then pull should be possible for player0
        TestUtils.move(players.get(1), players, boxes, GridMovement.Right, grid.getMazeLayout(), new Point(5, 2));
        TestUtils.move(players.get(0), players, boxes, GridMovement.Pull, grid.getMazeLayout(), new Point(4, 1), GridMovement.Pull);
    }

    @Test
    public void testCollect() throws Exception {

        loadGridAndTerrainFromString("##########\n" +
                "#   D    #\n" +
                "#   @    #\n" +
                "##########");

        List<GridMover> players = initPlayer(new Point(4, 1));
        List<GridMover> boxes = MazeFactory.buildMovers(grid.getBoxes());
        List<GridItem> diamonds = MazeFactory.buildDiamonds(grid.getDiamonds());
        TestUtil.assertEquals("diamonds", 1, diamonds.size());
        GridState gridState = new GridState(players, boxes, diamonds);

        TestUtils.move(players.get(0), gridState, GridMovement.Forward, grid.getMazeLayout(), new Point(4, 2));
        assertEquals(players.get(0).getId(), diamonds.get(0).getOwner(), "diamond owner");
        assertNull(diamonds.get(0).getLocation(), "diamond location");
    }

    private void loadGridAndTerrain(String mazeName) throws InvalidMazeException {
        grid = Grid.loadByReader(new StringReader(TestHelper.getDataBundleString("maze", mazeName))).get(0);

        terrain = new MazeTerrain(grid.getMaxWidth(), grid.getHeight());
        terrain.visualizeGrid(grid);
    }

    private void loadGridAndTerrainFromString(String gridData) throws InvalidMazeException {
        grid = Grid.loadByReader(new StringReader(gridData)).get(0);

        terrain = new MazeTerrain(grid.getMaxWidth(), grid.getHeight());
        terrain.visualizeGrid(grid);
    }

    private List<GridMover> initPlayer(Point... points) {
        List<Point> usedLaunchPositions = new ArrayList<Point>();

        Point startPosition = grid.getMazeLayout().getNextLaunchPosition(usedLaunchPositions);
        GridMover firstPlayer = MazeFactory.buildMover(startPosition);
        usedLaunchPositions.add(startPosition);
        TestUtil.assertPoint("current location first player", points[0], firstPlayer.getLocation());
        List<GridMover> players = new ArrayList<>(Arrays.asList(firstPlayer));

        if (points.length > 1) {
            startPosition = grid.getMazeLayout().getNextLaunchPosition(usedLaunchPositions);
            GridMover secondPlayer = MazeFactory.buildMover(startPosition);
            usedLaunchPositions.add(startPosition);
            TestUtil.assertPoint("current location second player", points[1], secondPlayer.getLocation());
            assertNull(grid.getMazeLayout().getNextLaunchPosition(usedLaunchPositions));
            players.add(secondPlayer);
        }

        return players;
    }
}
