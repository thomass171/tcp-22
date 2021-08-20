package de.yard.threed.engine.platform.common;

import de.yard.threed.core.Payload;

/**
 * Ein Request innerhalb ECS, aber auch losgelöst von ECS.
 * z.B. auch durch User Aktion angestossen, z.B. durch Button, Menu, Click, etc. als ActionCommand
 * Irgendeiner muss sich dann darum kümmern, z.B. ein ECS System.
 *
 * 21.3.19: Abgrenzung Request/Event.
 *
 */
public class Request {
    public Payload payload;
    public int declined = 0;
    private  RequestType requesttype;

    public Request(RequestType requesttype, Payload payload) {
        this.requesttype = requesttype;
        this.payload = payload;
    }

    /**
     * 4.10.19: Payload darf auch null sein, z.B. bei ButtonClick.
     */
    public Request(RequestType requesttype) {
        this.requesttype = requesttype;
    }

    public RequestType getType() {
        return requesttype;
    }

    public Payload getPayload() {
        return payload;
    }

    /**
     * Basiert auf der Annahme, dass PAyload ein Array ist.
     * @param index
     * @return
     */
    public Object getPayloadByIndex(int index) {

        if (payload==null){
            return null;
        }
        return (payload.o)[index];
    }

    public boolean isType(RequestType requestType) {
        return (getType().equals(requestType));
    }
}
