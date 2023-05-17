package de.yard.threed.maze;

import de.yard.threed.core.Event;
import de.yard.threed.core.EventType;
import de.yard.threed.core.Payload;
import de.yard.threed.engine.ecs.EcsEntity;

/**
 * Events outside of systems.
 * 16.4.23: Decouple events from Systems because of client/server might be a good idea.
 * <p>
 * Created on 24.10.18.
 */
public class MazeEventRegistry {

    public static EventType EVENT_MAZE_LOADED = EventType.register(2001, "EVENT_MAZE_LOADED");


    public static EventType EVENT_BULLET_FIRED = EventType.register(2002, "EVENT_BULLET_FIRED");

    /**
     * For now only used for testing
     */
    public static EventType EVENT_MAZE_VISUALIZED = EventType.register(2003, "EVENT_MAZE_VISUALIZED");

    public static Event buildMazeVisualizedEvent() {
        return new Event(EVENT_MAZE_VISUALIZED, new Payload());
    }

    /**
     * grid name is only included for historical reasons. There should be no need for it and it might be empty.
     */
    public static Event buildMazeLoadedEvent(String gridname, String rawGrid) {
        return new Event(EVENT_MAZE_LOADED, new Payload().add("gridname", gridname).add("grid", rawGrid));
    }

    public static EventType EVENT_MAZE_FIREFAILED = EventType.register(2004, "EVENT_MAZE_FIREFAILED");

    public static Event buildFireFailedEvent(EcsEntity userEntity, String msg) {
        return new Event(EVENT_MAZE_FIREFAILED, new Payload().add("userentityid", userEntity.getId()).add("message", msg));
    }
}
