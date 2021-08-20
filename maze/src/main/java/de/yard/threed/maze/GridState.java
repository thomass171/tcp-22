package de.yard.threed.maze;

import de.yard.threed.core.Util;
import de.yard.threed.core.Point;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;

import java.util.ArrayList;
import java.util.List;

/**
 * Status (Standort) aller dynamischen Elemente (nach Abschluss der Bewegung).
 * Weils einfacher ist auch mit Waenden; das ganze Grid. Kommen jetzt doch von aussen.
 * Ganz ausgegoren wegen Konsistenz ist das noch nicht. Immer uebergebn ist auch unuebersichtlich.
 * Also jetzt drin.
 * Die Klasse ist damit im Grunde das gleiche wie "Grid".
 * 3.3.17: Ja, das ist die logische Abbildung des ganzen Maze unabhaengig von der 3D Darstellung (MazeTerrain?). ODer
 * sagen wir mal der dynamischen Teile.  Das macht aber keinen Sinn, die zu trennen.
 * 7.3.17: Doch, eigentlich schon. Jetzt sind die statischen in MazeLayout und MazeMovingAndStateSystem hält es zusammen.
 * 10.4.21: MA32: Die State Engine Idee mal deprecaten und mehr auf ECS setzen. Ein next State wird hier aber optional trotzdem ermittelt, aber lediglich fuers
 * Gaming nicht verwendet. Für Autosolver und testing ist das praktisch.
 * <p>
 * <p>
 * Created by thomass on 14.02.17.
 */
public class GridState {
    Log logger = Platform.getInstance().getLog(GridState.class);

    List<GridMover> boxes;
    List<GridItem> items;
    GridMover player;
    // Zur Optimierung. Moeglöich, weil sich ein State nicht ändern kann.
    private String asstring = null;

    public GridState(GridMover player, List<GridMover> boxes, List<GridItem> items) {
        this.player = player;
        this.boxes = boxes;
        this.items = items;
    }

    public GridState(GridMover player, List<GridMover> boxes) {
        this.player = player;
        this.boxes = boxes;
        this.items = new ArrayList<GridItem>();
    }

    /**
     * Pruefen, ob ein Schritt in die Richtung der aktuellen Orientierung durch schieben eines Blocks gegangen werden kann.
     * Das geht nur, wenn das Feld dahinter frei ist.
     * <p/>
     * Liefert die Box(position) zurück, die geschoben wird.
     * Ist nicht fuer pull geeignet.
     *
     * @return
     */
    public GridMover canPush(MazeLayout mazeLayout) {
        return canPushFrom(player.getLocation(), player.getOrientation(), mazeLayout);
    }

    public GridMover canPushFrom(Point from, GridOrientation orientation, MazeLayout mazeLayout) {
        Direction direction = orientation.getDirectionForMovement(GridMovement.Forward);

        if (isWallAtDestination(from, direction, 1, mazeLayout)) {
            return null;
        }
        GridMover/*Point*/ b;
        if ((b = isBoxAtDestination(from, direction, 1)) == null) {
            return null;
        }
        if (isWallAtDestination(from, direction, 2, mazeLayout)) {
            return null;
        }
        if (isBoxAtDestination(from, direction, 2) != null) {
            // dahinter steht eine Box
            return null;
        }
        //logger.debug("can move in direction. foward= " + forward);

        return b;
    }

    public GridState rotate(boolean left) {
        player.rotate(left);
        return new GridState(player, boxes, items);
    }

    /**
     * Nur Walk, keine Box schieben. Liefert den neuen state, wenn der walk moeglich ist, sonst null.
     *
     * @param movement
     * @return
     */
    GridState walk(GridMovement movement, MazeLayout mazeLayout) {
        if (player.walk(movement, player.getOrientation(), this, mazeLayout) != null) {
            //Point newposition = playerposition.add(playerorientation.getDirectionForMovement(movement).getPoint());
            return new GridState(player, boxes, items);
        }
        return null;
    }

    /**
     * Koordinaten der schiebbaren Box liefern. Sonst null.
     *
     * @return
     */
    /*Dann kann man auch direkt canMove aufrufen public Point getPushBoxLocation(MazeLayout mazeLayout /*List<Point> walls* /) {
        Point boxloc = null;

        if (mazeLayout != null && (boxloc = canMove(mazeLayout)) == null) {
            return null;
        }
        return boxloc;
    }*/

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

    /**
     * Eine Box ziehen.
     * <p>
     * Kann nur back sein. Zumindest sollte es nur so sein.
     *
     * @return
     */
    GridState pull(MazeLayout mazeLayout /*List<Point> walls*/) {
        Point boxloc;

        //TODO 13.4.21
        Util.nomore();
        return null;
        /*if (mazeLayout != null && (boxloc = canPull(mazeLayout)) == null) {
            return null;
        }
        Point boxposition = playerposition.add(playerorientation.getOrientation().getPoint());
        List<Point> newboxes = new ArrayList<Point>();
        newboxes.addAll(boxes);
        int index = newboxes.indexOf(boxposition);
        if (index == -1) {
            logger.error("box not found");
        }
        newboxes.set(index, boxposition.subtract(playerorientation.getOrientation().getPoint()));
        return new GridState(playerposition.subtract(playerorientation.getOrientation().getPoint()), playerorientation,/*, newboxes, /*walls,* / mazeLayout.destinations);
*/
    }

    /**
     * Liefert die Stelle wo die Box jetzt steht.
     *
     * @param mazeLayout
     * @return
     */
    /*MA32 public GridMover/*Point* / canPull(MazeLayout mazeLayout ) {
        Direction direction = player.getOrientation().getRevertedOrientation();
        if (isWallAtPlayerDestination(direction, 1, mazeLayout)) {
            return null;
        }
        GridMover/*Point* / b;
        if (isBoxAtPlayerDestination(direction, 1) != null) {
            return null;
        }
        if ((b = isBoxAtPlayerDestination(direction.getReverted(), 1)) == null) {
            // keine Box zu ziehen
            return null;
        }
        //logger.debug("can move in direction. foward= " + forward);
        return b;
    }*/


    /**
     * Das Element von der Player Position + steps*dir ermitteln.
     *
     * @return
     */
    /*private GridElement getElementAtPlayerDestination(boolean forward, int steps) {
        Point gridposition = Player.getGridPosition(movable.getPosition());
        Point direction = getDirection(forward);
        logger.debug("gridposition=" + gridposition + ",direction=" + direction + ",yaw=" + yaw);
        //return MazeScene.grid.grid.get(gridposition.y + steps * direction.y).get(gridposition.x + steps * direction.x);
        return new GridState(MazeScene.grid.grid).getElementAtDestination(gridposition,direction,steps);
    }*/
    public boolean isWallAtDestination(Point gridposition, Direction direction, int steps, MazeLayout mazeLayout /*List<Point> walls*/) {

        //logger.debug("gridposition=" + gridposition + ",direction=" + direction + ",yaw=" + /*yaw*/0);
        Point destination = gridposition.add(direction.multiply(steps));
        boolean wallloc = mazeLayout.isWallAt(destination);
        return wallloc;
    }

    /**
     * Liefert die Destination.
     *
     * @param direction
     * @param steps
     * @return
     */
    public GridMover/*Point*/ isBoxAtDestination(Point gridposition, Direction direction, int steps) {

        //logger.debug("gridposition=" + gridposition + ",direction=" + direction + ",yaw=" + /*yaw*/0);
        Point destination = gridposition.add(direction.multiply(steps));
        for (GridMover box : boxes) {
            if (box.getLocation().equals(destination)) {
                return box;//destination;
            }
        }
        return null;
    }

    public GridMover/*Point*/ isBoxAtLocation(Point location) {
        return MazeUtils.isBoxAtLocation(boxes, location);
    }

    /**
     * There can be only one item at a grid cell.
     */
    public GridItem isItemAtDestination(Point gridposition, Direction direction, int steps) {

        //logger.debug("gridposition=" + gridposition + ",direction=" + direction + ",yaw=" + /*yaw*/0);
        Point destination = gridposition.add(direction.multiply(steps));
        for (GridItem item : items) {
            if (item.getLocation() != null && item.getLocation().equals(destination)) {
                return item;
            }
        }
        return null;
    }

    /**
     * 14.421: Statt FORWARDMOVE einen combined move versuchen.
     *
     * @param movement
     * @return
     */
    public GridState execute(GridMovement movement, MazeLayout layout) {
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
    }

    @Override
    public String toString() {
        if (asstring == null) {
            String s = "";
            s = player.getLocation().toString();
            s += player.getOrientation().toString();
            for (GridMover p : boxes) {
                s += p.toString();
            }
            /*for (Point p : walls) {
                s += p.toString();
            }*/
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
     * Geloest ist es, wenn auf jeder Destination eine Box steht.
     *
     * @return
     */
    public static boolean isSolved(List<GridMover> boxes, MazeLayout mazeLayout) {
        if (boxes.size() == 0) {
            //No Sokoban
            //TODO check player on destination
            return false;
        }
        for (Point d : mazeLayout.destinations) {
            if (MazeUtils.isBoxAtLocation(boxes, d) == null) {
                return false;
            }
        }
        return true;
    }

    /**
     * Free in terms of relocate target.
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
        return true;
    }

    /**
     * Find next box in direction of orientation without obstacle.
     *
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
