package de.yard.threed.maze;

import de.yard.threed.core.Dimension;
import de.yard.threed.core.Point;
import de.yard.threed.engine.util.IntProvider;
import de.yard.threed.engine.util.RandomIntProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * There are two major strategies for generating maze grids:
 * 1) Start with an empty grid and add wall by wall. Aspects are:
 * - it might be hard to make sure there still is a way through (is solvable)
 * 2) Start with a grid full of blocks and dig corridors/areas by removing blocks. Aspects are:
 * - Starting initially with a corridor form start to destination (solution path) make it easy to keep it solvable.
 *
 * Both strategies run iterative. Mixing probably doesn't make sense.
 *
 * Method name convention is:
 * - add*() add blocks
 * - dig*() remove blocks
 */
public class MazeGenerator {

    static final int ORIENTATION_DONTCARE = 0;
    static final int ORIENTATION_HORIZONTAL = 1;
    static final int ORIENTATION_VERTICAL = 2;

    static final int INTERSECTION_NONE = 0;
    IntProvider intProvider;

    public MazeGenerator() {
        intProvider = new RandomIntProvider();
    }

    public MazeGenerator(IntProvider intProvider) {
        this.intProvider = intProvider;
    }

    public Grid baseGrid(Dimension dimension, List<Point> destinations, List<GridTeam> initialPositions) throws InvalidMazeException {

        int width = dimension.getWidth();
        int height = dimension.getHeight();

        List<Point> fields = new ArrayList<Point>();
        List<Point> walls = new ArrayList<Point>();

        for (int x = 0; x < width; x++) {
            walls.add(new Point(x, 0));
            walls.add(new Point(x, height - 1));
        }

        for (int y = 1; y < height - 1; y++) {
            walls.add(new Point(0, y));
            walls.add(new Point(width - 1, y));
        }
        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {
                fields.add(new Point(x, y));
            }
        }
        MazeLayout layout = new MazeLayout(walls, destinations, initialPositions, width, height, fields);
        return new Grid(layout, new ArrayList<Point>(), new ArrayList<Point>(), new HashMap<String, String>());
    }

    /**
     * @param grid
     * @param length
     * @param orientation      0=dont't care
     *                         1=horizontal
     *                         2=vertical
     * @param intersectionMode intersection is a shared wall block with other walls
     *                         0=no intersection, no touch
     *                         1=touch allowed
     *                         2=must touch
     *                         3=intersection allowed
     * @return
     */
    public Grid addWall(Grid grid, int length, int orientation, int intersectionMode) throws InvalidMazeException {
        List<Point> unused = grid.getUnusedFields();
        for (Point p : unused) {
            if (grid.getWallNeighbors(p).size() == 0) {
                // no wall or else around. start candidate.
                if (orientation == ORIENTATION_DONTCARE) {
                    List<Point> wall = attemptBuildWall(grid, p, length, ORIENTATION_HORIZONTAL, intersectionMode);
                    if (wall != null) {
                        return addWallsToGrid(grid, wall);
                    }
                    wall = attemptBuildWall(grid, p, length, ORIENTATION_VERTICAL, intersectionMode);
                    if (wall != null) {
                        return addWallsToGrid(grid, wall);
                    }
                } else {
                    List<Point> wall = attemptBuildWall(grid, p, length, orientation, intersectionMode);
                    if (wall != null) {
                        return addWallsToGrid(grid, wall);
                    }
                }
            }
        }
        // not possible to add a wall.
        return null;
    }

    private List<Point> attemptBuildWall(Grid grid, Point start, int length, int orientation, int intersectionMode) {
        List<Point> wall = new ArrayList<Point>();
        wall.add(start);
        Direction[] directions = null;
        switch (orientation) {
            case ORIENTATION_HORIZONTAL:
                directions = new Direction[]{Direction.E, Direction.W};
                break;
            case ORIENTATION_VERTICAL:
                directions = new Direction[]{Direction.N, Direction.S};
                break;
            default:
                throw new RuntimeException("invalid orientation " + orientation);
        }
        for (Direction direction : directions) {
            Point p = wall.get(0);
            while (canExtendWall(grid, p, direction, intersectionMode)) {
p=direction.move(p);
                wall.add(p);

                if (wall.size() >= length) {
                    return wall;
                }
            }
        }
        // not possible to build a wall with required length.
        return null;
    }

    private boolean canExtendWall(Grid grid, Point position, Direction direction, int intersectionMode) {
        switch (intersectionMode) {
            case INTERSECTION_NONE:
                Point wallCandidate = direction.move(position);
                if (grid.getWallNeighbors(wallCandidate).size() == 0) {
                    // no surrounding wall. Current origin doesn't count yet. But don't occupy start destination
                    if (grid.getUnusedFields().contains(wallCandidate)) {
                        return true;
                    }
                }
                break;
            default:
                throw new RuntimeException("not yet");
        }
        // not possible
        return false;
    }

    private Grid addWallsToGrid(Grid grid, List<Point> additionalWall) throws InvalidMazeException {
        MazeLayout layout = grid.getMazeLayout().addWalls(additionalWall);
        return new Grid(layout, new ArrayList<Point>(), new ArrayList<Point>(), new HashMap<String, String>());
    }
}
