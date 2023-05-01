package de.yard.threed.engine.ecs;

import de.yard.threed.core.Event;
import de.yard.threed.core.EventType;
import de.yard.threed.core.Payload;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.platform.Log;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.platform.common.*;

import java.util.List;

import static de.yard.threed.engine.BaseEventRegistry.EVENT_CONNECTION_CLOSED;

/**
 * User administration
 * <p>
 * Created by thomass on 16.09.20.
 */

public class UserSystem extends DefaultEcsSystem {
    private static Log logger = Platform.getInstance().getLog(UserSystem.class);
    public static String TAG = "UserSystem";
    public static RequestType USER_REQUEST_LOGIN = RequestType.register(1000, "USER_REQUEST_LOGIN");
    // The logged in user wants to join.
    // The join request no longer creates an avatar for the user entity (is done after successful join)
    public static RequestType USER_REQUEST_JOIN = RequestType.register(1001, "USER_REQUEST_JOIN");

    public static EventType USER_EVENT_LOGGEDIN = EventType.register(1000, "USER_EVENT_LOGGEDIN");

    //MA31 aus RequestRegistry nach hier verschoben. Ob automove allerdings hier so passt? Mal sehen.
    public static RequestType USER_REQUEST_TELEPORT = RequestType.register(1002, "USER_REQUEST_TELEPORT");
    public static RequestType USER_REQUEST_AUTOMOVE = RequestType.register(1003, "USER_REQUEST_AUTOMOVE");

    boolean usersystemdebuglog = true;

    int userIndex = 0;

    /**
     *
     */
    public UserSystem() {
        super(new String[]{}, new RequestType[]{USER_REQUEST_LOGIN}, new EventType[]{EVENT_CONNECTION_CLOSED});
    }

    @Override
    public boolean processRequest(Request request) {
        if (usersystemdebuglog) {
            logger.debug("got request " + request);
        }
        if (request.getType().equals(USER_REQUEST_LOGIN) && SystemState.readyToJoin()) {
            String username = (String) request.getPayloadByIndex(0);
            String clientid = (String) request.getPayloadByIndex(1);
            EcsEntity user = new EcsEntity(new UserComponent(username, request.getConnectionId()));
            // Set entity name to user name. There is no benefit in setting it different, but makes things easier.
            user.setName(username);

            SystemManager.sendEvent(buildLoggedinEvent(username, clientid, user.getId(), request.getConnectionId()));
            // For simplification join immediately, without explicit request by client.
            SystemManager.putRequest(buildJoinRequest(user.getId()));
            userIndex++;
            return true;
        }
        return false;
    }

    @Override
    public void process(Event evt) {

        if (usersystemdebuglog) {
            logger.debug("got event " + evt.getType());
        }

        if (evt.getType().equals(EVENT_CONNECTION_CLOSED)) {
            String connectionid = (String) evt.getPayload().get("connectionid");
            for (EcsEntity entity : SystemManager.findEntities(e -> {
                UserComponent uc = UserComponent.getUserComponent(e);
                return uc != null && connectionid.equals(uc.getConnectionId());
            })) {
                logger.debug("removing user entity " + entity.getName());
                if (entity.getSceneNode() != null) {
                    SceneNode.removeSceneNode(entity.getSceneNode());
                }
                SystemManager.removeEntity(entity);
            }
        }
    }

    @Override
    public String getTag() {
        return TAG;
    }

    public static Request buildLoginRequest(String username, String clientid) {
        return new Request(USER_REQUEST_LOGIN, new Payload(username, clientid));
    }

    public static Event buildLoggedinEvent(String username, String clientid, int userEntityId, String connectionId) {
        return new Event(USER_EVENT_LOGGEDIN, new Payload()
                .add("username", username)
                .add("clientid", clientid)
                .add("userentityid", new Integer(userEntityId))
                .add("connectionid", connectionId));
    }

    public static Request buildJoinRequest(int userEntityId/*, boolean forLogin*/) {
        return new Request(USER_REQUEST_JOIN, new Payload(/*new Integer(userEntityId), new Boolean(forLogin)*/),userEntityId);
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
