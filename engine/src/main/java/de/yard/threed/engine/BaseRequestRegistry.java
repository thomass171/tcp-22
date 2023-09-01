package de.yard.threed.engine;

import de.yard.threed.core.Event;
import de.yard.threed.core.EventType;
import de.yard.threed.core.Payload;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.engine.platform.common.RequestType;


/**
 * Different to events it might be useful to define requests inside of systems.
 * But some still are not really related to a system.
 *
 * Some moving requests were moved from maze to here.
 */
public class BaseRequestRegistry {

    // 6 general moving requests (might be mapped to keyes w/a/s/d, CURLEFT;CURRIGHT,CURUP,CURDOWN
    public static RequestType TRIGGER_REQUEST_FORWARD = RequestType.register(2000, "TRIGGER_REQUEST_FORWARD");
    public static RequestType TRIGGER_REQUEST_BACK = RequestType.register(2001, "TRIGGER_REQUEST_BACK");
    public static RequestType TRIGGER_REQUEST_TURNLEFT = RequestType.register(2002, "TRIGGER_REQUEST_TURNLEFT");
    public static RequestType TRIGGER_REQUEST_TURNRIGHT = RequestType.register(2003, "TRIGGER_REQUEST_TURNRIGHT");

    public static RequestType TRIGGER_REQUEST_LEFT = RequestType.register(2011, "TRIGGER_REQUEST_LEFT");
    public static RequestType TRIGGER_REQUEST_RIGHT = RequestType.register(2012, "TRIGGER_REQUEST_RIGHT");

    public static Request buildForwardRequest(int userEntityId) {
        return new Request(TRIGGER_REQUEST_FORWARD, new Payload(),userEntityId);
    }
}
