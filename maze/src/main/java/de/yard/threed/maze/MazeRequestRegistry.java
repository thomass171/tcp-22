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
public class MazeRequestRegistry {
    /**
     *
     */
    public static RequestType TRIGGER_REQUEST_FORWARD = RequestType.register(2000,"TRIGGER_REQUEST_FORWARD");
    public static RequestType TRIGGER_REQUEST_BACK = RequestType.register(2001,"TRIGGER_REQUEST_BACK");
    public static RequestType TRIGGER_REQUEST_TURNLEFT = RequestType.register(2002,"TRIGGER_REQUEST_TURNLEFT");
    public static RequestType TRIGGER_REQUEST_TURNRIGHT = RequestType.register(2003,"TRIGGER_REQUEST_TURNRIGHT");

    public static RequestType TRIGGER_REQUEST_UNDO = RequestType.register(2004,"TRIGGER_REQUEST_UNDO");

    public static RequestType TRIGGER_REQUEST_AUTOSOLVE = RequestType.register(2005,"TRIGGER_REQUEST_AUTOSOLVE");

    public static RequestType MAZE_REQUEST_LOADLEVEL = RequestType.register(2006,"MAZE_REQUEST_LOADLEVEL");

    public static RequestType TRIGGER_REQUEST_VALIDATE = RequestType.register(2007,"TRIGGER_REQUEST_VALIDATE");

    public static RequestType TRIGGER_REQUEST_HELP = RequestType.register(2008,"TRIGGER_REQUEST_HELP");
    public static RequestType TRIGGER_REQUEST_RESET = RequestType.register(2009,"TRIGGER_REQUEST_RESET");

    @Deprecated
    public static RequestType TRIGGER_REQUEST_FORWARDMOVE = RequestType.register(2010,"TRIGGER_REQUEST_FORWARDMOVE");
    public static RequestType TRIGGER_REQUEST_LEFT = RequestType.register(2011,"TRIGGER_REQUEST_LEFT");
    public static RequestType TRIGGER_REQUEST_RIGHT = RequestType.register(2012,"TRIGGER_REQUEST_RIGHT");
    public static RequestType TRIGGER_REQUEST_PULL = RequestType.register(2013,"TRIGGER_REQUEST_PULL");


    // RELOCATE no longer for teleport: string payload: playername, logical target, orientation
    public static RequestType TRIGGER_REQUEST_RELOCATE = RequestType.register(2014,"TRIGGER_REQUEST_RELOCATE");
    public static RequestType TRIGGER_REQUEST_TELEPORT = RequestType.register(2015,"TRIGGER_REQUEST_TELEPORT");

    // Kick only applies to a box in direction of orientation
    public static RequestType TRIGGER_REQUEST_KICK = RequestType.register(2016,"TRIGGER_REQUEST_KICK");

    public static Request buildRelocate(int userEntityId, Point p, GridOrientation orientation) {
        return new Request(TRIGGER_REQUEST_RELOCATE, new Payload(new Integer(userEntityId), "" + p.getX() + "," + p.getY(), (orientation == null) ? "" : orientation.getDirectionCode()));
    }

    public static Request buildTeleport(int userEntityId, Point p, GridOrientation orientation) {
        return new Request(TRIGGER_REQUEST_TELEPORT, new Payload(new Integer(userEntityId), "" + p.getX() + "," + p.getY(), (orientation == null) ? "" : orientation.getDirectionCode()));
    }

    public static Request buildKick(int userEntityId) {
        return new Request(TRIGGER_REQUEST_KICK, new Payload(new Object[]{""}), new Integer(userEntityId));
    }

    public static RequestType TRIGGER_REQUEST_FIRE = RequestType.register(2017,"TRIGGER_REQUEST_FIRE");

    public static Request buildFireRequest(int userEntityId, Direction targetDirection) {
        return new Request(TRIGGER_REQUEST_FIRE, new Payload().add("targetdirection", targetDirection.getCode()), new Integer(userEntityId));
    }
}
