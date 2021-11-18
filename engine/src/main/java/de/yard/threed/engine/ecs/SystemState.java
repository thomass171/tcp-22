package de.yard.threed.engine.ecs;

/**
 * 18.11.21: Should be a system?
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
}
