package de.yard.threed.engine.ecs;

/**
 * A state of a game for example.
 * <p>
 * 18.11.21: Should be a system?
 * 8.8.23: Also for a client?
 */
public class SystemState {
    public static int STATE_READY_TO_JOIN = 10;
    public static int STATE_OVER = 30;

    public static int state = 0;

    public static boolean readyToJoin() {
        return state >= STATE_READY_TO_JOIN && state < STATE_OVER;
    }

    public static boolean isOver() {
        return state == STATE_OVER;
    }

    public static int getState() {
        return state;
    }

    public static String getStateAsString() {
        if (state == STATE_READY_TO_JOIN) {
            return "STATE_READY_TO_JOIN";
        }
        if (state == STATE_OVER) {
            return "STATE_OVER";
        }
        return "unknown state";
    }
}
