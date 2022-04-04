package de.yard.threed.maze;

import de.yard.threed.core.Util;
import de.yard.threed.core.Point;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper container of a temporary state (locations) of all dynamic elements (after movement completed) independent of 3D visualization.
 * Non temporary states are stored in the mover elements. Static elements are in MazeLayout.
 *
 * <p>
 * Created by thomass on 14.02.17.
 */
public class GridState {
    Log logger = Platform.getInstance().getLog(GridState.class);

    List<GridMover> boxes;
    // Collected items are not contained here. Why not? They just have no location. But they are dynamic elements that belong to a state.
    // But we might splits bullets and diamonds like we split player and boxes.
    List<GridItem> items;
    // bots and monster also also player
    List<GridMover> players;

    // Optimization. Possible, because a state cannot change.
    private String asstring = null;

    public GridState(List<GridMover> players, List<GridMover> boxes, List<GridItem> items) {
        this.players = players;
        this.boxes = boxes;
        this.items = items;
    }

    public GridState(List<GridMover> players, List<GridMover> boxes) {
        this.players = players;
        this.boxes = boxes;
        this.items = new ArrayList<GridItem>();
    }

    /**
     * Eine Box schieben. Coordinated operation of moving a box and the player. Really needed here?
     * <p>
     * Kann nur forward sein. Zumindest sollte es nur so sein.
     * Liefert den neuen state, wenn der push moeglich ist, sonst null.
     *
     * @return
     */
    GridState push(MazeLayout mazeLayout /*List<Point> walls*/) {
        Point boxloc;

        //TODO 13.4.21
        Util.nomore();
        return null;
        /*
        if (mazeLayout != null && (boxloc = canMove(/*true,* / mazeLayout)) == null) {
            return null;
        }
        Point boxposition = playerposition.add(playerorientation.getOrientation().getPoint());
        List<Point> newboxes = new ArrayList<Point>();
        newboxes.addAll(boxes);
        int index = newboxes.indexOf(boxposition);
        if (index == -1) {
            logger.error("box not found");
        }
        newboxes.set(index, boxposition.add(playerorientation.getOrientation().getPoint()));
        return new GridState(playerposition.add(playerorientation.getOrientation().getPoint()), playerorientation/*, newboxes/*, walls* /, mazeLayout.destinations);
*/
    }

    /**
     * Koordinaten der ziehbaren Box liefern. Sonst null.
     *
     * @return
     */
    public Point getPullBoxLocation(MazeLayout mazeLayout /*List<Point> walls*/) {

        Util.nomore();
        return null;
        /*MA32
        Point boxloc = null;

        if (mazeLayout != null && (boxloc = canPull(mazeLayout)) == null) {
            return null;
        }
        return boxloc;*/
    }

    public static boolean isWallAtDestination(Point gridposition, Direction direction, int steps, MazeLayout mazeLayout) {

        //logger.debug("gridposition=" + gridposition + ",direction=" + direction + ",yaw=" + /*yaw*/0);
        Point destination = gridposition.add(direction.multiply(steps));
        boolean wallloc = mazeLayout.isWallAt(destination);
        return wallloc;
    }

    /**
     * Returns the box or null.
     */
    public GridMover isBoxAtDestination(Point gridposition, Direction direction, int steps) {
        return isMoverAtDestination(gridposition, direction, steps, boxes);
    }

    /**
     * Returns the box or null.
     */
    public GridMover isPlayerAtDestination(Point gridposition, Direction direction, int steps) {
        return isMoverAtDestination(gridposition, direction, steps, players);
    }

    /**
     * Returns the move or null.
     */
    private GridMover isMoverAtDestination(Point gridposition, Direction direction, int steps, List<GridMover> movers) {

        //logger.debug("gridposition=" + gridposition + ",direction=" + direction + ",yaw=" + /*yaw*/0);
        Point destination = gridposition.add(direction.multiply(steps));
        for (GridMover mover : movers) {
            if (mover.getLocation().equals(destination)) {
                return mover;
            }
        }
        return null;
    }

    public boolean isOtherHomeAtDestination(Point gridposition, Direction direction, int steps, Team myTeam, MazeLayout mazeLayout) {
        Point destination = gridposition.add(direction.multiply(steps));
        int teamIdOfDestination = mazeLayout.getTeamByHome(destination);
        if (teamIdOfDestination == -1) {
            return false;
        }
        return teamIdOfDestination != myTeam.id;
    }

    /**
     * There can be only one item at a grid cell.
     */
    public GridItem isItemAtDestination(Point gridposition, Direction direction, int steps) {

        //logger.debug("gridposition=" + gridposition + ",direction=" + direction + ",yaw=" + /*yaw*/0);
        Point destination = gridposition.add(direction.multiply(steps));
        return isItemALocation(destination);
    }

    public GridItem isItemALocation(Point location) {
        for (GridItem item : items) {
            if (item.getLocation() != null && item.getLocation().equals(location)) {
                return item;
            }
        }
        return null;
    }

    /**
     * Check whether a step in the direction can be done.
     * Only possible if target field is not occupied (by box or other player) and no wall.
     * <p>
     * Don't use own orientation here because it might be a push action.
     * Also used for boxes that are about to be pushed?
     *
     * @return
     */
    public boolean canWalk(Point location, GridMovement movement, GridOrientation gridOrientation, Team team, MazeLayout mazeLayout) {
        Direction direction = gridOrientation.getDirectionForMovement(movement);
        if (GridState.isWallAtDestination(location, direction, 1, mazeLayout)) {
            logger.debug("cannot walk due to wall");
            return false;
        }
        GridMover b;
        if ((b = isBoxAtDestination(location, direction, 1)) != null) {
            logger.debug("cannot walk due to box");
            return false;
        }
        if ((b = isPlayerAtDestination(location, direction, 1)) != null) {
            logger.debug("cannot walk due to player");
            return false;
        }
        // don't check others for boxes (where team is null). At least for now
        if (team != null && isOtherHomeAtDestination(location, direction, 1, team, mazeLayout)) {
            logger.debug("cannot walk due to other home");
            return false;
        }
        logger.debug("canWalk true");
        return true;
    }

    /**
     * 14.421: Statt FORWARDMOVE einen combined move versuchen.
     *
     * @return
     */
    /*public GridState execute(GridMovement movement, MazeLayout layout) {
        switch (movement.movement) {
            case GridMovement.FORWARD:
                if (walk(GridMovement.Forward, layout) != null) {
                    // return GridMovement.FORWARD;
                }
                //9.8.21 der break fehlte doch
                break;
            case GridMovement.BACK:
                return walk(GridMovement.Back, layout);
            case GridMovement.FORWARDMOVE:
                return push(layout);
            case GridMovement.TURNLEFT:
                return rotate(true);
            case GridMovement.TURNRIGHT:
                return rotate(false);
            case GridMovement.LEFT:
                return walk(GridMovement.Left, layout);
            case GridMovement.RIGHT:
                return walk(GridMovement.Right, layout);
        }
        logger.error("unknown move");
        return null;
    }*/
    @Override
    public String toString() {
        if (asstring == null) {
            String s = "";
            for (GridMover p : players) {
                s += p.toString();
            }
            for (GridMover p : boxes) {
                s += p.toString();
            }
            asstring = s;
        }
        return asstring;
    }

    @Override
    public boolean equals(Object o) {
        String s1 = this.toString();
        String s2 = (((GridState) o).toString());
        return s1.equals(s2);
    }

    /*@Override
    public int hashCode(){
        // calculate has hashcode
        if (asstring == null) {
            toString()
        
    }*/

    /**
     * @return
     */
    public boolean isSolved(MazeLayout mazeLayout) {

        boolean itemsNeededToCollect = false;
        boolean allItemsNeededCollected = true;
        for (GridItem item : items) {
            if (item.isNeededForSolving()) {
                itemsNeededToCollect = true;
                if (item.getOwner() == -1) {
                    allItemsNeededCollected = false;
                }
            }
        }

        if (boxes.size() == 0) {
            //No Sokoban
            if (itemsNeededToCollect) {
                if (allItemsNeededCollected) {
                    return true;
                }
            } else {
                // just a maze
                for (Point d : mazeLayout.destinations) {
                    if (MazeUtils.getMoverFromListAtLocation(players, d) == null) {
                        return false;
                    }
                }
                return true;
            }
            return false;
        } else {
            // Sokoban style. Solved if on any destination there is a box
            for (Point d : mazeLayout.destinations) {
                if (MazeUtils.getMoverFromListAtLocation(boxes, d) == null) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Free in terms of relocate target. That is:
     * - no player or box on field
     * - no wall
     *
     * @param p
     * @return
     */
    public boolean isFree(Point p, MazeLayout mazeLayout) {
        if (mazeLayout.isWallAt(p)) {
            return false;
        }

        for (GridMover box : boxes) {
            if (box.getLocation().equals(p)) {
                return false;
            }
        }
        for (GridMover player : players) {
            if (player.getLocation().equals(p)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Find next box in direction of orientation without obstacle.
     * <p>
     * Independent from move opportunities.
     */
    public GridMover findNextBox(Point from, GridOrientation gridOrientation, MazeLayout mazeLayout) {

        Point p = from;
        Direction direction = gridOrientation.getDirectionForMovement(GridMovement.Forward);

        //TODO emergency break
        while (true) {

            GridMover b;
            if ((b = isBoxAtDestination(p, direction, 1)) != null) {
                return b;
            }

            p = p.add(gridOrientation.getDirectionForMovement(GridMovement.Forward).getPoint());

            if (!isFree(p, mazeLayout)) {
                return null;
            }

        }
        //return null;
    }
}
