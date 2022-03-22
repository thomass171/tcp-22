package de.yard.threed.maze;

import de.yard.threed.core.platform.Log;
import de.yard.threed.core.Point;
import de.yard.threed.core.platform.Platform;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstraction of an element that moves/rotates in a grid.
 * Independent from ECS.
 */
public class SimpleGridMover implements GridMover {

    Log logger = Platform.getInstance().getLog(SimpleGridMover.class);

    private Point location;
    private GridOrientation ownOrientation;
    private boolean debugmovement = false;
    private MoverComponent parent;
    // id to be used outside ECS.
    private int nonEcsId;
    private static int id = 1;
    private Team team;

    /**
     * Constructor for non ECS usage (testing, dry run).
     */
    public SimpleGridMover(Point location, GridOrientation orientation, Team team) {
        this.location = location;
        this.ownOrientation = orientation;
        this.nonEcsId = id++;
        this.team = team;
    }

    /**
     * Constructor for ECS usage.
     */
    public SimpleGridMover(Point location, GridOrientation orientation, MoverComponent parent, Team team) {
        this.location = location;
        this.ownOrientation = orientation;
        this.parent = parent;
        nonEcsId = -1;
        this.team = team;
    }

    public Point getLocation() {
        return location;
    }

    public void setLocation(Point p) {
        location = p;
    }

    public GridOrientation getOrientation() {
        return ownOrientation;
    }

    public GridMovement rotate(boolean left) {
        if (debugmovement) {
            logger.debug("setRotateStatus key was pressed. currentdelta=" /*+ tpf*/);
        }
        // 12.4.21: MA32: jetzt hier state changen
        ownOrientation = ownOrientation.rotate(left);

        if (left) {
            return GridMovement.TurnLeft;
        }
        return GridMovement.TurnRight;
    }

    @Override
    public void setOrientation(GridOrientation gridOrientation) {
        ownOrientation = gridOrientation;
    }

    /**
     * See interface.
     * <p>
     * <p>
     * 12.4.21: MA32: jetzt hier state changen
     */
    @Override
    public GridMovement move(GridMovement movement, GridOrientation gridOrientation, GridState gridState, MazeLayout mazeLayout) {

        // 22.5.21: Special case kick, that is possible from more far distance without self walking. Pull also.
        if (movement.equals(GridMovement.Kick) || movement.equals(GridMovement.Pull)) {
            GridMover nextBox = gridState.findNextBox(location, gridOrientation, mazeLayout);
            if (nextBox == null) {
                return null;
            }
            if (movement.equals(GridMovement.Kick)) {
                Point pushLocation = nextBox.getLocation().subtract(gridOrientation.getDirectionForMovement(GridMovement.Forward).getPoint());
                GridMover boxloc;
                if ((boxloc = canPushFrom(pushLocation, gridOrientation, gridState, mazeLayout)) != null) {
                    //GridMover boxloc = currentstate.canPushFrom(from, orientation, layout);
                    derivedMove(boxloc, gridOrientation, GridMovement.Forward, gridState, mazeLayout);
                    return GridMovement.Kick;
                    //}
                }
            }
            // a pull box might not be a neighbor.
            if (movement.equals(GridMovement.Pull) && Point.getDistance(location, nextBox.getLocation()) > 1) {
                derivedMove(nextBox, gridOrientation, GridMovement.Back, gridState, mazeLayout);
                return GridMovement.Pull;
            }
            return null;
        }

        if (!canWalk(movement, gridOrientation, gridState, mazeLayout)) {
            //15.4.21 try push if requested
            if (movement.equals(GridMovement.ForwardMove) && combinedMove(gridState, this, mazeLayout)) {
                return GridMovement.ForwardMove;
            }
            return null;
        }
        // so: do the move. If there is an item, collect it.
        Direction direction = gridOrientation.getDirectionForMovement(movement);
        GridItem item;
        if ((item = gridState.isItemAtDestination(location, direction, 1)) != null) {
            logger.debug("walk with collect of " + item);
            item.collectedBy(getId());
        }
        location = location.add(direction.getPoint());
        return movement;
    }

    @Override
    public MoverComponent getParent() {
        return parent;
    }


    /**
     * Check whether a step in the direction can be done.
     * Only possible if target field is not occupied (by box or other player) and no wall.
     * <p>
     * Don't use own orientation here because it might be a push action.
     * Also used for boxes that are about to be pushed?
     * @return
     */
    public boolean canWalk(GridMovement movement, GridOrientation gridOrientation, GridState gridState, MazeLayout mazeLayout) {
        Direction direction = gridOrientation.getDirectionForMovement(movement);
        if (GridState.isWallAtDestination(location, direction, 1, mazeLayout)) {
            logger.debug("cannot walk due to wall");
            return false;
        }
        GridMover b;
        if ((b = gridState.isBoxAtDestination(location, direction, 1)) != null) {
            logger.debug("cannot walk due to box");
            return false;
        }
        if ((b = gridState.isPlayerAtDestination(location, direction, 1)) != null) {
            logger.debug("cannot walk due to player");
            return false;
        }
        // don't check others for boxes (where team is null). At least for now
        if (getTeam() != null && gridState.isOtherHomeAtDestination(location, direction, 1, getTeam(), mazeLayout)) {
            logger.debug("cannot walk due to other home");
            return false;
        }
        logger.debug("canWalk true");
        return true;
    }

    /**
     * Check whether a move in direction of current orientation is possible by pushing a box.
     * Only possible if the field behind the box is empty.
     * <p>
     * Returns box that could be moved.
     */
    public GridMover canPushFrom(Point from, GridOrientation gridOrientation, GridState gridState, MazeLayout mazeLayout) {
        Direction direction = gridOrientation.getDirectionForMovement(GridMovement.Forward);

        if (GridState.isWallAtDestination(from, direction, 1, mazeLayout)) {
            return null;
        }
        GridMover b;
        if ((b = gridState.isBoxAtDestination(from, direction, 1)) == null) {
            return null;
        }
        if (GridState.isWallAtDestination(from, direction, 2, mazeLayout)) {
            return null;
        }
        //TODO also check for player behind box
        if (gridState.isBoxAtDestination(from, direction, 2) != null) {
            // dahinter steht eine Box
            return null;
        }
        //logger.debug("can move in direction. foward= " + forward);

        return b;
    }

    @Override
    public List<GridMovement> getMoveOptions(GridState gridState, MazeLayout mazeLayout) {

        ArrayList<GridMovement> result = new ArrayList<GridMovement>();
        result.add(GridMovement.TurnLeft);
        result.add(GridMovement.TurnRight);

        GridMovement[] candidates = new GridMovement[]{GridMovement.Forward, GridMovement.Left, GridMovement.Right, GridMovement.Back};
        for (GridMovement movement : candidates) {
            if (canWalk(movement, ownOrientation, gridState, mazeLayout)) {
                result.add(movement);
            }
        }
        if (canPushFrom(location, ownOrientation, gridState, mazeLayout) != null) {
            result.add(GridMovement.ForwardMove);
        }
        collectHorizontalMoveOptions(-1, gridState, mazeLayout, result);
        collectHorizontalMoveOptions(1, gridState, mazeLayout, result);
        collectVerticalMoveOptions(location, -1, gridState, mazeLayout, result);
        collectVerticalMoveOptions(location, 1, gridState, mazeLayout, result);

        return result;
    }

    @Override
    public int getId() {
        if (parent != null) {
            return parent.getId();
        } else {
            return nonEcsId;
        }
    }

    @Override
    public Team getTeam() {
        return team;
    }

    /**
     * Bei der zu schiebenden Box auch einen Walkstatus anlegen.
     * Movement ist Push oder - bei undo - Pull.
     * <p>
     * 13.4.21: Erst durch Schieben der Box das Feld freimachen. Und dann das ganze hier generisch und static.
     * 22.5.21: Optionally only move box, not player ("Kick")
     * <p>
     * box cannot be detected here, because it might be a MoverComponent.Mit parent doch.
     */
    private boolean combinedMove(GridState currentstate, GridMover player, /*GridMover/*Point* / boxloc,*/ MazeLayout layout) {
        GridMover boxloc = canPushFrom(location, ownOrientation, currentstate, layout);
        if (boxloc != null && boxloc.getParent() != null) {
            // Might be a MoverComponent (ECS)
            boxloc = boxloc.getParent();
        }
        logger.debug("combinedMove: boxloc=" + ((boxloc == null) ? "null" : boxloc.getLocation().toString()));
        if (boxloc != null) {

            boxloc.move(GridMovement.Forward, player.getOrientation(), currentstate, layout);
            player.move(GridMovement.Forward, player.getOrientation(), currentstate, layout);
            return true;
        }
        return false;
    }

    /**
     * @param boxloc
     * @param movement
     * @param orientation
     * @param currentstate
     * @param layout
     */
    private void/*boolean*/ derivedMove(GridMover boxloc/*Point from*/, GridOrientation orientation, GridMovement movement, GridState currentstate, MazeLayout layout) {
        if (!movement.equals(GridMovement.Forward) && !movement.equals(GridMovement.Back)) {
            throw new RuntimeException("invalid movement");
        }
        //GridMover boxloc = currentstate.canPushFrom(from, orientation, layout);
        if (boxloc != null && boxloc.getParent() != null) {
            // Might be a MoverComponent (ECS)
            boxloc = boxloc.getParent();
        }
        logger.debug("derivedMove: boxloc=" + ((boxloc == null) ? "null" : boxloc.getLocation().toString()));
        //if (boxloc != null) {

        boxloc.move(movement, orientation, currentstate, layout);
        //    return true;
        //}
        //return false;
    }

    /**
     * TODO emergency break for inconsistent grids
     */
    private void collectHorizontalMoveOptions(int offset, GridState gridState, MazeLayout mazeLayout, List<GridMovement> collector) {

        Point p = location.clone();
        while (true) {
            p.translateX(offset);
            if (!gridState.isFree(p, mazeLayout)) {
                return;
            }
            collector.add(new GridMovement(p.clone()));
            collectVerticalMoveOptions(p, -1, gridState, mazeLayout, collector);
            collectVerticalMoveOptions(p, 1, gridState, mazeLayout, collector);
        }
    }

    /**
     * TODO emergency break for inconsistent grids
     */
    private void collectVerticalMoveOptions(Point from, int yoffset, GridState gridState, MazeLayout mazeLayout, List<GridMovement> collector) {

        Point p = from.clone();
        while (true) {
            p.translateY(yoffset);
            if (!gridState.isFree(p, mazeLayout)) {
                return;
            }
            collector.add(new GridMovement(p.clone()));
        }
    }


}
