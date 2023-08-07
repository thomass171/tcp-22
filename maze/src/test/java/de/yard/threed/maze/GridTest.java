package de.yard.threed.maze;

import de.yard.threed.core.Point;
import de.yard.threed.core.Util;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.SimpleEventBusForTesting;
import de.yard.threed.javacommon.SimpleHeadlessPlatformFactory;
import de.yard.threed.maze.testutils.ExpectedGridData;
import de.yard.threed.maze.testutils.ExpectedGridTeam;
import de.yard.threed.maze.testutils.MazeTestUtils;
import de.yard.threed.engine.testutil.EngineTestFactory;

import de.yard.threed.engine.platform.common.StringReader;
import de.yard.threed.engine.testutil.TestHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static de.yard.threed.core.testutil.TestUtils.assertPoint;
import static de.yard.threed.engine.avatar.TeamColor.TEAMCOLOR_DARKGREEN;
import static de.yard.threed.engine.avatar.TeamColor.TEAMCOLOR_RED;
import static de.yard.threed.maze.MazeTheme.THEME_TRADITIONAL;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNull;


/**
 * Tests without ECS (See MazeTest for tests with ECS) and without visuals.
 * Just for testing the grid (the maze logic) and some of the base helper.
 * Also for MazeFactory, GridState, Direction, GridOrientation and GridReader (MazeTerrain now in MazeTerrainTest).
 * <p>
 * <p>
 * Created by thomass on 15.07.15.
 */
public class GridTest {
    static Platform platform = EngineTestFactory.initPlatformForTest(new String[]{"engine", "maze"}, new SimpleHeadlessPlatformFactory(new SimpleEventBusForTesting()));
    MazeTheme mazeTheme;
    GridOrientation defaultOrientation;
    //no good idea to define statics here because it restarts platform init

    @BeforeEach
    public void setup() {
        defaultOrientation = GridOrientation.fromDirection("N");
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

        Grid grid = GridReader.readPlain(new StringReader(TestHelper.getDataBundleString("maze", "maze/grid1.txt"))).get(0);

        MazeLayout layout = grid.getMazeLayout();
        StartPosition startPosition = grid.getMazeLayout().getNextLaunchPosition(null, true);
        Assertions.assertEquals(11, layout.getMaxWidth(), "width");
        Assertions.assertEquals(7, layout.getHeight(), "height");
        Assertions.assertEquals(5, startPosition.p.getX(), "start.x");
        Assertions.assertEquals(1, startPosition.p.getY(), "start.y");

        Assertions.assertEquals(2, startPosition.p.add(Direction.N.getPoint()).getY(), "starty.");

        GridMover player = MazeFactory.buildMover(startPosition.p);

        assertPoint(new Point(5, 1), player.getLocation(), "current location");
        GridState state = new GridState(Arrays.asList(player), new ArrayList<GridMover>(), new ArrayList<GridItem>());
        List<GridMovement> moveOptions = player.getMoveOptions(state, grid.getMazeLayout());
        // 2 rotate, 3 simple move, 0 push, 8+4+8 relocates
        Assertions.assertEquals(2 + 3 + 0 + 8 + 4 + 8, moveOptions.size(), "moveOptions");
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

        Grid grid = GridReader.readPlain(new StringReader(MazeTestUtils.loadGrid("skbn/SokobanSimple.txt"))).get(0);

        StartPosition startPosition = grid.getMazeLayout().getNextLaunchPosition(null, true);
        GridMover player = MazeFactory.buildMover(startPosition.p);
        List<GridMover> boxes = MazeFactory.buildMovers(grid.getBoxes());
        Assertions.assertEquals(1, boxes.size(), "boxes");
        GridMover box = boxes.get(0);

        assertPoint(new Point(2, 1), player.getLocation(), "current location");
        GridState state = new GridState(Arrays.asList(player), boxes, new ArrayList<GridItem>());
        List<GridMovement> moveOptions = player.getMoveOptions(state, grid.getMazeLayout());
        // 2 rotate, 2 simple move, 1 push, 6 relocates (not behind box)
        Assertions.assertEquals(2 + 2 + 1 + 6, moveOptions.size(), "moveOptions");

        MazeTestUtils.rotatePlayer(player, true, new Point(2, 1));

        state = new GridState(Arrays.asList(player), boxes, new ArrayList<GridItem>());
        moveOptions = player.getMoveOptions(state, grid.getMazeLayout());
        // 2 rotate, 2 simple move, 0 push, 6 relocates (not behind box)
        Assertions.assertEquals(2 + 2 + 6, moveOptions.size(), "moveOptions");

        MazeTestUtils.move(player, GridMovement.Forward, state, grid.getMazeLayout(), new Point(1, 1));
        MazeTestUtils.rotatePlayer(player, false, new Point(1, 1));
        MazeTestUtils.move(player, GridMovement.Forward, state, grid.getMazeLayout(), new Point(1, 2));
        MazeTestUtils.rotatePlayer(player, false, new Point(1, 2));
        // forward should fail because of box
        MazeTestUtils.move(player, GridMovement.Forward, state, grid.getMazeLayout(), new Point(1, 2));
        // but push should be possible
        MazeTestUtils.move(player, GridMovement.ForwardMove, state, grid.getMazeLayout(), new Point(2, 2));
        MazeTestUtils.move(player, GridMovement.Right, state, grid.getMazeLayout(), new Point(2, 1));
        MazeTestUtils.move(player, GridMovement.Forward, state, grid.getMazeLayout(), new Point(3, 1));
        MazeTestUtils.rotatePlayer(player, true, new Point(3, 1));

        GridState gridState = new GridState(Util.buildList(player), boxes);
        Assertions.assertFalse(gridState.isSolved(grid.getMazeLayout()), "solved");
        MazeTestUtils.move(player, GridMovement.ForwardMove, state, grid.getMazeLayout(), new Point(3, 2));
        Assertions.assertTrue(gridState.isSolved(grid.getMazeLayout()), "solved");

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

        Grid grid = GridReader.readPlain(new StringReader(TestHelper.getDataBundleString("maze", "skbn/SokobanWikipedia.txt"))).get(0);

        GridMover player = MazeFactory.buildMover(grid.getMazeLayout().getNextLaunchPosition(null, true).p);
        List<GridMover> players = Arrays.asList(player);
        List<GridMover> boxes = MazeFactory.buildMovers(grid.getBoxes());
        GridMover bottombox = boxes.get(0);
        GridMover topbox = boxes.get(1);

        GridState gridState = new GridState(players, boxes);
        assertPoint(new Point(6, 1), player.getLocation(), "start location");
        assertPoint(new Point(6, 3), topbox.getLocation(), "top box start location");
        assertPoint(new Point(6, 2), bottombox.getLocation(), "bottom box start location");
        assertPoint(new Point(6, 3), gridState.findNextBox(new Point(2, 3), GridOrientation.fromDirection("E"), grid.getMazeLayout()).getLocation(), "box push location");
        Assertions.assertNotNull(((SimpleGridMover) player).canPushFrom(new Point(5, 3), GridOrientation.fromDirection("E"), gridState, grid.getMazeLayout()), "can push box ");

        // forward should fail because of unmovable box. Same for kick
        MazeTestUtils.move(player, players, boxes, GridMovement.Forward, grid.getMazeLayout(), new Point(6, 1), null);
        MazeTestUtils.move(player, players, boxes, GridMovement.Kick, grid.getMazeLayout(), new Point(6, 1), null);
        assertPoint(new Point(6, 2), bottombox.getLocation(), "bottombox box location");

        // also pull should fail
        MazeTestUtils.move(player, players, boxes, GridMovement.Pull, grid.getMazeLayout(), new Point(6, 1), null);
        assertPoint(new Point(6, 2), bottombox.getLocation(), "bottom box location");

        MazeTestUtils.move(player, players, boxes, GridMovement.Right, grid.getMazeLayout(), new Point(7, 1));
        MazeTestUtils.move(player, players, boxes, GridMovement.Forward, grid.getMazeLayout(), new Point(7, 2));
        MazeTestUtils.move(player, players, boxes, GridMovement.Forward, grid.getMazeLayout(), new Point(7, 3));

        // forward should fail because of wall
        MazeTestUtils.move(player, players, boxes, GridMovement.Forward, grid.getMazeLayout(), new Point(7, 3));

        MazeTestUtils.rotatePlayer(player, true, new Point(7, 3));

        // push box
        MazeTestUtils.move(player, players, boxes, GridMovement.ForwardMove, grid.getMazeLayout(), new Point(6, 3), GridMovement.ForwardMove);
        assertPoint(new Point(5, 3), topbox.getLocation(), "top box location");
        assertPoint(new Point(6, 3), player.getLocation(), "player location");

        // kick (keeps position, only moves box)
        MazeTestUtils.move(player, players, boxes, GridMovement.Kick, grid.getMazeLayout(), new Point(6, 3), GridMovement.Kick);
        assertPoint(new Point(4, 3), topbox.getLocation(), "top box location");
        assertPoint(new Point(6, 3), player.getLocation(), "player location");

        // kick again from same position
        MazeTestUtils.move(player, players, boxes, GridMovement.Kick, grid.getMazeLayout(), new Point(6, 3), GridMovement.Kick);
        assertPoint(new Point(3, 3), topbox.getLocation(), "top box location");

        // pull it back
        MazeTestUtils.move(player, players, boxes, GridMovement.Pull, grid.getMazeLayout(), new Point(6, 3), GridMovement.Pull);
        assertPoint(new Point(4, 3), topbox.getLocation(), "top box location");
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

        Grid grid = loadGridAndTerrain("maze/Area15x10.txt", 2, null);

        ExpectedGridData expectedGridData = new ExpectedGridData(
                new ExpectedGridTeam[]{
                        new ExpectedGridTeam(new StartPosition[]{new StartPosition(6, 4, defaultOrientation)}, false, TEAMCOLOR_DARKGREEN),
                        new ExpectedGridTeam(new StartPosition[]{new StartPosition(6, 8, GridOrientation.fromDirection("E"))}, false, TEAMCOLOR_RED)
                },
                new Point[]{},
                new Point[]{new Point(11, 2), new Point(6, 3), new Point(6, 6), new Point(10, 6)}
        );

        GridState gridState = initAndValidateGrid(grid, expectedGridData);

        GridMover player = gridState.players.get(0);
        assertPoint(new Point(6, 4), player.getLocation(), "current location");

        MazeTestUtils.move(player, gridState, GridMovement.Forward, grid.getMazeLayout(), new Point(6, 5));

        // collect diamond on (6,6)
        assertPoint(new Point(6, 6), gridState.items.get(2).getLocation(), "diamond location");
        MazeTestUtils.move(player, gridState, GridMovement.Forward, grid.getMazeLayout(), new Point(6, 6));
        assertNull(gridState.items.get(2).getLocation(), "diamond location");

        // relocate to a diamond at (11,2) looking south. Should be collected.
        assertPoint(new Point(11, 2), gridState.items.get(0).getLocation(), "diamond location");
        MazeTestUtils.move(player, gridState, GridMovement.buildRelocate(new Point(11, 2), null), grid.getMazeLayout(), new Point(11, 2));
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

        Grid grid = loadGridAndTerrain("maze/Maze-M-30x20.txt", 2, null);
        GridState gridState = initAndValidateGrid(grid, ExpectedGridData.buildForM_30x20());
        assertFalse(gridState.isSolved(grid.getMazeLayout()));

        GridMover player = gridState.players.get(0);
        MazeTestUtils.move(player, gridState, GridMovement.Left, grid.getMazeLayout(), new Point(27, 3));

        assertFalse(gridState.isSolved(grid.getMazeLayout()));

    }

    @Test
    public void testMultiple() throws Exception {

        List<Grid> grids = GridReader.readPlain(new StringReader(TestHelper.getDataBundleString("maze", "skbn/DavidJoffe.txt")));

        Assertions.assertEquals(90, grids.size(), "grids");

        Grid g = Grid.findByTitle(grids, "2");

        Assertions.assertNotNull(g);
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

        Grid grid = loadGridAndTerrain("maze/Maze-P-Simple.txt", 2, null);
        GridState gridState = initAndValidateGrid(grid, ExpectedGridData.buildForP_simple(false));

        GridMover player = gridState.players.get(0);
        MazeTestUtils.move(player, gridState, GridMovement.Right, grid.getMazeLayout(), new Point(6, 1));
        MazeTestUtils.move(player, gridState, GridMovement.Right, grid.getMazeLayout(), new Point(7, 1));
        MazeTestUtils.move(player, gridState, GridMovement.Left, grid.getMazeLayout(), new Point(6, 1));
        MazeTestUtils.move(player, gridState, GridMovement.Left, grid.getMazeLayout(), new Point(5, 1));
        MazeTestUtils.move(player, gridState, GridMovement.Left, grid.getMazeLayout(), new Point(4, 1));
        MazeTestUtils.move(player, gridState, GridMovement.Left, grid.getMazeLayout(), new Point(3, 1));
        MazeTestUtils.move(player, gridState, GridMovement.Forward, grid.getMazeLayout(), new Point(3, 2));

        // collect. There are no bullets without ECS.
        assertFalse(gridState.isSolved(grid.getMazeLayout()));
        // how to know it is diamond 1?
        GridItem diamond = gridState.items.get(1);
        assertEquals(-1, diamond.getOwner());
        assertNotNull(diamond.getLocation());
        MazeTestUtils.move(player, gridState, GridMovement.Forward, grid.getMazeLayout(), new Point(3, 3));
        assertEquals(player.getId(), diamond.getOwner());
        assertNull(diamond.getLocation());
        assertTrue(gridState.isSolved(grid.getMazeLayout()));
    }

    @Test
    public void shouldNotWalkOnPlayer() throws Exception {

        Grid grid = loadGridAndTerrainFromString("##########\n" +
                "#   @    #\n" +
                "#   @    #\n" +
                "##########", 1, null);

        List<GridMover> players = initGridMover(grid,
                new GridTeam[]{new GridTeam(new StartPosition[]{new StartPosition(4, 1, GridOrientation.fromDirection("E")), new StartPosition(4, 2, GridOrientation.fromDirection("E"))}, false)});

        MazeTestUtils.move(players.get(0), players, Collections.emptyList(), GridMovement.Forward, grid.getMazeLayout(), new Point(4, 1));
    }

    @Test
    public void shouldNotPushPlayer() throws Exception {

        Grid grid = loadGridAndTerrainFromString("##########\n" +
                "#        #\n" +
                "#   @    #\n" +
                "#   @    #\n" +
                "##########", 1, null);

        List<GridMover> players = initGridMover(grid,
                new GridTeam[]{new GridTeam(new StartPosition[]{new StartPosition(4, 1, GridOrientation.fromDirection("E")),
                        new StartPosition(4, 2, defaultOrientation)}, false)});

        // Neither move ...
        MazeTestUtils.move(players.get(0), players, Collections.emptyList(), GridMovement.Forward, grid.getMazeLayout(), new Point(4, 1));
        // nor push should be possible
        MazeTestUtils.move(players.get(0), players, Collections.emptyList(), GridMovement.ForwardMove, grid.getMazeLayout(), new Point(4, 1));
    }

    @Test
    public void shouldNotPushOnPlayer() throws Exception {

        Grid grid = loadGridAndTerrainFromString("##########\n" +
                "#   @    #\n" +
                "#   $    #\n" +
                "#   @    #\n" +
                "##########", 2, null);

        List<GridMover> players = initGridMover(grid,
                new GridTeam[]{
                        new GridTeam(new StartPosition[]{new StartPosition(4, 1, defaultOrientation)}, false),
                        new GridTeam(new StartPosition[]{new StartPosition(4, 3, GridOrientation.fromDirection("E"))}, false)});
        List<GridMover> boxes = MazeFactory.buildMovers(grid.getBoxes());

        // Neither move ...
        MazeTestUtils.move(players.get(0), players, boxes, GridMovement.Forward, grid.getMazeLayout(), new Point(4, 1));
        // nor push should be possible
        MazeTestUtils.move(players.get(0), players, boxes, GridMovement.ForwardMove, grid.getMazeLayout(), new Point(4, 1));
    }

    @Test
    public void shouldNotPullOnPlayer() throws Exception {

        Grid grid = loadGridAndTerrainFromString("##########\n" +
                "#   $    #\n" +
                "#   @    #\n" +
                "#   @    #\n" +
                "##########", 1, null);

        List<GridMover> players = initGridMover(grid,
                new GridTeam[]{new GridTeam(new StartPosition[]{new StartPosition(4, 1, GridOrientation.fromDirection("E")), new StartPosition(4, 2, defaultOrientation)}, false)});
        List<GridMover> boxes = MazeFactory.buildMovers(grid.getBoxes());

        // Neither move ...
        MazeTestUtils.move(players.get(1), players, boxes, GridMovement.Forward, grid.getMazeLayout(), new Point(4, 2));
        // nor push should be possible for player 1
        MazeTestUtils.move(players.get(1), players, boxes, GridMovement.ForwardMove, grid.getMazeLayout(), new Point(4, 2));
        // and no pull
        MazeTestUtils.move(players.get(1), players, boxes, GridMovement.Pull, grid.getMazeLayout(), new Point(4, 2), null);
        // move player1 aside. Then pull should be possible for player0
        MazeTestUtils.move(players.get(1), players, boxes, GridMovement.Right, grid.getMazeLayout(), new Point(5, 2));
        MazeTestUtils.move(players.get(0), players, boxes, GridMovement.Pull, grid.getMazeLayout(), new Point(4, 1), GridMovement.Pull);
    }

    @Test
    public void testCollect() throws Exception {

        Grid grid = loadGridAndTerrainFromString("##########\n" +
                "#   D    #\n" +
                "#   @    #\n" +
                "##########", 1, null);

        ExpectedGridData expectedGridData = new ExpectedGridData(
                new ExpectedGridTeam[]{
                        new ExpectedGridTeam(new StartPosition[]{new StartPosition(4, 1, defaultOrientation)}, false, TEAMCOLOR_DARKGREEN),
                },
                new Point[]{},
                new Point[]{new Point(4, 2)}
        );

        GridState gridState = initAndValidateGrid(grid, expectedGridData);
        Assertions.assertEquals(1, gridState.items.size(), "diamonds");

        MazeTestUtils.move(gridState.players.get(0), gridState, GridMovement.Forward, grid.getMazeLayout(), new Point(4, 2));
        assertEquals(gridState.players.get(0).getId(), gridState.items.get(0).getOwner(), "diamond owner");
        assertNull(gridState.items.get(0).getLocation(), "diamond location");
    }

    @Test
    public void shouldNotEnterOtherHome() throws Exception {

        Grid grid = loadGridAndTerrainFromString("##########\n" +
                "#   @    #\n" +
                "#        #\n" +
                "#   @    #\n" +
                "##########", 2, null);

        List<GridMover> players = initGridMover(grid, new GridTeam[]{
                new GridTeam(new StartPosition[]{new StartPosition(4, 1, defaultOrientation)}, false),
                new GridTeam(new StartPosition[]{new StartPosition(4, 3, GridOrientation.fromDirection("E"))}, false)});

        List<GridMover> boxes = MazeFactory.buildMovers(grid.getBoxes());

        // just leave home
        MazeTestUtils.move(players.get(1), players, boxes, GridMovement.Forward, grid.getMazeLayout(), new Point(5, 3), GridMovement.Forward);
        MazeTestUtils.move(players.get(0), players, boxes, GridMovement.Forward, grid.getMazeLayout(), new Point(4, 2), GridMovement.Forward);
        // don't enter others home.
        MazeTestUtils.move(players.get(0), players, boxes, GridMovement.Forward, grid.getMazeLayout(), new Point(4, 2));
        // but walk to own home should be possible
        MazeTestUtils.move(players.get(0), players, boxes, GridMovement.Back, grid.getMazeLayout(), new Point(4, 1), GridMovement.Back);
    }

    @Test
    public void testSimpleMaze() throws Exception {

        Grid grid = loadGridAndTerrainFromString("##########\n" +
                "#   .    #\n" +
                "#        #\n" +
                "#   @    #\n" +
                "##########", 1, null);

        ExpectedGridData expectedGridData = new ExpectedGridData(
                new ExpectedGridTeam[]{
                        new ExpectedGridTeam(new StartPosition[]{new StartPosition(4, 1, defaultOrientation)}, false, TEAMCOLOR_DARKGREEN),
                },
                new Point[]{},
                new Point[]{}
        );

        GridState gridState = initAndValidateGrid(grid, expectedGridData);

        assertFalse(gridState.isSolved(grid.getMazeLayout()));

        MazeTestUtils.move(gridState.players.get(0), gridState, GridMovement.Forward, grid.getMazeLayout(), new Point(4, 2));
        MazeTestUtils.move(gridState.players.get(0), gridState, GridMovement.Forward, grid.getMazeLayout(), new Point(4, 3));
        assertTrue(gridState.isSolved(grid.getMazeLayout()));
    }

    @Test
    public void testGridReaderSokoban() throws Exception {

        Grid grid = GridReader.readPlain(new StringReader(TestHelper.getDataBundleString("maze", "skbn/SokobanWikipedia.txt"))).get(0);

        // has three more blanks than original at end of irst line.
        String expectedRawGrid = "  ####   n###  ####n#     $ #n# #  #$ #n# . .#@ #n#########n";
        // grid rebuild doesn't use traditional sokoban style
        assertEquals(expectedRawGrid.replace("$", "B"), grid.getRawGrid("n"));
    }

    @Test
    public void testGridReaderD80x25() throws Exception {

        String rawGrid = TestHelper.getDataBundleString("maze", "maze/Maze-D-80x25.txt");
        Grid grid = GridReader.readPlain(new StringReader(rawGrid)).get(0);

        String expectedRawGrid = rawGrid;
        assertEquals(expectedRawGrid, grid.getRawGrid("\n"));
    }

    /**
     * A monster is a bot player.
     */
    @Test
    public void testPlayerStartLocation() throws Exception {

        Grid grid = loadGridAndTerrainFromString("##########\n" +
                "#   .    #\n" +
                "#  @     #\n" +
                "#   M    #\n" +
                "##########", 2, null);

        ExpectedGridData expectedGridData = new ExpectedGridData(
                new ExpectedGridTeam[]{
                        new ExpectedGridTeam(new StartPosition[]{new StartPosition(4, 1, defaultOrientation)}, true, null),
                        new ExpectedGridTeam(new StartPosition[]{new StartPosition(3, 2, defaultOrientation)}, false, TEAMCOLOR_DARKGREEN)
                },
                new Point[]{},
                new Point[]{}
        );

        GridState gridState = initAndValidateGrid(grid, expectedGridData);

        assertPoint(new Point(3, 2), grid.getMazeLayout().getNextLaunchPosition(null, false).p);
    }

    /**
     * ################################################################################
     * ####  M         ################################################################
     * ####  M         ################################################################
     * ####                  ##########################################################
     * ######D## ########### ##########################################################
     * ######### ###########                        ###################################
     * ######### ################################## ###################################
     * ######### ################################## ###################################
     * ########     #######    #################### ###################################
     * ########  M  #######    #################### ###################################
     * ########     #######  M ####################            ########################
     * #####            ###    ############################### ########################
     * ##### ##     ### ###    ##################     ######## ########################
     * ##### ##     ### ###    ################## M      ##### ########################
     * ##### ##     ### #### #################### M   ## ##### ########################
     * ##### ##     ### ###  ##############           ##      B       #################
     * ##### ##  M  ### ### ############### #####     ######## ###### #################
     * ##### ########## ### ############### ################## ###### #################
     * ##### ########## ##   ############## ################## ###### #################
     * ##### ########## ##   ##############       D####        ######                 #
     * #@ ## ########## ##   ############## ########################################  #
     * #@    ##########              ###### ######################################## D#
     * #@ ################   ##      ###### ###########################################
     * ########################  M          ###########################################
     * ################################################################################
     */
    @Test
    public void testD_80x25() throws Exception {
        runD_80x25(null);
    }

    @Test
    public void testD_80x25Teamsize1() throws Exception {
        runD_80x25("1");
    }

    public void runD_80x25(String teamSize) throws Exception {

        Grid grid = loadGridAndTerrain("maze/Maze-D-80x25.txt", 7, teamSize);
        ExpectedGridData expectedGridData = ExpectedGridData.buildForD_80x25(teamSize);
        GridState gridState = initAndValidateGrid(grid, expectedGridData);
        assertFalse(gridState.isSolved(grid.getMazeLayout()));

        GridMover player = gridState.players.get(0);
        // t.b.c. with moving

    }

    public static Grid loadGridAndTerrain(String mazeName, int expectedNumberOfTeams, String teamSize) throws InvalidMazeException {
        return loadGridAndTerrainFromString(TestHelper.getDataBundleString("maze", mazeName), expectedNumberOfTeams, teamSize);
    }

    public static Grid loadGridAndTerrainFromString(String gridData, int expectedNumberOfTeams, String teamSize) throws InvalidMazeException {
        Grid grid = GridReader.readWithModificator(new StringReader(gridData), teamSize).get(0);
        assertEquals(expectedNumberOfTeams, grid.getMazeLayout().getNumberOfTeams(), "number of teams");

        return grid;
    }

    /**
     * Monster are also player.
     */
    public static GridState initAndValidateGrid(Grid grid, ExpectedGridData expectedGridData) {
        List<GridMover> players = initGridMover(grid, expectedGridData.expectedTeams);
        List<GridMover> boxes = MazeFactory.buildMovers(grid.getBoxes());
        for (int i = 0; i < boxes.size(); i++) {
            assertPoint(expectedGridData.expectedBoxesLocations[i], boxes.get(i).getLocation(), "box " + i + " ");
        }
        List<GridItem> items = MazeFactory.buildItems(grid.getDiamonds(), 'D');
        for (int i = 0; i < items.size(); i++) {
            assertPoint(expectedGridData.expectedItemsLocations[i], items.get(i).getLocation(), "point" + i + " ");
        }
        return new GridState(players, boxes, items);
    }

    /**
     * Player(User) and monster (monster is nothing special here).
     */
    public static List<GridMover> initGridMover(Grid grid, GridTeam[] expectedTeams) {
        List<Point> usedLaunchPositions = new ArrayList<Point>();
        List<GridMover> players = new ArrayList<>();

        StartPosition startPosition;
        for (int team = 0; team < expectedTeams.length; team++) {
            for (int index = 0; index < expectedTeams[team].positions.size(); index++) {
                if ((startPosition = grid.getMazeLayout().getNextLaunchPosition(usedLaunchPositions, true)) != null) {
                    int teamID = grid.getMazeLayout().getTeamByHome(startPosition.p);
                    assertEquals(team, teamID);
                    List<Point> teamHomes = grid.getMazeLayout().getTeamByIndex(teamID).positions.stream().map(StartPosition::getPoint).collect(Collectors.toList());
                    GridMover playerOrMonster = MazeFactory.buildMover(startPosition, teamID/*new Team(teamID, teamHomes)*/);
                    usedLaunchPositions.add(startPosition.p);
                    Point expectLocation = expectedTeams[team].positions.get(index).getPoint();
                    assertPoint(expectLocation, playerOrMonster.getLocation(), "current location player, team=" + team + " ");
                    assertTrue(MazeUtils.getHomesOfTeam(grid.getMazeLayout(), playerOrMonster.getTeam()).contains(expectLocation));
                    players.add(playerOrMonster);
                    /*currentIndex++;
                    if (currentIndex >= expectedTeams[currentTeam].positions.size()) {
                        currentTeam++;
                        currentIndex = 0;
                    }*/
                } else {
                    fail("no start position");
                }
            }
        }
        //assertEquals(points.length, players.size());

        return players;
    }
}
