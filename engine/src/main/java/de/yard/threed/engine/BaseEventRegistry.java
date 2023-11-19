package de.yard.threed.engine;

import de.yard.threed.core.Event;
import de.yard.threed.core.EventType;
import de.yard.threed.core.LocalTransform;
import de.yard.threed.core.Payload;
import de.yard.threed.engine.ecs.EcsEntity;


/**
 * 17.1.23: Why 'BASE'? Is it more base than eg. USER_EVENT_LOGGEDIN? Because its not related to a system?
 * 19.2.23: Due to client/server it make sense to decouple event/request registration from specific systems. But where?
 * In the app? Hmm. Maybe in the module. So this here could be the registry for module "engine". So the name might fit though.
 */
public class BaseEventRegistry {

    // payload is entity id
    public static EventType USER_EVENT_JOINED = EventType.register(1001, "USER_EVENT_JOINED");

    public static Event buildUserJoinedEvent(EcsEntity userEntity) {
        return new Event(USER_EVENT_JOINED, new Payload().add("userentityid", userEntity.getId()));
    }

    /*public static EventType BASE_EVENT_ENTITY_CREATE = EventType.register(1002, "BASE_EVENT_ENTITY_CREATE");

    public static EventType BASE_EVENT_ENTITY_CHANGE = EventType.register(1003, "BASE_EVENT_ENTITY_CHANGE");*/

    public static EventType EVENT_ENTITYSTATE = EventType.register(1008, "EVENT_ENTITYSTATE");

    public static EventType EVENT_CONNECTION_CLOSED = EventType.register(1010, "EVENT_CONNECTION_CLOSED");

    public static Event buildConnectionClosedEvent(String connectionId) {
        return new Event(EVENT_CONNECTION_CLOSED, new Payload().add("connectionid", connectionId));
    }


    public static EventType EVENT_USER_ASSEMBLED = EventType.register(1011, "EVENT_USER_ASSEMBLED");

    public static Event buildUserAssembledEvent(EcsEntity userEntity) {
        return new Event(EVENT_USER_ASSEMBLED, new Payload().add("userentityid", userEntity.getId()));
    }

    public static EventType USER_EVENT_JOINFAILED = EventType.register(1012, "USER_EVENT_JOINFAILED");

    public static Event buildUserJoinFailedEvent(EcsEntity userEntity, String msg) {
        return new Event(USER_EVENT_JOINFAILED, new Payload().add("userentityid", userEntity.getId()).add("message", msg));
    }

    public static EventType USER_EVENT_VIEWPOINT = EventType.register(1013, "USER_EVENT_VIEWPOINT");

    public static Event buildViewpointEvent(String name, LocalTransform transform) {
        return new Event(USER_EVENT_VIEWPOINT, new Payload()
                .addName(name)
                .addPosition(transform.position)
                .addRotation(transform.rotation)
                .addScale(transform.scale));
    }
}
