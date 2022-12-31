package de.yard.threed.maze;

import de.yard.threed.core.Point;
import de.yard.threed.core.Util;
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
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNull;


/**
 * Tests without ECS (See MazeTest for tests with ECS) and without visuals.
 * Just for testing the grid (the maze logic).
 * Also for GridState, MazeTerrain, Direction and GridOrientation.
 * <p>
 * <p>
 * Created by thomass on 15.07.15.
 */
public class GridTest {
    static Platform platform = TestFactory.initPlatformForTest(new String[]{"engine", "maze"}, new PlatformFactoryHeadless());

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

        GridState gridState = new GridState(Util.buildList(player), boxes);
        TestUtil.assertFalse("solved", gridState.isSolved(grid.getMazeLayout()));
        TestUtils.move(player, GridMovement.ForwardMove, state, grid.getMazeLayout(), new Point(3, 2));
        TestUtil.assertTrue("solved", gridState.isSolved(grid.getMazeLayout()));

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
        TestUtil.assertPoint("box push location", new Point(6, 3), gridState.findNextBox(new Point(2, 3), GridOrientation.fromDirection("E"), grid.getMazeLayout()).getLocation());
        TestUtil.assertNotNull("can push box ", ((SimpleGridMover) player).canPushFrom(new Point(5, 3), GridOrientation.fromDirection("E"), gridState, grid.getMazeLayout()));

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

        Grid grid = loadGridAndTerrain("skbn/SokobanWikipedia.txt", 1);
        MazeTerrain terrain = new MazeTerrain(grid.getMaxWidth(), grid.getHeight());
        terrain.visualizeGrid(grid);
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

        Grid grid = loadGridAndTerrain("maze/Area15x10.txt", 2);
        MazeTerrain terrain = new MazeTerrain(grid.getMaxWidth(), grid.getHeight());
        terrain.visualizeGrid(grid);

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

        GridState gridState = initContent(grid, new Point[]{new Point(6, 4), new Point(6, 8)}, new Point[]{},
                new Point[]{new Point(11, 2), new Point(6, 3), new Point(6, 6), new Point(10, 6)});

        GridMover player = gridState.players.get(0);
        TestUtil.assertPoint("current location", new Point(6, 4), player.getLocation());

        TestUtils.move(player, gridState, GridMovement.Forward, grid.getMazeLayout(), new Point(6, 5));

        // collect diamond on (6,6)
        TestUtil.assertPoint(new Point(6, 6), gridState.items.get(2).getLocation(), "diamond location");
        TestUtils.move(player, gridState, GridMovement.Forward, grid.getMazeLayout(), new Point(6, 6));
        assertNull(gridState.items.get(2).getLocation(), "diamond location");

        // relocate to a diamond at (11,2) looking south. Should be collected.
        TestUtil.assertPoint(new Point(11, 2), gridState.items.get(0).getLocation(), "diamond location");
        TestUtils.move(player, gridState, GridMovement.buildRelocate(new Point(11, 2), null), grid.getMazeLayout(), new Point(11, 2));
        assertNull(gridState.items.get(0).getLocation(), "diamond location");

    }

    /**
     * A monster is a bot player.
     * ##############################
     * #                            #
     * #  #         # ##            #
     * # .####         #            #
     * ####            #        #####
     * #    #            #          #
     * #  ###         ####          #
     * #  #           #             #
     * #    #               MMM     #
     * #                            #
     * #  #         # ##            #
     * #  #         # ##            #
     * #  #         # ##            #
     * #  ####         #            #
     * ####            #        #####
     * #    #            #          #
     * #  ###         ####         @#
     * #  #           #             #
     * #    #                       #
     * ##############################
     */
    @Test
    public void testM_30x20() throws Exception {

        Grid grid = loadGridAndTerrain("maze/Maze-M-30x20.txt", 2);

        GridState gridState = initContent(grid, new Point[]{new Point(28, 3), new Point(21, 11), new Point(22, 11), new Point(23, 11)}, new Point[]{},
                new Point[]{});
        assertFalse(gridState.isSolved(grid.getMazeLayout()));

        GridMover player = gridState.players.get(0);
        TestUtils.move(player, gridState, GridMovement.Left, grid.getMazeLayout(), new Point(27, 3));

        assertFalse(gridState.isSolved(grid.getMazeLayout()));

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
     * A monster is a bot player.
     * ##########
     * #   M    #
     * #  D# #  #
     * #   # #  #
     * #    P D #
     * ##########
     */
    @Test
    public void testP_Simple() throws Exception {

        Grid grid = loadGridAndTerrain("maze/Maze-P-Simple.txt", 2);

        GridState gridState = initContent(grid, new Point[]{new Point(5, 1), new Point(4, 4)}, new Point[]{}, new Point[]{new Point(7, 1), new Point(3, 3)});

        GridMover player = gridState.players.get(0);
        TestUtils.move(player, gridState, GridMovement.Right, grid.getMazeLayout(), new Point(6, 1));
        TestUtils.move(player, gridState, GridMovement.Right, grid.getMazeLayout(), new Point(7, 1));
        TestUtils.move(player, gridState, GridMovement.Left, grid.getMazeLayout(), new Point(6, 1));
        TestUtils.move(player, gridState, GridMovement.Left, grid.getMazeLayout(), new Point(5, 1));
        TestUtils.move(player, gridState, GridMovement.Left, grid.getMazeLayout(), new Point(4, 1));
        TestUtils.move(player, gridState, GridMovement.Left, grid.getMazeLayout(), new Point(3, 1));
        TestUtils.move(player, gridState, GridMovement.Forward, grid.getMazeLayout(), new Point(3, 2));

        // collect. There are no bullets without ECS.
        assertFalse(gridState.isSolved(grid.getMazeLayout()));
        // how to know it is diamond 1?
        GridItem diamond = gridState.items.get(1);
        assertEquals(-1, diamond.getOwner());
        assertNotNull(diamond.getLocation());
        TestUtils.move(player, gridState, GridMovement.Forward, grid.getMazeLayout(), new Point(3, 3));
        assertEquals(player.getId(), diamond.getOwner());
        assertNull(diamond.getLocation());
        assertTrue(gridState.isSolved(grid.getMazeLayout()));
    }

    @Test
    public void shouldNotWalkOnPlayer() throws Exception {

        Grid grid = loadGridAndTerrainFromString("##########\n" +
                "#   @    #\n" +
                "#   @    #\n" +
                "##########", 1);

        List<GridMover> players = initPlayer(grid, new Point(4, 1), new Point(4, 2));

        TestUtils.move(players.get(0), players, Collections.emptyList(), GridMovement.Forward, grid.getMazeLayout(), new Point(4, 1));
    }

    @Test
    public void shouldNotPushPlayer() throws Exception {

        Grid grid = loadGridAndTerrainFromString("##########\n" +
                "#        #\n" +
                "#   @    #\n" +
                "#   @    #\n" +
                "##########", 1);

        List<GridMover> players = initPlayer(grid, new Point(4, 1), new Point(4, 2));

        // Neither move ...
        TestUtils.move(players.get(0), players, Collections.emptyList(), GridMovement.Forward, grid.getMazeLayout(), new Point(4, 1));
        // nor push should be possible
        TestUtils.move(players.get(0), players, Collections.emptyList(), GridMovement.ForwardMove, grid.getMazeLayout(), new Point(4, 1));
    }

    @Test
    public void shouldNotPushOnPlayer() throws Exception {

        Grid grid = loadGridAndTerrainFromString("##########\n" +
                "#   @    #\n" +
                "#   $    #\n" +
                "#   @    #\n" +
                "##########", 2);

        List<GridMover> players = initPlayer(grid, new Point(4, 1), new Point(4, 3));
        List<GridMover> boxes = MazeFactory.buildMovers(grid.getBoxes());

        // Neither move ...
        TestUtils.move(players.get(0), players, boxes, GridMovement.Forward, grid.getMazeLayout(), new Point(4, 1));
        // nor push should be possible
        TestUtils.move(players.get(0), players, boxes, GridMovement.ForwardMove, grid.getMazeLayout(), new Point(4, 1));
    }

    @Test
    public void shouldNotPullOnPlayer() throws Exception {

        Grid grid = loadGridAndTerrainFromString("##########\n" +
                "#   $    #\n" +
                "#   @    #\n" +
                "#   @    #\n" +
                "##########", 1);

        List<GridMover> players = initPlayer(grid, new Point(4, 1), new Point(4, 2));
        List<GridMover> boxes = MazeFactory.buildMovers(grid.getBoxes());

        // Neither move ...
        TestUtils.move(players.get(1), players, boxes, GridMovement.Forward, grid.getMazeLayout(), new Point(4, 2));
        // nor push should be possible for player 1
        TestUtils.move(players.get(1), players, boxes, GridMovement.ForwardMove, grid.getMazeLayout(), new Point(4, 2));
        // and no pull
        TestUtils.move(players.get(1), players, boxes, GridMovement.Pull, grid.getMazeLayout(), new Point(4, 2), null);
        // move player1 aside. Then pull should be possible for player0
        TestUtils.move(players.get(1), players, boxes, GridMovement.Right, grid.getMazeLayout(), new Point(5, 2));
        TestUtils.move(players.get(0), players, boxes, GridMovement.Pull, grid.getMazeLayout(), new Point(4, 1), GridMovement.Pull);
    }

    @Test
    public void testCollect() throws Exception {

        Grid grid = loadGridAndTerrainFromString("##########\n" +
                "#   D    #\n" +
                "#   @    #\n" +
                "##########", 1);

        GridState gridState = initContent(grid, new Point[]{new Point(4, 1)}, new Point[]{}, new Point[]{new Point(4, 2)});
        TestUtil.assertEquals("diamonds", 1, gridState.items.size());

        TestUtils.move(gridState.players.get(0), gridState, GridMovement.Forward, grid.getMazeLayout(), new Point(4, 2));
        assertEquals(gridState.players.get(0).getId(), gridState.items.get(0).getOwner(), "diamond owner");
        assertNull(gridState.items.get(0).getLocation(), "diamond location");
    }

    @Test
    public void shouldNotEnterOtherHome() throws Exception {

        Grid grid = loadGridAndTerrainFromString("##########\n" +
                "#   @    #\n" +
                "#        #\n" +
                "#   @    #\n" +
                "##########", 2);

        List<GridMover> players = initPlayer(grid, new Point(4, 1), new Point(4, 3));
        List<GridMover> boxes = MazeFactory.buildMovers(grid.getBoxes());

        // just leave home
        TestUtils.move(players.get(1), players, boxes, GridMovement.Forward, grid.getMazeLayout(), new Point(5, 3), GridMovement.Forward);
        TestUtils.move(players.get(0), players, boxes, GridMovement.Forward, grid.getMazeLayout(), new Point(4, 2), GridMovement.Forward);
        // don't enter others home.
        TestUtils.move(players.get(0), players, boxes, GridMovement.Forward, grid.getMazeLayout(), new Point(4, 2));
        // but walk to own home should be possible
        TestUtils.move(players.get(0), players, boxes, GridMovement.Back, grid.getMazeLayout(), new Point(4, 1), GridMovement.Back);
    }

    @Test
    public void testSimpleMaze() throws Exception {

        Grid grid = loadGridAndTerrainFromString("##########\n" +
                "#   .    #\n" +
                "#        #\n" +
                "#   @    #\n" +
                "##########", 1);

        GridState gridState = initContent(grid, new Point[]{new Point(4, 1)}, new Point[]{}, new Point[]{});

        assertFalse(gridState.isSolved(grid.getMazeLayout()));

        TestUtils.move(gridState.players.get(0), gridState, GridMovement.Forward, grid.getMazeLayout(), new Point(4, 2));
        TestUtils.move(gridState.players.get(0), gridState, GridMovement.Forward, grid.getMazeLayout(), new Point(4, 3));
        assertTrue(gridState.isSolved(grid.getMazeLayout()));
    }

    public static Grid loadGridAndTerrain(String mazeName, int expectedNumberOfTeams) throws InvalidMazeException {
        return loadGridAndTerrainFromString(TestHelper.getDataBundleString("maze", mazeName), expectedNumberOfTeams);
    }

    public static Grid loadGridAndTerrainFromString(String gridData, int expectedNumberOfTeams) throws InvalidMazeException {
        Grid grid = Grid.loadByReader(new StringReader(gridData)).get(0);
        assertEquals(expectedNumberOfTeams, grid.getMazeLayout().getNumberOfTeams());

        return grid;
    }

    /**
     * Monster are also player.
     */
    public static GridState initContent(Grid grid, Point[] expectedPlayerLocations, Point[] expectedBoxesLocations, Point[] expectedItemsLocations) {
        List<GridMover> players = initPlayer(grid, expectedPlayerLocations);
        List<GridMover> boxes = MazeFactory.buildMovers(grid.getBoxes());
        for (int i = 0; i < boxes.size(); i++) {
            TestUtil.assertPoint(expectedBoxesLocations[i], boxes.get(i).getLocation());
        }
        List<GridItem> items = MazeFactory.buildItems(grid.getDiamonds(), 'D');
        for (int i = 0; i < items.size(); i++) {
            TestUtil.assertPoint(expectedItemsLocations[i], items.get(i).getLocation());
        }
        return new GridState(players, boxes, items);
    }

    public static List<GridMover> initPlayer(Grid grid, Point... points) {
        List<Point> usedLaunchPositions = new ArrayList<Point>();
        List<GridMover> players = new ArrayList<>();

        Point startPosition;
        while ((startPosition = grid.getMazeLayout().getNextLaunchPosition(usedLaunchPositions)) != null) {
            int teamID = grid.getMazeLayout().getTeamByHome(startPosition);
            List<Point> teamHomes = grid.getMazeLayout().getStartPositionsOfTeam(teamID).stream().map(StartPosition::getPoint).collect(Collectors.toList());
            GridMover player = MazeFactory.buildMover(startPosition, grid.getMazeLayout().getInitialOrientation(startPosition), new Team(teamID, teamHomes));
            usedLaunchPositions.add(startPosition);
            TestUtil.assertPoint("current location player", points[players.size()], player.getLocation());
            assertTrue(player.getTeam().homeFields.contains(points[players.size()]));
            players.add(player);
        }
        assertEquals(points.length, players.size());

        return players;
    }
}
