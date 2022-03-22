package de.yard.threed.maze;

import de.yard.threed.core.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * Dies und das
 * <p>
 * 13.4.21
 */
public class MazeFactory {

    /**
     * For outside ECS only? Testing?
     *
     * @param initialLocation
     * @return
     */
    public static GridMover buildMover(Point initialLocation) {

        return buildMover(initialLocation, new GridOrientation(), null);
    }

    public static GridMover buildMover(Point initialLocation, GridOrientation initialOrientation, Team team) {

        return new SimpleGridMover(initialLocation, initialOrientation, team);
    }

    /**
     * For outside ECS only? Testing?
     *
     * @param initialLocation
     * @return
     */
    public static List<GridMover> buildMovers(List<Point> initialLocation) {

        Team team = null;
        List<GridMover> mover = new ArrayList<GridMover>();
        for (Point p : initialLocation) {
            mover.add(new SimpleGridMover(p, new GridOrientation(), team));
        }
        return mover;
    }

    /**
     * For outside ECS only? Testing?
     *
     * @param initialLocation
     * @return
     */
    public static List<GridItem> buildDiamonds(List<Point> initialLocation) {

        List<GridItem> items = new ArrayList<GridItem>();
        for (Point p : initialLocation) {
            items.add(new SimpleGridItem(p));
        }
        return items;
    }


}
