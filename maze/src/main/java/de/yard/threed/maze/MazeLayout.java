package de.yard.threed.maze;

import de.yard.threed.core.Point;

import java.util.List;

/**
 * Die statischen Teile eines Maze.
 * <p>
 * 26.4.21: Und darum ist es jetzt auch static/singleton. NeeNee, das muss dann woanders ein, in Grid.
 *
 * <p>
 * Created by thomass on 07.03.17.
 */
public class MazeLayout {
    public List<Point> walls;
    public List<Point> destinations;
    public Point initialPosition;
    public GridOrientation initialOrientation;
    //private static MazeLayout instance;
    int maxwidth, height;
    public List<Point> fields;

    public MazeLayout(List<Point> walls, List<Point> destinations, Point initialPosition, GridOrientation initialOrientation, int maxwidth, int height, List<Point> fields) {
        this.walls = walls;
        this.destinations = destinations;
        this.initialPosition = initialPosition;
        this.initialOrientation = initialOrientation;
        this.maxwidth = maxwidth;
        this.height = height;
        this.fields = fields;
    }


    public boolean isWallAt(Point p) {
        return walls.contains(p);
    }
}
