package de.yard.threed.maze;

import de.yard.threed.core.Point;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;

import java.util.ArrayList;
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
    private List<List<StartPosition>> initialPosition;
    //private static MazeLayout instance;
    int maxwidth, height;
    // the inner fields that could be visited. No walls.
    private List<Point> fields;

    public MazeLayout(List<Point> walls, List<Point> destinations, List<List<StartPosition>> initialPosition, int maxwidth, int height, List<Point> fields) {
        this.walls = walls;
        this.destinations = destinations;
        this.initialPosition = initialPosition;
        this.maxwidth = maxwidth;
        this.height = height;
        this.fields = fields;
    }


    public boolean isWallAt(Point p) {
        return walls.contains(p);
    }

    /**
     * Find a previously unused launch position. For now this is team independent. So a new player
     * will join a team randomly.
     */
    public Point getNextLaunchPosition(List<Point> positionsToIgnore) {
        for (List<StartPosition> pset : initialPosition) {
            for (StartPosition p : pset) {
                if (positionsToIgnore == null || !positionsToIgnore.contains(p.p)) {
                    return p.p;
                }
            }
        }
        // no more unused launch position
        logger.debug("no more unused launch position");
        return null;
    }

    /**
     * The default orientation is 'North'. But if this results in facing a wall, turn clockwise until not facing a wall.
     */
    public GridOrientation getInitialOrientation(Point launchPosition) {
        GridOrientation orientation = new GridOrientation();
        if (!GridState.isWallAtDestination(launchPosition, orientation.getDirection(), 1, this)) {
            return orientation;
        }
        orientation = orientation.rotate(false);
        if (!GridState.isWallAtDestination(launchPosition, orientation.getDirection(), 1, this)) {
            return orientation;
        }
        orientation = orientation.rotate(false);
        if (!GridState.isWallAtDestination(launchPosition, orientation.getDirection(), 1, this)) {
            return orientation;
        }
        // then just use 'West'.
        return orientation.rotate(false);
    }

    /**
     * If the number of teams is > 1 consider the game to be a battle game (with bullet firing, save home).
     */
    public int getNumberOfTeams() {
        return initialPosition.size();
    }

    public boolean isStartField(Point p) {
        for (List<StartPosition> l : initialPosition) {
            for (StartPosition point : l) {
                if (point.p.equals(p)) {
                    return true;
                }
            }
        }
        return false;
    }

    public int getTeamByHome(Point p) {
        for (int team = 0; team < initialPosition.size(); team++) {
            for (StartPosition point : initialPosition.get(team)) {
                if (point.p.equals(p)) {
                    return team;
                }
            }
        }
        return -1;
    }

    public int getStartPositionCount() {
        int cnt = 0;
        for (int team = 0; team < initialPosition.size(); team++) {
            cnt += initialPosition.get(team).size();
        }
        return cnt;
    }

    public List<List<StartPosition>> getStartPositions() {
        return initialPosition;
    }

    public List<StartPosition> getStartPositionsOfTeam(int teamId) {
        return initialPosition.get(teamId);
    }

    public List<Point> getFields() {
        return fields;
    }

    public List<Point> getWalls() {
        return walls;
    }

    public MazeLayout addWalls(List<Point> additionalWall) {
        List<Point> newWalls = new ArrayList<>();
        newWalls.addAll(walls);
        newWalls.addAll(additionalWall);
        return new MazeLayout(newWalls, this.destinations, this.initialPosition, this.maxwidth, this.height, this.fields);
    }

    public boolean isDestinationAt(Point p) {
        return destinations.contains(p);
    }
}
