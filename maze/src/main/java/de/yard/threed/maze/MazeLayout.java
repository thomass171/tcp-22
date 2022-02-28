package de.yard.threed.maze;

import de.yard.threed.core.Point;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;

import java.util.List;

/**
 * Static parts of a maze.
 * Somehow duplicate to {@Grid}? Not really, grid also contains initial non static elements.
 *
 * <p>
 * 26.4.21: Und darum ist es jetzt auch static/singleton. NeeNee, das muss dann woanders ein, in Grid.
 *
 * <p>
 * Created by thomass on 07.03.17.
 */
public class MazeLayout {
    Log logger = Platform.getInstance().getLog(MazeLayout.class);
    public List<Point> walls;
    public List<Point> destinations;
    // Several sets of start positions. Starting at lower (small y) left (small x).
    public List<List<Point>> initialPosition;
    public GridOrientation initialOrientation;
    //private static MazeLayout instance;
    int maxwidth, height;
    public List<Point> fields;

    public MazeLayout(List<Point> walls, List<Point> destinations, List<List<Point>> initialPosition, GridOrientation initialOrientation, int maxwidth, int height, List<Point> fields) {
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

    public Point getNextLaunchPosition(List<Point> usedLaunchPositions) {
        for (List<Point> pset:initialPosition){
            for (Point p:pset) {
                if (usedLaunchPositions == null || !usedLaunchPositions.contains(p)){
                    return p;
                }
            }
        }
        // no more unused launch position
        logger.debug("no more unused launch position");
        return null;
    }
}
