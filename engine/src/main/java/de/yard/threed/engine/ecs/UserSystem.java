package de.yard.threed.engine.ecs;

import de.yard.threed.core.Event;
import de.yard.threed.core.EventType;
import de.yard.threed.core.Payload;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.platform.Log;
import de.yard.threed.engine.platform.common.*;

/**
 * User administration
 * <p>
 * Created by thomass on 16.09.20.
 */

public class UserSystem extends DefaultEcsSystem {
    private static Log logger = Platform.getInstance().getLog(UserSystem.class);

    public static RequestType USER_REQUEST_LOGIN = new RequestType("USER_REQUEST_LOGIN");
    // Der Avatar ist erstellt und will jetzt teilnehmen. Oder wird er erst durch den Join erstellt? Ja,
    // der Join request ist fuer anlegen der entity und des Avatar. Parameter 0 "userName"?, Parameter 1 "forlogin"
    public static RequestType USER_REQUEST_JOIN = new RequestType("USER_REQUEST_JOIN");

    public static EventType USER_EVENT_LOGGEDIN = new EventType("USER_EVENT_LOGGEDIN");
    // payload ist entity id
    public static EventType USER_EVENT_JOINED = new EventType("USER_EVENT_JOINED");

    //MA31 aus RequestRegistry nach hier verschoben. Ob automove allerdings hier so passt? Mal sehen.
    public static RequestType USER_REQUEST_TELEPORT = new RequestType("USER_REQUEST_TELEPORT");
    public static RequestType USER_REQUEST_AUTOMOVE = new RequestType("USER_REQUEST_AUTOMOVE");

    boolean usersystemdebuglog = true;

    /**
     *
     */
    public UserSystem() {
        super(new String[]{}, new RequestType[]{USER_REQUEST_LOGIN}, new EventType[]{});
    }

    @Override
    public boolean processRequest(Request request) {
        if (usersystemdebuglog) {
            logger.debug("got request " + request.getType());
        }
        if (request.getType().equals(USER_REQUEST_LOGIN) && SystemState.readyToJoin()) {
            SystemManager.sendEvent(new Event(USER_EVENT_LOGGEDIN, new Payload("")));
            // als Vereinfachung direkt joinen, ohne das der Client es anfragt.
            SystemManager.putRequest(buildJOIN("",true));

            return true;
        }
        return false;
    }

    public static Request buildLOGIN(String s) {
        return new Request(USER_REQUEST_LOGIN, new Payload(s));
    }

    public static Request buildJOIN(String s, boolean forLogin) {
        return new Request(USER_REQUEST_JOIN, new Payload(s, new Boolean(forLogin)));
    }

}
