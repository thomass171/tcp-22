package de.yard.threed.maze;

import de.yard.threed.core.EventType;

/**
 * Um Events nicht im System zu definieren, weil sie ja auch übergreifend verwendet werden koennten.
 *
 * 30.10.20: Allmaehlich umbenennen Event->Request
 * Created on 24.10.18.
 */
public class EventRegistry {

    public static EventType EVENT_MAZE_LOADED = new EventType("EVENT_MAZE_LOADED");



    public static EventType EVENT_BULLET_FIRED = new EventType("EVENT_BULLET_FIRED");
}
