package de.yard.threed.core;

import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;

import java.util.HashMap;
import java.util.Map;

/**
 * Classification of events and registry of all available event types.
 * <p>
 * 7.5.19: There is also a {@link EventRegistry}.
 * Range of types:
 * 1000-1999 engine
 * 2000-2999 maze
 * 3000-3999 graph
 * 4000-4999 traffic
 *
 * <p>
 * Created by thomass on 27.12.16.
 */
public class EventType {
    static Log logger = Platform.getInstance().getLog(EventType.class);
    public int type;
    String label = "";
    private static Map<Integer, EventType> registry = new HashMap<Integer, EventType>();

    private EventType(int uniquetype) {
        this.type = uniquetype;
        if (registry.containsKey(uniquetype)) {
            throw new RuntimeException("Duplicate event id type " + uniquetype);
        }
        registry.put(this.type, this);
    }

    private EventType(int uniquetype, String label) {
        this(uniquetype);
        this.label = label;
    }

    @Override
    public boolean equals(Object evt) {
        return ((EventType) evt).type == type;
    }

    public final int getType() {
        return type;
    }

    public final String getLabel() {
        return label;
    }

    public static EventType findById(int type) {
        EventType requestType = registry.get(type);
        if (requestType == null) {
            logger.warn("EventType not found:" + type);
        }
        return requestType;
    }

    public static EventType register(int type, String label) {
        EventType eventType = registry.get(type);
        if (eventType == null) {
            return new EventType(type, label);
        }
        if (!eventType.getLabel().equals(label)) {
            throw new RuntimeException("inconsistent EventType");
        }
        return eventType;
    }

    @Override
    public String toString() {
        return "" + type + "(" + label + ")";
    }
}
