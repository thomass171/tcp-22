package de.yard.threed.core;

import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;

import java.util.HashMap;
import java.util.Map;

/**
 * Classification of events and registry of all available event types.
 * <p>
 * 7.5.19: There is also a {@link EventRegistry}.
 *
 * <p>
 * Created by thomass on 27.12.16.
 */
public class EventType {
    static Log logger = Platform.getInstance().getLog(EventType.class);
    public int type;
    String label = "";
    // 477 arbitrary
    private static int uniquetype = 477;
    private static Map<Integer, EventType> registry;

    public EventType() {
        this.type = uniquetype++;
        if (registry == null) {
            registry = new HashMap<Integer, EventType>();
        }
        registry.put(this.type, this);
    }

    public EventType(String label) {
        this();
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

    @Override
    public String toString() {
        return "" + type + "(" + label + ")";
    }
}
