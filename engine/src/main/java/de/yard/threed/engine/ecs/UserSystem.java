package de.yard.threed.engine.ecs;

import de.yard.threed.core.Event;
import de.yard.threed.core.EventType;
import de.yard.threed.core.Payload;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.platform.Log;
import de.yard.threed.engine.platform.common.*;

import java.util.List;

/**
 * User administration
 * <p>
 * Created by thomass on 16.09.20.
 */

public class UserSystem extends DefaultEcsSystem {
    private static Log logger = Platform.getInstance().getLog(UserSystem.class);
    public static String TAG = "UserSystem";
    public static RequestType USER_REQUEST_LOGIN = RequestType.register(1000,"USER_REQUEST_LOGIN");
    // The logged in user wants to join.
    // The join request creates an avatar for the user entity. Parameter 0 "userEntityId", Parameter 1 "forlogin"
    public static RequestType USER_REQUEST_JOIN = RequestType.register(1001,"USER_REQUEST_JOIN");

    public static EventType USER_EVENT_LOGGEDIN = EventType.register(1000, "USER_EVENT_LOGGEDIN");
    // payload is entity id
    public static EventType USER_EVENT_JOINED = EventType.register(1001, "USER_EVENT_JOINED");

    //MA31 aus RequestRegistry nach hier verschoben. Ob automove allerdings hier so passt? Mal sehen.
    public static RequestType USER_REQUEST_TELEPORT = RequestType.register(1002,"USER_REQUEST_TELEPORT");
    public static RequestType USER_REQUEST_AUTOMOVE = RequestType.register(1003,"USER_REQUEST_AUTOMOVE");

    boolean usersystemdebuglog = true;

    int userIndex = 0;

    /**
     *
     */
    public UserSystem() {
        super(new String[]{}, new RequestType[]{USER_REQUEST_LOGIN}, new EventType[]{});
    }

    @Override
    public boolean processRequest(Request request) {
        if (usersystemdebuglog) {
            logger.debug("got request " + request);
        }
        if (request.getType().equals(USER_REQUEST_LOGIN) && SystemState.readyToJoin()) {
            String username = (String) request.getPayloadByIndex(0);
            String clientid = (String) request.getPayloadByIndex(1);
            EcsEntity user = new EcsEntity(new UserComponent(username));
            // Set entity name to user name. There is no benefit in setting it different, but makes things easier.
            user.setName(username);

            SystemManager.sendEvent(buildLoggedinEvent(username, clientid, user.getId()));
            // als Vereinfachung direkt joinen, ohne das der Client es anfragt.
            SystemManager.putRequest(buildJoinRequest(user.getId(), true));
            userIndex++;
            return true;
        }
        return false;
    }

    @Override
    public String getTag() {
        return TAG;
    }

    public static Request buildLoginRequest(String username, String clientid) {
        return new Request(USER_REQUEST_LOGIN, new Payload(username, clientid));
    }

    public static Event buildLoggedinEvent(String username, String clientid, int userEntityId) {
        return new Event(USER_EVENT_LOGGEDIN, new Payload(username, clientid, new Integer(userEntityId)));
    }

    public static Request buildJoinRequest(int userEntityId, boolean forLogin) {
        return new Request(USER_REQUEST_JOIN, new Payload(new Integer(userEntityId), new Boolean(forLogin)));
    }

    /**
     * Only real user, no bots.
     */
    public static EcsEntity getInitialUser() {
        List<EcsEntity> candidates = SystemManager.findEntities((e) -> {
            return UserComponent.getUserComponent(e) != null;
        });
        if (candidates.size() == 0) {
            return null;
        }
        return candidates.get(0);
    }

    public static EcsEntity getUserByEntityId(int userEntityId) {
        List<EcsEntity> candidates = SystemManager.findEntities((e) -> e.getId() == userEntityId);
        if (candidates.size() == 0) {
            return null;
        }
        return candidates.get(0);
    }

    public static List<EcsEntity> getAllUser() {
        return EcsHelper.findEntitiesByComponent(UserComponent.TAG);
    }
}
