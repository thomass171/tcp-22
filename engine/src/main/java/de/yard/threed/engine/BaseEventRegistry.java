package de.yard.threed.engine;

import de.yard.threed.core.EventType;

/**
 * 17.1.23: Why 'BASE'? Is it more base than eg. USER_EVENT_LOGGEDIN? Because its not related to a system?
 * 19.2.23: Due to client/server it make sense to decouple event/request registration from specific systems. But where?
 * In the app? Hmm. Maybe in the module. So this here could be the registry for module "engine". So the name might fit though.
 */
public class BaseEventRegistry {

    /*public static EventType BASE_EVENT_ENTITY_CREATE = EventType.register(1002, "BASE_EVENT_ENTITY_CREATE");

    public static EventType BASE_EVENT_ENTITY_CHANGE = EventType.register(1003, "BASE_EVENT_ENTITY_CHANGE");*/

    public static EventType EVENT_ENTITYSTATE = EventType.register(1008, "EVENT_ENTITYSTATE");
}
