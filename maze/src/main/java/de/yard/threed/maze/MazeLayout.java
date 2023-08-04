package de.yard.threed.maze;

import de.yard.threed.core.Point;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;

import java.util.ArrayList;
import java.util.List;

/**
 * Static parts of a maze grid.
 * Somehow duplicate to {@link Grid}? Not really, grid also contains initial non static elements.
 *
 * <p>
 * 26.4.21: Isn't static/singleton because there might be multiple (multi level multi player? who knows)
 *
 * <p>
 * Created by thomass on 07.03.17.
 */
public class MazeLayout {
    Log logger = Platform.getInstance().getLog(MazeLayout.class);
    public List<Point> walls;
    public List<Point> destinations;
    // Several sets of start positions. Starting at lower (small y) left (small x).
    // also for monster
    private List<GridTeam> initialPosition;
    int maxwidth, height;
    // the inner fields that could be visited. No walls.
    private List<Point> fields;

    public MazeLayout(List<Point> walls, List<Point> destinations, List<GridTeam> initialPosition, int maxwidth, int height, List<Point> fields) {
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
     * Find a previously unused launch position for a new joining player. For now this is team independent. So a new player
     * will join a team randomly.
     * Might also return monster start locations.
     * @return
     */
    public StartPosition getNextLaunchPosition(List<Point> positionsToIgnore, boolean includeMonster) {
        for (GridTeam pset : initialPosition) {
            for (StartPosition p : pset.positions) {
                if (positionsToIgnore == null || !positionsToIgnore.contains(p.p)) {
                    if (!pset.isMonsterTeam || includeMonster) {
                        return p;
                    }
                }
            }
        }
        // no more unused launch position
        logger.debug("no more unused launch position");
        return null;
    }

    /**
     * The default orientation is 'North'. But if this results in facing a wall, turn clockwise until not facing a wall.
     * Facing a team member is OK.
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
        for (GridTeam l : initialPosition) {
            for (StartPosition point : l.positions) {
                if (point.p.equals(p)) {
                    return true;
                }
            }
        }
        return false;
    }

    public int getTeamByHome(Point p) {
        for (int team = 0; team < initialPosition.size(); team++) {
            for (StartPosition point : initialPosition.get(team).positions) {
                if (point.p.equals(p)) {
                    return team;
                }
            }
        }
        return -1;
    }

    public int getNonMonsterTeamByHome(Point p) {
        int idx = 0;
        for (GridTeam team : initialPosition) {
            if (!team.isMonsterTeam) {
                for (StartPosition point : team.positions) {
                    if (point.p.equals(p)) {
                        return idx;
                    }
                }
                idx++;
            }
        }
        return -1;
    }

    public int getStartPositionCount(boolean ignoreMonster) {
        int cnt = 0;
        for (int team = 0; team < initialPosition.size(); team++) {
            for (int i=0;i<initialPosition.get(team).positions.size();i++) {
                if (ignoreMonster == false || !initialPosition.get(team).isMonsterTeam) {
                    cnt++;
                }
            }
        }
        return cnt;
    }

    public List<GridTeam> getStartPositions() {
        return initialPosition;
    }

    public GridTeam getTeamByIndex(int teamId) {
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

    public int getMaxWidth() {

        return maxwidth;
    }

    public int getHeight() {
        return height;
    }


    public boolean isWall(Point p) {
        return walls.contains(p);
    }

    /**
     * Is it an inner field, a field that could be visited. No walls.
     */
    public boolean isField(Point p) {
        return getFields().contains(p);
    }

}
