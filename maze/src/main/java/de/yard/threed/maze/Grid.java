package de.yard.threed.maze;


import de.yard.threed.core.Point;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.platform.Log;
import de.yard.threed.engine.platform.common.StringReader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Static data of a maze grid. Moving objects only have a start position here.
 * <p/>
 * <p>
 * - A pillar is always located in the mid on the boundary of two grid fields.
 * - There is no pillar at corners (where walls intersect) or on 'T's.
 * <p>
 * top is in Y-direction, right in X-direction
 * <p>
 * 26.4.21: Vielleicht mache ich den mal static? Aber nicht schoen wegen unabhaengiger Tests. TODO der static muss in ?? MazeScene? Oder als Provider ins System?
 * <p>
 * <p/>
 * Created by thomass on 15.07.15.
 */
public class Grid {
    Log logger = Platform.getInstance().getLog(Grid.class);
    MazeLayout layout;
    List<Point> boxes;
    List<Point> diamonds;
    // current grid in use? TODO: should have a better place
    static Grid instance;
    Map<String, String> tags;

    static int STRAIGHTWALLMODE_NONE = 0;
    static int STRAIGHTWALLMODE_FULL = 1;
    static final int STRAIGHTWALLMODE_LOW_PART = 2;
    static final int STRAIGHTWALLMODE_HIGH_PART = 3;

    public Grid(MazeLayout layout, List<Point> boxes, List<Point> diamonds, Map<String, String> tags) throws InvalidMazeException {
        this.layout = layout;
        this.boxes = boxes;
        this.diamonds = diamonds;
        this.tags = tags;
        if (!GridValidator.hasClosedWallBoundary(this)) {
            throw new InvalidMazeException("no closed wall boundary");
        }
    }

    public static List<Grid> loadByReader(StringReader ins) throws InvalidMazeException {

        GridReader reader = new GridReader();
        return reader.readGrid(ins);
    }

    public static Grid getInstance() {
        return instance;
    }

    /**
     * 26.4.21: muss static sein. Wo holen wir das bloss her.
     * TODO der static muss in ?? MazeScene? Oder als Provider ins System? Muss aber auch ohne System verfuegbar sein. Ein Dilemma.
     * Man kann die Aufrufe aber zumindest reduzieren/zentralisieren.
     *
     * @return
     */
    public static void setInstance(Grid grid) {
        instance = grid;
    }

    public static Grid findByTitle(List<Grid> grids, String s) {
        for (Grid grid : grids) {
            if (grid.hasTag("title", s)) {
                return grid;
            }
        }
        return null;
    }

    private boolean hasTag(String tag, String s) {
        String v = tags.get(tag);
        if (v != null && v.equals(s)) {
            return true;
        }
        return false;
    }

    public MazeLayout getMazeLayout() {
        return layout;
    }

    public boolean isMovePossible(GridPosition source, GridPosition destination) {
        // Moves gehen nur orthogonal. Ein Move nach ausserhalb wird durch die Aussenwaende
        // verhindert.
        // Zwischenkoordinaten von dem Feld, das fuer den Move ueberschritten werden muss.
        int midx, midy;
        if (source.x != destination.x) {
            if (source.y != destination.y) {
                return false;
            }
            // Nur Einzelschritte
            if (Math.abs(source.x - destination.x) != 2) {
                return false;
            }
            if (source.x > destination.x) {
                midx = destination.x + 1;
            } else {
                midx = source.x + 1;
            }
            // beide y sind ja gleich
            midy = source.y;
        }
        /*if (grid[midx][midy].iswall){
            return false;
        }*/
        return true;
    }

    public int getMaxWidth() {

        return layout.maxwidth;
    }

    public int getHeight() {
        return layout.height;
    }

    public int isVWALL(Point p) {

        if (!isWall(p)) {
            return STRAIGHTWALLMODE_NONE;
        }
        if (isWall(p.addX(-1)) || isWall(p.addX(1))) {
            return STRAIGHTWALLMODE_NONE;
        }
        boolean high = isWall(p.addY(1));
        boolean low = isWall(p.addY(-1));
        if (high && low) {
            return STRAIGHTWALLMODE_FULL;
        }
        if (high) {
            return STRAIGHTWALLMODE_HIGH_PART;
        }
        if (low) {
            return STRAIGHTWALLMODE_LOW_PART;
        }

        return STRAIGHTWALLMODE_NONE;
    }

    public int isHWALL(Point p) {

        // selber Block und links oder rechts aber nicht drüber oder drunter
        if (!isWall(p)) {
            return STRAIGHTWALLMODE_NONE;
        }
        if (isWall(p.addY(-1)) || isWall(p.addY(1))) {
            return STRAIGHTWALLMODE_NONE;
        }
        boolean high = isWall(p.addX(1));
        boolean low = isWall(p.addX(-1));
        if (high && low) {
            return STRAIGHTWALLMODE_FULL;
        }
        if (high) {
            return STRAIGHTWALLMODE_HIGH_PART;
        }
        if (low) {
            return STRAIGHTWALLMODE_LOW_PART;
        }

        return STRAIGHTWALLMODE_NONE;
    }

    public boolean isWall(Point p) {
        return layout.walls.contains(p);
    }

    /**
     * Is it an inner field, a field that could be visited. No walls.
     */
    public boolean isField(Point p) {
        return layout.getFields().contains(p);
    }

    /**
     * Does the wall continue at top?
     * <p>
     * 31.5.21: Also when it is a wall and beyond, but not to the left or right?
     * 1.6.21: No, then it is a center
     */
    public boolean hasTopPillar(Point p) {
        boolean isblock = false;

        if (isWall(p)) {
            isblock = true;
        }

        // Wenn es selber Wall ist und darüber auch
        if (isblock && isWall(p.addY(1))) {
            return true;
        }
        return false;
    }

    /**
     * Does the wall continue to the right?
     * <p>
     * 31.5.21: Also when it is the end of a wall, ie wall to the left but not above and beyond?
     * 3.6.21: No, then it is a center
     */
    public boolean hasRightPillar(Point p) {

        boolean isblock = false;

        if (isWall(p)) {
            isblock = true;
        }
        // Wenn es selber BLOCK ist und rechts auch
        if (isblock && isWall(p.addX(1))) {
            return true;
        }
        return false;
    }

    /**
     * Alle nicht durchgehenden Walls (also endende) haben einen center pillar. Ausser alleinstehende.
     *
     * @return
     */
    public boolean hasCenterPillar(Point p) {

        int surroundingwalls = 0;

        if (!isWall(p)) {
            return false;
        }
        if (isWall(p.addX(1))) {
            surroundingwalls++;
        }
        if (isWall(p.addX(-1))) {
            surroundingwalls++;
        }
        if (isWall(p.addY(1))) {
            surroundingwalls++;
        }
        if (isWall(p.addY(-1))) {
            surroundingwalls++;
        }
        return surroundingwalls == 1;
    }

    public List<Point> getBoxes() {
        return boxes;
    }

    public List<Point> getDiamonds() {
        return diamonds;
    }

    /**
     * Unused fields are inner fields without any purpose.
     * start/destination are also used fields.
     *
     * @return
     */
    public List<Point> getUnusedFields() {
        List<Point> unusedFields = new ArrayList<Point>();
        unusedFields.addAll(layout.getFields());
        unusedFields.removeAll(boxes);
        unusedFields.removeAll(diamonds);
        for (List<StartPosition> starts : layout.getStartPositions()) {
            for (StartPosition start : starts) {
                unusedFields.remove(start.p);
            }
        }
        unusedFields.removeAll(layout.destinations);
        return unusedFields;
    }

    public List<Point> getWallNeighbors(Point point) {
        List<Point> wallNeighbors = new ArrayList<Point>();
        for (Point p : layout.walls) {
            if (Point.getDistance(p, point) == 1) {
                wallNeighbors.add(p);
            }
        }
        return wallNeighbors;
    }

}
