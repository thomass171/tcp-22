package de.yard.threed.maze;

import de.yard.threed.core.Point;
import de.yard.threed.core.Util;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.platform.Log;
import de.yard.threed.engine.platform.common.StringReader;
import de.yard.threed.core.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by thomass on 15.07.15.
 */
public class GridReader {
    Log logger = Platform.getInstance().getLog(GridReader.class);

    public List<Grid> readGrid(StringReader ins) throws InvalidMazeException {
        List<String> rows = new ArrayList<String>();
        List<GridDraft> drafts = new ArrayList<GridDraft>();

        String row;

        while ((row = ins.readLine()) != null) {
            if (row.equals("--")) {
                drafts.add(new GridDraft(revertRows(rows)));
                rows = new ArrayList<String>();
            } else {
                rows.add(row);
            }
        }
        drafts.add(new GridDraft(revertRows(rows)));

        List<Grid> grids = new ArrayList<Grid>();
        for (GridDraft draft : drafts) {
            grids.add(draft.readGrid());
        }
        //logger.debug("lines found: " + rows.size());
        //

        return grids;
    }

    /**
     * Umsortieren
     * Zeile 0 (die untere) soll auch an index 0 liegen
     * nicht in Unity Collections.reverse(rows);
     */
    private List<String> revertRows(List<String> rows) {
        List<String> trows = new ArrayList<String>();
        for (int i = rows.size() - 1; i >= 0; i--) {
            String r = rows.get(i);
            trows.add(r);
        }
        return trows;
    }
}

class GridDraft {
    List<String> rows;

    public GridDraft(List<String> rows) {
        this.rows = rows;
    }

    public Grid readGrid() throws InvalidMazeException {
        List<Point> walls;
        List<Point> destinations;
        List<Point> diamonds;
        List<Point> bots;
        List<Point> boxes;
        // everything not a wall
        List<Point> fields;
        // Several sets of start positions
        List<List<StartPosition>> playerposition = new ArrayList<List<StartPosition>>();
        int height;
        int maxwidth = 0;

        walls = new ArrayList<Point>();

        boxes = new ArrayList<Point>();
        destinations = new ArrayList<Point>();
        diamonds = new ArrayList<Point>();
        fields = new ArrayList<Point>();

        if (rows.size() < 3) {
            throw new InvalidMazeException("less than 3 rows:inconsistent grid?");
        }
        height = rows.size();

        Map<String, String> tags = new HashMap<String, String>();
        // Jetzt liegt das Grid als String array vor, das sich bequemer einlesen laesst
        // von unten (y=0) nach oben lesen.
        // Die Zeilen muessen nicht gleich lang sein.
        for (int y = 0; y < rows.size(); y++) {
            //logger.debug("y : " + y + ",len=" + StringUtils.length(rows.get(y)));
            List<GridElement> rowlist = new ArrayList<GridElement>();

            String row = rows.get(y);
            if (!collectTag(row, tags)) {
                int len = StringUtils.length(row);

                if (len > maxwidth) {
                    maxwidth = len;
                }
                for (int x = 0; x < StringUtils.length(row); x++) {
                    //GridElement ge;
                    char c = StringUtils.charAt(rows.get(y), x);
                    //logger.debug("c : " + c);
                    Point p = new Point(x, y);

                    switch (c) {
                        case '#':
                            walls.add(p);
                            break;
                        case ' ':
                            // TODO exclude outside
                            fields.add(p);
                            break;
                        case '*':
                            // box on target
                            destinations.add(p);
                            boxes.add(p);
                            fields.add(p);
                            break;
                        case 'T':
                        case '.':
                            destinations.add(p);
                            fields.add(p);
                            break;
                        case 'M':
                            addStartPosition(playerposition, p, true);
                            fields.add(p);
                            break;
                        case 'P':
                        case '@':
                            addStartPosition(playerposition, p, false);
                            fields.add(p);
                            break;
                        case 'B':
                        case '$':
                            boxes.add(p);
                            fields.add(p);
                            break;
                        case 'D':
                            diamonds.add(p);
                            fields.add(p);
                            break;
                        default:
                            throw new InvalidMazeException("invalid char " + c);
                    }
                }
            }
        }
        if (playerposition.size() == 0)
            throw new InvalidMazeException("no start position");
        //default heading is 'N'orth.
        MazeLayout mazeLayout = new MazeLayout(walls, destinations, playerposition, maxwidth, height, fields);
        Grid grid = new Grid(mazeLayout, boxes, diamonds, tags);

        return grid;

    }

    private boolean collectTag(String row, Map<String, String> tags) {
        String[] parts = StringUtils.split(row, "=");
        if (parts.length != 2) {
            return false;
        }
        String tag = StringUtils.toLowerCase(parts[0]);
        if (tag.equals("title")) {
            tags.put(tag, parts[1]);
            return true;
        }
        if (tag.equals("comment")) {
            tags.put(tag, parts[1]);
            return true;
        }
        return false;
    }

    private void addStartPosition(List<List<StartPosition>> playerposition, Point p, boolean isMonster) {
        // check extend existing team
        for (int team = 0; team < playerposition.size(); team++) {
            for (StartPosition point : playerposition.get(team)) {
                if (Point.getDistance(point.p, p) == 1) {
                    // same team start location
                    playerposition.get(team).add(new StartPosition(p, isMonster));
                    return;
                }
            }
        }
        // create new teams start positions
        playerposition.add(Util.buildList(new StartPosition(p, isMonster)));
    }
}