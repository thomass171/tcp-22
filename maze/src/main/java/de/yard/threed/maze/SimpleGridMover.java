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
    private GridOrientation ownOrientation = new GridOrientation();
    private boolean debugmovement = false;
    private MoverComponent parent;

    public SimpleGridMover(Point location, GridOrientation orientation) {
        this.location = location;
        this.ownOrientation = orientation;
    }

    public SimpleGridMover(Point location, GridOrientation orientation, MoverComponent parent) {
        this.location = location;
        this.ownOrientation = orientation;
        this.parent = parent;
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
    public GridMovement walk(GridMovement movement, GridOrientation gridOrientation, GridState gridState, MazeLayout mazeLayout) {

        // 22.5.21: kick ist ein Sonderfall, denn das geht auch aus der Ferne ohne eigenen Walk. Pull auch.
        if (movement.equals(GridMovement.Kick) || movement.equals(GridMovement.Pull)) {
            /*Point*/GridMover nextBox/*Location*/ = gridState.findNextBox/*Location*/(location, gridOrientation, mazeLayout);
            if (nextBox/*Location*/ == null) {
                return null;
            }
            if (movement.equals(GridMovement.Kick)) {
                Point pushLocation = nextBox.getLocation().subtract(gridOrientation.getDirectionForMovement(GridMovement.Forward).getPoint());
                GridMover boxloc;
                if ((boxloc = gridState.canPushFrom(pushLocation, gridOrientation, mazeLayout)) != null) {
                    //GridMover boxloc = currentstate.canPushFrom(from, orientation, layout);
                    derivedMove(boxloc, gridOrientation,GridMovement.Forward, gridState, mazeLayout);
                        return GridMovement.Kick;
                    //}
                }
            }
            // a pull box might not be a neighbor.
            if (movement.equals(GridMovement.Pull) && Point.getDistance(location,nextBox.getLocation()) > 1) {
                derivedMove(nextBox, gridOrientation,GridMovement.Back, gridState, mazeLayout);
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
        location = location.add(gridOrientation.getDirectionForMovement(movement).getPoint());
        return movement;
    }

    @Override
    public MoverComponent getParent() {
        return parent;
    }


    /**
     * Pruefen, ob ein Schritt in die Richtung gegangen werden kann.
     * Das geht nur, wenn es frei ist. Keine Wand und keine Box.
     * <p>
     * Don't use own orientation here because it might be a push action.
     *
     * @return
     */
    public boolean canWalk(GridMovement movement, GridOrientation gridOrientation, GridState gridState, MazeLayout mazeLayout) {
        Direction direction = gridOrientation.getDirectionForMovement(movement);
        if (gridState.isWallAtDestination(location, direction, 1, mazeLayout)) {
            logger.debug("cannot walk due to wall");
            return false;
        }
        GridMover b;
        if ((b = gridState.isBoxAtDestination(location, direction, 1)) != null) {
            logger.debug("cannot walk due to box");
            return false;
        }
        GridItem item;
        if ((item = gridState.isItemAtDestination(location, direction, 1)) != null) {
            logger.debug("walk with collect");
        }
        logger.debug("canWalk true");
        return true;
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
        if (gridState.canPush(mazeLayout) != null) {
            result.add(GridMovement.ForwardMove);
        }
        collectHorizontal(-1, gridState, mazeLayout, result);
        collectHorizontal(1, gridState, mazeLayout, result);
        collectVertical(location, -1, gridState, mazeLayout, result);
        collectVertical(location, 1, gridState, mazeLayout, result);

        return result;
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
        GridMover/*Point*/ boxloc = currentstate.canPush(layout);
        if (boxloc != null && boxloc.getParent() != null) {
            // Might be a MoverComponent (ECS)
            boxloc = boxloc.getParent();
        }
        logger.debug("combinedMove: boxloc=" + ((boxloc == null) ? "null" : boxloc.getLocation().toString()));
        if (boxloc != null) {

            boxloc.walk(GridMovement.Forward, player.getOrientation(), currentstate, layout);
            player.walk(GridMovement.Forward, player.getOrientation(), currentstate, layout);
            return true;
        }
        return false;
    }

    /**
     *
     * @param boxloc
     * @param movement
     * @param orientation
     * @param currentstate
     * @param layout
     */
    private void/*boolean*/ derivedMove(GridMover boxloc/*Point from*/,  GridOrientation orientation,GridMovement movement, GridState currentstate, MazeLayout layout) {
        if (!movement.equals(GridMovement.Forward) &&!movement.equals(GridMovement.Back)){
            throw new RuntimeException("invalid movement");
        }
        //GridMover boxloc = currentstate.canPushFrom(from, orientation, layout);
        if (boxloc != null && boxloc.getParent() != null) {
            // Might be a MoverComponent (ECS)
            boxloc = boxloc.getParent();
        }
        logger.debug("derivedMove: boxloc=" + ((boxloc == null) ? "null" : boxloc.getLocation().toString()));
        //if (boxloc != null) {

            boxloc.walk(movement, orientation, currentstate, layout);
        //    return true;
        //}
        //return false;
    }

    /**
     * TODO emergency break for inconsistent grids
     */
    private void collectHorizontal(int offset, GridState gridState, MazeLayout mazeLayout, List<GridMovement> collector) {

        Point p = location.clone();
        while (true) {
            p.translateX(offset);
            if (!gridState.isFree(p, mazeLayout)) {
                return;
            }
            collector.add(new GridMovement(p.clone()));
            collectVertical(p, -1, gridState, mazeLayout, collector);
            collectVertical(p, 1, gridState, mazeLayout, collector);
        }
    }

    /**
     * TODO emergency break for inconsistent grids
     */
    private void collectVertical(Point from, int yoffset, GridState gridState, MazeLayout mazeLayout, List<GridMovement> collector) {

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
