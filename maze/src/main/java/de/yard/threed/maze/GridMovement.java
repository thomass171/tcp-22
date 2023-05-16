package de.yard.threed.maze;

import de.yard.threed.core.Point;

/**
 * Eine Bewegung im Grid (quasi ein Zug).
 * <p>
 * 10.4.21: Ist das nicht Replay/StateEngine lastig und damit zumindest teilweise eher deprecated? (MA32)
 * 20.5.21: Ein Relocate dürfte auch ein GridMovement sein.
 * Created by thomass on 14.02.17.
 */
public class GridMovement {
    public static final int FORWARD = 1;
    public static final int TURNLEFT = 2;
    public static final int TURNRIGHT = 3;
    //Der ForwardMove wird zusaetzlich zum Forward gebraucht, da man sonst beim Undo nicht weiss,
    //ob eine Box zurueckgezogen werden muss.
    public static final int FORWARDMOVE = 4;
    public static final int BACK = 5;
    // piull gibt es nur, um den undo ohne Verrenkungen abbilden zu koennen.
    public static final int PULL = 6;
    // Left/Right sind in 3D Sicht praktisch. Die schieben aber genausowenig wie Back eine Box (also Box wird nicht geschoben).
    public static final int LEFT = 7;
    public static final int RIGHT = 8;
    public static final int RELOCATE = 9;
    public static final int KICK = 10;
    // Teleport is an own action that only is possible when not moving. Relocate is an extern action that is also possible while moving.
    public static final int TELEPORT = 11;
    public static GridMovement Forward = new GridMovement(FORWARD);
    public static GridMovement TurnLeft = new GridMovement(TURNLEFT);
    public static GridMovement TurnRight = new GridMovement(TURNRIGHT);
    // move forward by moving a box. 13.4.21: No longer, because this isType no single movement but needs coordination.
    // trotzdem. Wenn ein Mover Forward macht sollte er selber auch pushen können und damit FORWARDMOVE zurückliefern.
    public static GridMovement ForwardMove = new GridMovement(FORWARDMOVE);
    public static GridMovement Back = new GridMovement(BACK);
    // 27.5.21: Pull ist jetzt das Gegenstück zu kick, also ohne eigenes movement. Und damit auch nur begrenzt das undo Gegenstueck zu push.
    public static GridMovement Pull = new GridMovement(PULL);
    public static GridMovement Left = new GridMovement(LEFT);
    public static GridMovement Right = new GridMovement(RIGHT);
    // relocate and teleport need additional attribute target. So the constant here is private.
    private static GridMovement Relocate = new GridMovement(RELOCATE);
    private static GridMovement Teleport = new GridMovement(TELEPORT);
    public static GridMovement Kick = new GridMovement(KICK);
    //Back ist nicht in den moves, weil es als Teil einer Lösung sehr speziell wäre. Vor allem, wenn es ohne Grund
    //eingesetzt wird. Left/Right aber schon, das kann deutlich Turns sparen.
    public static GridMovement[] regularpossiblemoves = new GridMovement[]{Forward, ForwardMove, TurnLeft, TurnRight, Left, Right/*, Back*/};
    int movement;
    // relocatetarget is also for teleport.
    private Point relocateTarget = null;
    private GridOrientation relocateOrientation;

    public GridMovement(int movement) {
        this.movement = movement;
    }

    private GridMovement(int movement, Point relocateTarget, GridOrientation relocateOrientation) {
        this.movement = movement;
        this.relocateTarget = relocateTarget;
        this.relocateOrientation = relocateOrientation;
    }

    public static GridMovement buildRelocate(Point relocateTarget, GridOrientation relocateOrientation) {
        return new GridMovement(RELOCATE, relocateTarget, relocateOrientation);
    }

    public static GridMovement buildTeleport(Point relocateTarget, GridOrientation relocateOrientation) {
        return new GridMovement(TELEPORT, relocateTarget, relocateOrientation);
    }

    @Override
    public String toString() {
        switch (movement) {
            case 1:
                return "Forward";
            case 2:
                return "TurnLeft";
            case 3:
                return "TurnRight";
            case 4:
                return "ForwardMove";
            case 5:
                return "Back";
            case 6:
                return "Pull";
            case 7:
                return "Left";
            case 8:
                return "Right";
            case 9:
                return "Relocate " + relocateTarget;
            case 10:
                return "Kick";
            case 11:
                return "Teleport " + relocateTarget;
        }
        return "";
    }

    @Override
    public boolean equals(Object o) {
        return movement == ((GridMovement) o).movement;
    }

    public boolean isForward() {
        return movement == FORWARD;
    }

    public /*EventType*/Object getEvent() {
        switch (movement) {
            case 1:
                return MazeRequestRegistry.TRIGGER_REQUEST_FORWARD;
            case 2:
                return MazeRequestRegistry.TRIGGER_REQUEST_TURNLEFT;
            case 3:
                return MazeRequestRegistry.TRIGGER_REQUEST_TURNRIGHT;
            case 4:
                return MazeRequestRegistry.TRIGGER_REQUEST_FORWARDMOVE;
            case 5:
                return MazeRequestRegistry.TRIGGER_REQUEST_BACK;
            case 6:
                return MazeRequestRegistry.TRIGGER_REQUEST_PULL;
            case 7:
                return MazeRequestRegistry.TRIGGER_REQUEST_LEFT;
            case 8:
                return MazeRequestRegistry.TRIGGER_REQUEST_RIGHT;
        }
        throw new RuntimeException("unknown");
    }

    public boolean isRelocate() {
        return movement == RELOCATE;
    }

    public boolean isTeleport() {
        return movement == TELEPORT;
    }

    public boolean isKick() {
        return movement == KICK;
    }

    public boolean isPull() {
        return movement == PULL;
    }

    public Point getRelocateTarget() {
        return relocateTarget;
    }

    public GridOrientation getRelocateOrientation() {
        return relocateOrientation;
    }
}
