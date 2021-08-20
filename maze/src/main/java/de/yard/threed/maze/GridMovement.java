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
    // relocate needs additonal attribute target.
    public static GridMovement Relocate = new GridMovement(RELOCATE);
    public static GridMovement Kick = new GridMovement(KICK);
    //Back ist nicht in den moves, weil es als Teil einer Lösung sehr speziell wäre. Vor allem, wenn es ohne Grund
    //eingesetzt wird. Left/Right aber schon, das kann deutlich Turns sparen.
    public static GridMovement[] regularpossiblemoves = new GridMovement[]{Forward, ForwardMove, TurnLeft, TurnRight, Left, Right/*, Back*/};
    int movement;
    public Point relocateTarget = null;

    public GridMovement(int movement) {
        this.movement = movement;
    }

    public GridMovement(Point relocateTarget) {
        this.movement = RELOCATE;
        this.relocateTarget = relocateTarget;
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
                return RequestRegistry.TRIGGER_REQUEST_FORWARD;
            case 2:
                return RequestRegistry.TRIGGER_REQUEST_TURNLEFT;
            case 3:
                return RequestRegistry.TRIGGER_REQUEST_TURNRIGHT;
            case 4:
                return RequestRegistry.TRIGGER_REQUEST_FORWARDMOVE;
            case 5:
                return RequestRegistry.TRIGGER_REQUEST_BACK;
            case 6:
                return RequestRegistry.TRIGGER_REQUEST_PULL;
            case 7:
                return RequestRegistry.TRIGGER_REQUEST_LEFT;
            case 8:
                return RequestRegistry.TRIGGER_REQUEST_RIGHT;
        }
        throw new RuntimeException("unknown");
    }

    public boolean isRelocate() {
        return movement == RELOCATE;
    }

    public boolean isKick() {
        return movement == KICK;
    }

    public boolean isPull() {
        return movement == PULL;
    }
}
