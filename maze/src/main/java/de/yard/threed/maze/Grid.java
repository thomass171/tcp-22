package de.yard.threed.maze;


import de.yard.threed.core.Point;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.platform.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Representation of a maze grid loaded from a grid definition. Moving objects only have a start position here.
 * <p>
 * top is in Y-direction, right in X-direction
 * <p>
 * 26.4.21: Shouldn't be static, there can be multiple in tests and later during game play. A global grid is currently available
 * via provider in EcsSystem.
 * <p>
 * 16.4.23: Until today property 'name' is not needed.
 * 03.08.23: Can serialize back to a raw grid for reflecting modificator like 'teamsize'.
 * <p/>
 * Created by thomass on 15.07.15.
 */
public class Grid {

    MazeLayout layout;
    List<Point> boxes;
    List<Point> diamonds;
    // current grid in use? TODO: should have a better place
    static Grid instance;
    Map<String, String> tags;

    public Grid(MazeLayout layout, List<Point> boxes, List<Point> diamonds, Map<String, String> tags) throws InvalidMazeException {
        this.layout = layout;
        this.boxes = boxes;
        this.diamonds = diamonds;
        this.tags = tags;
        if (!GridValidator.hasClosedWallBoundary(this)) {
            throw new InvalidMazeException("no closed wall boundary");
        }
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
        for (GridTeam starts : layout.getStartPositions()) {
            for (StartPosition start : starts.positions) {
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

    /**
     * Needs to be rebuilt from meta data because of possible modificator.
     * Might differ from original in line length due to possible clipped blanks.
     */
    public String getRawGrid(String delimiter) {
        //C#    String[][] rawGrid = new String[layout.getHeight()][];
        String[][] rawGrid = new String[layout.getHeight()][layout.getMaxWidth()];
        for (int y = 0; y < layout.getHeight(); y++) {
            for (int x = 0; x < layout.getMaxWidth(); x++) {
                rawGrid[y][x] = " ";
            }
        }
        for (Point p : layout.getWalls()) {
            rawGrid[p.getY()][p.getX()] = "#";
        }
        for (Point p : getDiamonds()) {
            rawGrid[p.getY()][p.getX()] = "D";
        }
        for (Point p : getBoxes()) {
            rawGrid[p.getY()][p.getX()] = "B";
        }
        for (Point p : layout.destinations) {
            rawGrid[p.getY()][p.getX()] = ".";
        }
        for (GridTeam team : layout.getStartPositions()) {
            for (StartPosition sp : team.positions) {
                rawGrid[sp.getPoint().getY()][sp.getPoint().getX()] = team.isMonsterTeam ? "M" : "@";
            }
        }
        String s = "";
        for (int y = layout.getHeight() - 1; y >= 0; y--) {
            for (int x = 0; x < rawGrid[y].length; x++) {
                s += rawGrid[y][x];
            }
            s += delimiter;
        }
        return s;
    }

    /*public static List<Grid> loadByReader(StringReader ins) throws InvalidMazeException {
        return loadByReader(ins, null);
    }*/

    /*public static List<Grid> loadByReader(StringReader ins, String teamSize) throws InvalidMazeException {

        GridReader reader = new GridReader();
        return reader.readGrid(ins, teamSize);
    }*/



    /**
     * 15.2.23 TODO should be no static singleton
     *
     * @return
     */
    @Deprecated
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

    private static Log getLogger() {
        return Platform.getInstance().getLog(Grid.class);
    }
}
