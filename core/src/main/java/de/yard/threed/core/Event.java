package de.yard.threed.core;

import java.util.Map;

/**
 * Is in platform for future queued platform connections.
 * <p>
 * Created by thomass on 31.08.16.
 */
public class Event {
    public Payload payload;
    private EventType eventtype;

    public Event(EventType eventtype, Payload payload) {
        this.eventtype = eventtype;
        this.payload = payload;
    }

    public EventType getType() {
        return eventtype;
    }

    public Payload getPayload() {
        return payload;
    }

    @Override
    public String toString() {
        return eventtype.toString() + ",payload=" + ((payload == null) ? "null" : payload.toString());
    }

    /**
     * Basiert auf der Annahme, dass PAyload ein Array ist.
     * 16.11.23: Also deprecated.
     * @param index
     * @return
     */
    @Deprecated
    public Object getPayloadByIndex(int index) {

        if (payload == null) {
            return null;
        }
        return (payload.o)[index];
    }

    /**
     * Basiert auf der Annahme, dass PAyload eine key/value map ist.
     *
     * @return
     */
    public Map<String, String> getPayloadAsMap() {

        if (payload == null) {
            return null;
        }
        return (Map<String, String>) payload.o[0];
    }

    public boolean isType(EventType eventType) {
        return (getType().equals(eventType));
    }
}
