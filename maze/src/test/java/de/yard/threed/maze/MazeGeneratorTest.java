package de.yard.threed.maze;

import de.yard.threed.core.Dimension;
import de.yard.threed.core.Point;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.SimpleEventBusForTesting;
import de.yard.threed.engine.util.DeterministicIntProvider;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.engine.util.IntProvider;
import de.yard.threed.javacommon.SimpleHeadlessPlatformFactory;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MazeGeneratorTest {

    static Platform platform = EngineTestFactory.initPlatformForTest(new String[]{"engine", "maze"}, new SimpleHeadlessPlatformFactory(new SimpleEventBusForTesting()));

    @Test
    public void test6x5() throws InvalidMazeException {
        IntProvider intProvider = new DeterministicIntProvider(new int[]{0});
        MazeGenerator generator = new MazeGenerator(intProvider);


        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> {
            buildSimpleGrid(generator, 6, 5);
        });
        /*TODO org.junit.jupiter.api.Assertions.assertThrows(InvalidMazeException.class, () -> {
            buildSimpleGrid(generator, 4,5);
        });*/

        Grid grid = buildSimpleGrid(generator, 6, 5);

        assertEquals(2 * 6 + 2 * 3, grid.getMazeLayout().getWalls().size());
        assertEquals(4 * 3, grid.getMazeLayout().getFields().size());
        assertEquals(4 * 3 - 2, grid.getUnusedFields().size());

        // exactly one wall possible
        Grid newGrid = generator.addWall(grid, 2, MazeGenerator.ORIENTATION_HORIZONTAL, MazeGenerator.INTERSECTION_NONE);
        assertNotNull(newGrid);
        List<Point> newWalls = newGrid.getMazeLayout().getWalls();
        assertEquals(2 * 6 + 2 * 3 + 2, newWalls.size());
        assertTrue(newWalls.contains(new Point(2, 2)), "(2,2)");
        assertTrue(newWalls.contains(new Point(3, 2)), "(3,2)");
        // but no more
        assertNull(generator.addWall(newGrid, 2, MazeGenerator.ORIENTATION_DONTCARE, MazeGenerator.INTERSECTION_NONE));
    }

    @Test
    public void test10x8() throws InvalidMazeException {
        IntProvider intProvider = new DeterministicIntProvider(new int[]{0});
        MazeGenerator generator = new MazeGenerator(intProvider);

        Grid grid = buildSimpleGrid(generator, 10, 8);

        int initialWallElements = 2 * 10 + 2 * (8 - 2);
        assertEquals(initialWallElements, grid.getMazeLayout().getWalls().size());
        //assertEquals((10-2) * (8-2), grid.getMazeLayout().getFields().size());
        //assertEquals(4 * 3 - 2, grid.getUnusedFields().size());

        //
        Grid newGrid = generator.addWall(grid, 6, MazeGenerator.ORIENTATION_HORIZONTAL, MazeGenerator.INTERSECTION_NONE);
        assertNotNull(newGrid);
        List<Point> newWalls = newGrid.getMazeLayout().getWalls();
        assertEquals(initialWallElements + 6, newWalls.size());
        // one more
        newGrid = generator.addWall(newGrid, 6, MazeGenerator.ORIENTATION_HORIZONTAL, MazeGenerator.INTERSECTION_NONE);
        assertNotNull(newGrid);
        newWalls = newGrid.getMazeLayout().getWalls();
        assertEquals(initialWallElements + 6 + 6, newWalls.size());
        // but no more
        assertNull(generator.addWall(newGrid, 2, MazeGenerator.ORIENTATION_DONTCARE, MazeGenerator.INTERSECTION_NONE));
    }

    private Grid buildSimpleGrid(MazeGenerator generator, int width, int height) throws InvalidMazeException {
        List<GridTeam> startPositions = Arrays.asList(new GridTeam(new StartPosition(new Point(4, 1)), false));
        List<Point> destinations = Arrays.asList(new Point(2, 3));
        return generator.baseGrid(new Dimension(width, height), destinations, startPositions);
    }
}
