package de.yard.threed.maze;

import de.yard.threed.core.EventType;

/**
 * Events outside of systems.
 * <p>
 * Created on 24.10.18.
 */
public class EventRegistry {

    public static EventType EVENT_MAZE_LOADED = EventType.register(2001, "EVENT_MAZE_LOADED");


    public static EventType EVENT_BULLET_FIRED = EventType.register(2002, "EVENT_BULLET_FIRED");
}
