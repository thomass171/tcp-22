package de.yard.threed.maze;

import de.yard.threed.core.Point;
import de.yard.threed.core.Payload;
import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.engine.platform.common.RequestType;

/**
 * Um Requests nicht im System zu definieren, weil sie ja auch übergreifend verwendet werden koennten.
 * <p>
 * Created on 24.10.20.
 */
public class RequestRegistry {
    /**
     *
     */
    public static RequestType TRIGGER_REQUEST_FORWARD = new RequestType("TRIGGER_REQUEST_FORWARD");
    public static RequestType TRIGGER_REQUEST_BACK = new RequestType("TRIGGER_REQUEST_BACK");
    public static RequestType TRIGGER_REQUEST_TURNLEFT = new RequestType("TRIGGER_REQUEST_TURNLEFT");
    public static RequestType TRIGGER_REQUEST_TURNRIGHT = new RequestType("TRIGGER_REQUEST_TURNRIGHT");

    public static RequestType TRIGGER_REQUEST_UNDO = new RequestType("TRIGGER_REQUEST_UNDO");

    public static RequestType TRIGGER_REQUEST_AUTOSOLVE = new RequestType("TRIGGER_REQUEST_AUTOSOLVE");

    public static RequestType MAZE_REQUEST_LOADLEVEL = new RequestType("MAZE_REQUEST_LOADLEVEL");

    public static RequestType TRIGGER_REQUEST_VALIDATE = new RequestType("TRIGGER_REQUEST_VALIDATE");

    public static RequestType TRIGGER_REQUEST_HELP = new RequestType("TRIGGER_REQUEST_HELP");
    public static RequestType TRIGGER_REQUEST_RESET = new RequestType("TRIGGER_REQUEST_RESET");

    @Deprecated
    public static RequestType TRIGGER_REQUEST_FORWARDMOVE = new RequestType("TRIGGER_REQUEST_FORWARDMOVE");
    public static RequestType TRIGGER_REQUEST_LEFT = new RequestType("TRIGGER_REQUEST_LEFT");
    public static RequestType TRIGGER_REQUEST_RIGHT = new RequestType("TRIGGER_REQUEST_RIGHT");
    public static RequestType TRIGGER_REQUEST_PULL = new RequestType("TRIGGER_REQUEST_PULL");

    public static RequestType TRIGGER_REQUEST_FIRE = new RequestType("TRIGGER_REQUEST_FIRE");
    // RELOCATE also for teleport: string payload: playername, logical target, orientation
    public static RequestType TRIGGER_REQUEST_RELOCATE = new RequestType("TRIGGER_REQUEST_RELOCATE");
    public static RequestType TRIGGER_REQUEST_KICK = new RequestType("TRIGGER_REQUEST_KICK");

    public static Request buildRelocate(int userEntityId, Point p, String orientation) {
        return new Request(TRIGGER_REQUEST_RELOCATE, new Payload(new Integer(userEntityId), "" + p.getX() + "," + p.getY(), orientation));
    }

    public static Request buildKick(int userEntityId) {
        return new Request(TRIGGER_REQUEST_KICK, new Payload(""), new Integer(userEntityId));
    }
}
