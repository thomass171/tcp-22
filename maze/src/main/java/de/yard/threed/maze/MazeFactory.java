package de.yard.threed.maze;

import de.yard.threed.core.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory for grid elements (mover, items etc).
 * Not for 3D model building and visuals. No ECS.
 * See also {@MazeModelBuilder}
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

        return buildMover(new StartPosition(initialLocation, new GridOrientation()), -1);
    }

    public static GridMover buildMover(StartPosition startPosition, int team) {

        return new SimpleGridMover(startPosition, team);
    }

    /**
     * For outside ECS only? Testing?
     *
     * @param initialLocation
     * @return
     */
    public static List<GridMover> buildMovers(List<Point> initialLocation) {

        int team = -1;
        List<GridMover> mover = new ArrayList<GridMover>();
        for (Point p : initialLocation) {
            mover.add(new SimpleGridMover(new StartPosition(p, new GridOrientation()), team));
        }
        return mover;
    }

    /**
     * For outside ECS only? Testing?
     *
     * @param initialLocation
     * @return
     */
    public static List<GridItem> buildItems(List<Point> initialLocation, char type) {

        List<GridItem> items = new ArrayList<GridItem>();
        for (Point p : initialLocation) {
            SimpleGridItem item = new SimpleGridItem(p);
            switch (type) {
                case 'D':
                    item.setNeededForSolving();
                    break;
            }
            items.add(item);
        }
        return items;
    }
}
