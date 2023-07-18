package de.yard.threed.engine.platform.common;

import de.yard.threed.core.Payload;

/**
 * Ein Request innerhalb ECS, aber auch losgelöst von ECS.
 * z.B. auch durch User Aktion angestossen, z.B. durch Button, Menu, Click, etc. als ActionCommand
 * Irgendeiner muss sich dann darum kümmern, z.B. ein ECS System.
 * <p>
 * 21.3.19: Abgrenzung Request/Event.
 */
public class Request {
    // payload might be null, eg. for button click.
    public Payload payload;
    public int declined = 0;
    private RequestType requesttype;
    // userEntityId is null for non user requests.
    private Integer userEntityId;
    // Set in client/server. null otherwise
    private String connectionId;

    public Request(RequestType requesttype, Payload payload, Integer userEntityId) {
        this.requesttype = requesttype;
        this.payload = payload;
        this.userEntityId = userEntityId;
    }

    public Request(RequestType requesttype, Payload payload) {
        this.requesttype = requesttype;
        this.payload = payload;
    }

    public Request(RequestType requesttype, Integer userEntityId) {
        this.requesttype = requesttype;
        this.userEntityId = userEntityId;
        // avoid NPE
        this.payload = new Payload();
    }

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
     * payload must be an array!
     *
     * @param index
     * @return
     */
    public Object getPayloadByIndex(int index) {

        if (payload == null) {
            return null;
        }
        return (payload.o)[index];
    }

    public boolean isType(RequestType requestType) {
        return (getType().equals(requestType));
    }

    public Integer getUserEntityId() {
        return userEntityId;
    }

    public void setUserEntityId(Integer userEntityId) {
        this.userEntityId = userEntityId;
    }

    @Override
    public String toString() {
        return "type=" + requesttype + ",payload=" + payload + ",userEntityId=" + userEntityId;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    /**
     * Will be null in local mode.
     */
    public String getConnectionId() {
        return connectionId;
    }
}
