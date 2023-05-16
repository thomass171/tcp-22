package de.yard.threed.maze;

import de.yard.threed.core.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thomass on 19.01.16.
 */
public class GridPath {
    public List<Direction> steps = new ArrayList<Direction>();

    public GridPath() {

    }

    public int length() {
        return steps.size();
    }

    public void addStep(Direction p) {
        steps.add(p);
    }

    public void optimize() {
        while (optimize(this)) {
            ;
        }
    }

    /**
     * Pruefen, ob ein dreisschrittiger Umweg auf ein direktes Nachbarfeld rausoptimiert
     * werden kann.
     *
     * @param way
     * @return
     */
    private boolean optimize(GridPath way) {
        Point somepoint = new Point(5, 5);
        for (int i = 0; i < way.steps.size() - 4; i++) {
            Point p = somepoint;
            for (int j = 0; j < 3; j++) {
                p = Direction.add(p, way.steps.get(i + j));
            }
            if (MazeUtils.distance(somepoint, p) == 1) {
                // 16.5.23: "of" instead of substract
                Direction dir = Direction.of(p, somepoint);
                for (int j = 0; j < 3; j++) {
                    p = Direction.add(p, way.steps.remove(i));
                }
                way.steps.add(i, dir);
                return true;
            }
        }
        return false;
    }

}
