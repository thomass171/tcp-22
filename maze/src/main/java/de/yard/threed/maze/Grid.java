package de.yard.threed.maze;


import de.yard.threed.core.Point;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.platform.Log;
import de.yard.threed.engine.platform.common.StringReader;

import java.util.List;
import java.util.Map;

/**
 * Die statischen Daten zu einem Maze Grid. Bewegende Objekte sind in dem Grid nur mit
 * ihrer Startposition abgebildet.
 * <p/>
 * Ein Pillar steht immer in der Mitte auf der Grenze zweier Gridfelder.
 * damit ist es durch die Position der beiden Felder immer eindeutig identifizierbar.
 * auf Ecken (da wo sich Wände kreuzen) steht kein Pillar.
 * top ist in Y-Richtung, right in X-Richtung
 * <p>
 * 9.4.21: Ist das nicht ein Mischmasch aus State und Visualization?
 * 26.4.21: Vielleicht mache ich den mal static? Aber nicht schoen wegen unabhaengiger Tests. TODO der static muss in ?? MazeScene? Oder als Provider ins System?
 * <p>
 * <p/>
 * Created by thomass on 15.07.15.
 */
public class Grid {
    Log logger = Platform.getInstance().getLog(Grid.class);
    // Zeile 0 ist die untere. An leeren Stellen steht ein BLANK Element, aber nie null
    //public List<List<GridElement>> grid;
    //12.4.21
    MazeLayout layout;
    List<Point> boxes;
    List<Point> diamonds;
    List<Point> bots;
    static Grid instance;
    Map<String, String> tags;

    static int STRAIGHTWALLMODE_NONE = 0;
    static int STRAIGHTWALLMODE_FULL = 1;
    static final int STRAIGHTWALLMODE_LOW_PART = 2;
    static final int STRAIGHTWALLMODE_HIGH_PART = 3;

    public Grid(MazeLayout layout, List<Point> boxes, List<Point> bots, List<Point> diamonds, Map<String, String> tags) {
        this.layout = layout;
        this.boxes = boxes;
        this.bots = bots;
        this.diamonds = diamonds;
        this.tags = tags;

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
       /* int currmax = 0;
        for (int i = 0; i < grid.size()/*length* /; i++)
            if (grid.get(i).size()/*[i].length* / > currmax) {
                currmax = grid.get(i).size()/*[i].length* /;
            }*/
        return layout.maxwidth;//currmax;
    }

    public int getHeight() {
        return layout.height;//grid.size()/*length*/;
    }

    public Point getStartPos() {
        return layout.initialPosition;
    }

    public int isVWALL(Point p) {
       /* if (el.getType() == GridElementType.VWALL) {
            return true;
        }*/
        // selber Block und drueber oder drunter aber nicht links oder rechts
        /*if (isWall(p) && (isWall(p.add(new Point(0, 1))) || isWall(p.add(new Point(0, -1))))
                && !isWall(p.add(new Point(-1, 0))) && !isWall(p.add(new Point(1, 0)))) {
            return true;
        }
        return false;*/
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


    /**
     * Sicher vor ArrayIndexOutOfBoundsException
     */
    /*private boolean isType(int x, int y, GridElementType type) {
        if (isValid(x, y)) {
            return (grid.get(y).get(x)/*[y][x]* /.getType() == type);
        }
        return false;
    }*/
    public boolean isWall(Point p) {
        /*if (isValid(x, y)) {
            return (grid.get(y).get(x).getType() == GridElementType.BLOCKWALL);
        }
        return false;*/
        return layout.walls.contains(p);
    }

    public boolean isField(Point p) {
        /*if (isValid(x, y)) {
            return (grid.get(y).get(x).getType() == GridElementType.BLOCKWALL);
        }
        return false;*/
        return layout.fields.contains(p);
    }

   /* private boolean isValid(int x, int y) {
        if (y < grid.size() && y >= 0) {
            if (x >= 0 && x < grid.get(y).size()) {
                return true;
            }
        }
        return false;
    }*/

    public boolean hasTopPillar(Point p) {
        boolean isblock = false;

        if (isWall(p)) {
            isblock = true;
        }

        // Wenn es selber Wall ist und darüber auch
        if (isblock && isWall(p.addY(1))) {
            return true;
        }
        // Auch wenn es selber Wall ist und unten auch und links/rechts nicht
        /*1.6.21 nicht mehr, dann ist es center if (isblock && isWall(p.add(new Point(0, -1))) && !isWall(p.add(new Point(-1, 0))) && !isWall(p.add(new Point(1, 0)))) {
            return true;
        }*/
        return false;
    }

    public boolean hasRightPillar(Point p) {

        boolean isblock = false;

        if (isWall(p)) {
            isblock = true;
        }
        // Wenn rechts vom Feld eine HWALL ist, hat es auch einen rightpillar
        /*if (isType(x + 1, y, GridElementType.HWALL)) {
            return true;
        }*/
        // Wenn es selber BLOCK ist und rechts auch
        if (isblock && isWall(p.addX(1))) {
            return true;
        }
        // 31.5.21: aber auch, wenn es das Ende einer Wall ist, also links eine, aber nicht oben und unten.
        /*3.6.21:Nee, dann ist nur center
        if (isblock && isWall(p.addX(-1)) && !isWall(p.addY(1)) && !isWall(p.addY(-1))) {
            return true;
        }*/
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

    /*MA32 public GridState getState() {
        
     
        return new GridState(reader.playerposition,  reader.boxes/*, reader.destinations* /);
    }*/

    public MazeLayout getLayout() {
        return layout;//new MazeLayout(reader.walls, reader.destinations, reader.playerposition, new GridOrientation());
    }

    public List<Point> getBoxes() {
        return boxes;
    }

    public List<Point> getDiamonds() {
        return diamonds;
    }

    public List<Point> getBots() {
        return bots;
    }
}
