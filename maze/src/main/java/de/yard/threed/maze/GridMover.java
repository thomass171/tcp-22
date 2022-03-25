package de.yard.threed.maze;

import de.yard.threed.core.Point;

import java.util.List;

/**
 * Abstraction of an element that moves/rotates in a grid, but no item (see {@GridItem}) that can be collected (put into inventory).
 * An equals() method here is very inconvenient and maybe useless.
 *
 * Independent from ECS.
 */
public interface GridMover {

    Point getLocation() ;

    /**
     * Zunaechst zum initialisieren
     * 24.4.21
     * @return
     */
    void setLocation(Point point);

    /**
     * Checks for validness.
     *
     * Returns true if relocated, false otherwise.
     */
    /*boolean relocate(Point point, GridOrientation gridOrientation, GridState gridState, MazeLayout mazeLayout);*/

    GridOrientation getOrientation() ;

    GridMovement rotate(boolean left) ;

    void setOrientation(GridOrientation gridOrientation);

    /**
     * Ein Movement versuchen. Als "Forward" nur Walk, keine Box schieben. Als "ForwardMove" mit push.
     * 20.5.21 Kann auch ein Relocate sein. Und auch ein "Kick/Pull". Muesste vielleicht mal umbenannt werden nach "act" oder execute? Oder activeMove/passiveMove?
     *
     * Liefert den neuen state, wenn der walk moeglich ist, sonst null.
     *
     * Moving in *some* direction which not needs to be the orientation.
     * 12.4.21: MA32: jetzt hier state changen ??
     * Might have effects like collecting items.
     */
    MoveResult move(GridMovement movement, GridOrientation gridOrientation, GridState gridState, MazeLayout mazeLayout) ;

    /**
     * Nicht ganz schoen.
     * @return
     */
    MoverComponent getParent();


    /**
     * Pruefen, ob ein Schritt in die Richtung gegangen werden kann.
     * Das geht nur, wenn es frei ist. Keine Wand und keine Box.
     *
     * Don't use own orientation here because it might be a push action.
     *
     * @return
     */
    //public boolean canWalk(GridMovement movement, GridOrientation gridOrientation, GridState gridState, MazeLayout mazeLayout);

    /**
     * push and relocate are also options.
     *
     * @return
     */
    List<GridMovement> getMoveOptions(GridState gridState, MazeLayout mazeLayout);

    /**
     * Some id that make the mover unique. Might be the entity id in ECS.
     */
    int getId();

    /**
     * might be null (eg. for boxes)
     * @return
     */
    Team getTeam();
}
