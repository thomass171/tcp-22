package de.yard.threed.maze;

import de.yard.threed.core.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * Dies und das
 *
 * 13.4.21
 */
public class MazeFactory {

    /**
     * For outside ECS only? Testing?
     * @param initialLocation
     * @return
     */
    public static GridMover buildMover(Point initialLocation){

        return new SimpleGridMover(initialLocation,new GridOrientation());
    }

    /**
     * For outside ECS only? Testing?
     *
     * @param initialLocation
     * @return
     */
    public static List<GridMover> buildMovers(List<Point> initialLocation){

        List<GridMover> mover = new ArrayList<GridMover>();
        for (Point p:initialLocation) {
            mover.add(new SimpleGridMover(p, new GridOrientation()));
        }
        return mover;
    }

    /**
     * For outside ECS only? Testing?
     *
     * @param initialLocation
     * @return
     */
    public static List<GridItem> buildDiamonds(List<Point> initialLocation){

        List<GridItem> items = new ArrayList<GridItem>();
        for (Point p:initialLocation) {
            items.add(new SimpleGridItem(p));
        }
        return items;
    }


}
