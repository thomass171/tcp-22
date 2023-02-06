package de.yard.threed.engine;

import de.yard.threed.core.EventType;

/**
 * 17.1.23: Why 'BASE'? Is it more base than eg. USER_EVENT_LOGGEDIN? Because its not related to a system?
 * 19.2.21
 */
public class BaseEventRegistry {

    public static EventType BASE_EVENT_ENTITY_CREATE = EventType.register(1002, "BASE_EVENT_ENTITY_CREATE");

    public static EventType BASE_EVENT_ENTITY_CHANGE = EventType.register(1003, "BASE_EVENT_ENTITY_CHANGE");
}
