package de.yard.threed.engine;

import de.yard.threed.core.Payload;
import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.engine.platform.common.RequestType;


/**
 * Different to events it might be useful to define requests inside of systems.
 * But some still are not really related to a system.
 * <p>
 * Some moving requests were moved from maze to here.
 * 12.5.25: What does prefix 'TRIGGER_' mean?
 */
public class BaseRequestRegistry {

    // 10 general moving requests (might be mapped to keys w/a/s/d, CURLEFT;CURRIGHT,CURUP,CURDOWN
    // Values up to 1016 seem to be in use already. Start a new cycle from 1100.
    public static RequestType TRIGGER_REQUEST_FORWARD = RequestType.register(1100, "TRIGGER_REQUEST_FORWARD");
    public static RequestType TRIGGER_REQUEST_BACK = RequestType.register(1101, "TRIGGER_REQUEST_BACK");
    public static RequestType TRIGGER_REQUEST_TURNLEFT = RequestType.register(1102, "TRIGGER_REQUEST_TURNLEFT");
    public static RequestType TRIGGER_REQUEST_TURNRIGHT = RequestType.register(1103, "TRIGGER_REQUEST_TURNRIGHT");
    public static RequestType TRIGGER_REQUEST_TURNUP = RequestType.register(1104, "TRIGGER_REQUEST_TURNUP");
    public static RequestType TRIGGER_REQUEST_TURNDOWN = RequestType.register(1105, "TRIGGER_REQUEST_TURNDOWN");
    public static RequestType TRIGGER_REQUEST_ROLLLEFT = RequestType.register(1106, "TRIGGER_REQUEST_ROLLLEFT");
    public static RequestType TRIGGER_REQUEST_ROLLRIGHT = RequestType.register(1107, "TRIGGER_REQUEST_ROLLRIGHT");
    public static RequestType TRIGGER_REQUEST_LEFT = RequestType.register(1108, "TRIGGER_REQUEST_LEFT");
    public static RequestType TRIGGER_REQUEST_RIGHT = RequestType.register(1109, "TRIGGER_REQUEST_RIGHT");

    public static RequestType TRIGGER_REQUEST_START_FORWARD = RequestType.register(1110, "TRIGGER_REQUEST_START_FORWARD");
    public static RequestType TRIGGER_REQUEST_STOP_FORWARD = RequestType.register(1111, "TRIGGER_REQUEST_STOP_FORWARD");
    public static RequestType TRIGGER_REQUEST_START_BACK = RequestType.register(1112, "TRIGGER_REQUEST_START_BACK");
    public static RequestType TRIGGER_REQUEST_STOP_BACK = RequestType.register(1113, "TRIGGER_REQUEST_STOP_BACK");
    public static RequestType TRIGGER_REQUEST_START_TURNLEFT = RequestType.register(1114, "TRIGGER_REQUEST_START_TURNLEFT");
    public static RequestType TRIGGER_REQUEST_STOP_TURNLEFT = RequestType.register(1115, "TRIGGER_REQUEST_STOP_TURNLEFT");
    public static RequestType TRIGGER_REQUEST_START_TURNRIGHT = RequestType.register(1116, "TRIGGER_REQUEST_START_TURNRIGHT");
    public static RequestType TRIGGER_REQUEST_STOP_TURNRIGHT = RequestType.register(1117, "TRIGGER_REQUEST_STOP_TURNRIGHT");
    public static RequestType TRIGGER_REQUEST_START_TURNUP = RequestType.register(1118, "TRIGGER_REQUEST_START_TURNUP");
    public static RequestType TRIGGER_REQUEST_STOP_TURNUP = RequestType.register(1119, "TRIGGER_REQUEST_STOP_TURNUP");
    public static RequestType TRIGGER_REQUEST_START_TURNDOWN = RequestType.register(1120, "TRIGGER_REQUEST_START_TURNDOWN");
    public static RequestType TRIGGER_REQUEST_STOP_TURNDOWN = RequestType.register(1121, "TRIGGER_REQUEST_STOP_TURNDOWN");
    public static RequestType TRIGGER_REQUEST_START_ROLLLEFT = RequestType.register(1122, "TRIGGER_REQUEST_START_ROLLLEFT");
    public static RequestType TRIGGER_REQUEST_STOP_ROLLLEFT = RequestType.register(1123, "TRIGGER_REQUEST_STOP_ROLLLEFT");
    public static RequestType TRIGGER_REQUEST_START_ROLLRIGHT = RequestType.register(1124, "TRIGGER_REQUEST_START_ROLLRIGHT");
    public static RequestType TRIGGER_REQUEST_STOP_ROLLRIGHT = RequestType.register(1125, "TRIGGER_REQUEST_STOP_ROLLRIGHT");

    public static RequestType TRIGGER_REQUEST_START_GRABBING = RequestType.register(1126, "TRIGGER_REQUEST_START_GRABBING");
    public static RequestType TRIGGER_REQUEST_STOP_GRABBING = RequestType.register(1127, "TRIGGER_REQUEST_STOP_GRABBING");

    // might be specific for 'traffic'. 16.5.25: But relates to Velocity
    public static RequestType TRIGGER_REQUEST_START_SPEEDUP = RequestType.register(1128, "TRIGGER_REQUEST_START_SPEEDUP");
    public static RequestType TRIGGER_REQUEST_STOP_SPEEDUP = RequestType.register(1129, "TRIGGER_REQUEST_STOP_SPEEDUP");
    public static RequestType TRIGGER_REQUEST_START_SPEEDDOWN = RequestType.register(1130, "TRIGGER_REQUEST_START_SPEEDDOWN");
    public static RequestType TRIGGER_REQUEST_STOP_SPEEDDOWN = RequestType.register(1131, "TRIGGER_REQUEST_STOP_SPEEDDOWN");

    public static RequestType REQUEST_USER_MESSAGE = RequestType.register(1132, "REQUEST_USER_MESSAGE");

    public static RequestType TRIGGER_REQUEST_SPEEDUP = RequestType.register(1133, "TRIGGER_REQUEST_SPEEDUP");
    public static RequestType TRIGGER_REQUEST_SPEEDDOWN = RequestType.register(1134, "TRIGGER_REQUEST_SPEEDDOWN");

    // range of numbers continued in ObserverSystem up to 1138

    public static Request buildForwardRequest(int userEntityId) {
        return new Request(TRIGGER_REQUEST_FORWARD, new Payload(), userEntityId);
    }

    public static Request buildUserMessageRequest(Integer userEntityId, String message, int durationMillis, Integer receiverUserEntityId) {
        return new Request(REQUEST_USER_MESSAGE, new Payload()
                .addMessage(message)
                .addDuration(durationMillis)
                .addUserEntityId(receiverUserEntityId),
                userEntityId);
    }
}
