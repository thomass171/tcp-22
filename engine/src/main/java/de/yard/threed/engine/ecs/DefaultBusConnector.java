package de.yard.threed.engine.ecs;

import de.yard.threed.core.Event;
import de.yard.threed.core.EventType;
import de.yard.threed.core.GeneralHandlerMap;
import de.yard.threed.core.Packet;
import de.yard.threed.core.Payload;
import de.yard.threed.core.StringUtils;
import de.yard.threed.core.Util;
import de.yard.threed.core.platform.NativeSocket;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.platform.Log;
import de.yard.threed.engine.BaseEventRegistry;
import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.engine.platform.common.RequestType;

import java.util.ArrayList;
import java.util.List;


/**
 * Connection to a scene server. And back to the client.
 * <p>
 * Common super class for client and server.
 * <p>
 * * 28.12.22: Is it really useful to have this as a dedicated system? Shouldn't the platform just extend the event bus? So no longer a ECS system.
 * <p>
 * Created by thomass on 16.02.21.
 */
public abstract class DefaultBusConnector/*System extends DefaultEcsSystem*/ {
    static Log logger = Platform.getInstance().getLog(DefaultBusConnector.class);

    GeneralHandlerMap<String> eventHandler = new GeneralHandlerMap<String>();

    /*public BusConnectorSystem(RequestType[] requestTypes, EventType[] eventTypes) {
        super(requestTypes, eventTypes);
    }*/

    /**
     * no "updatepergroup"
     *
     * @param entity always null
     * @param group
     * @param tpf
     */
    /*public void update(EcsEntity entity, EcsGroup group, double tpf) {

    }
*/

    /**
     * Send event to network for all clients (or server).
     */
    public void pushEvent(Event event) {
        pushEvent(event, null);
    }

    /**
     * Send event to network (optionally to specific client).
     */
    public void pushEvent(Event event, String clientId) {
        for (NativeSocket socket : getSockets(clientId)) {
            socket.sendPacket(encodeEvent(event));
        }
    }

    /**
     * Send request to network.
     */
    public void pushRequest(Request request) {
        for (NativeSocket socket : getSockets(null)) {
            socket.sendPacket(encodeRequest(request));
        }
    }

    /**
     * Client and server usable socket provider.
     *
     * @param clientId null to return all client sockets.
     * @return List of sockets on server, always one socket in client.
     */
    public abstract List<NativeSocket> getSockets(String clientId);

    public static Packet encodeRequest(Request request) {
        Packet packet = new Packet();
        packet.add("request", "" + request.getType().getType());
        if (request.getPayload() != null) {
            request.getPayload().encode(packet);
        }
        if (request.getUserEntityId() != null) {
            packet.add("userentityid", "" + request.getUserEntityId());
        }
        return packet;
    }

    public static Request decodeRequest(Packet packet) {

        String request = packet.getValue("request");
        if (request != null) {
            RequestType requestType = RequestType.findById(Util.atoi(request));

            Payload payload = Payload.decode(packet);
            String s_userEntityId = packet.getValue("userentityid");
            if (s_userEntityId == null) {
                return new Request(requestType, payload);
            }
            Integer userEntityId = Util.atoi(s_userEntityId);
            return new Request(requestType, payload, userEntityId);
        }
        return null;
    }

    public static Packet encodeEvent(Event evt) {
        Packet packet = new Packet();
        packet.add("event", "" + evt.getType().getType());
        evt.getPayload().encode(packet);
        return packet;
    }

    public static Event decodeEvent(Packet packet) {
        String evt = packet.getValue("event");
        if (evt != null) {
            EventType eventType = EventType.findById(Util.atoi(evt));

            Payload payload = Payload.decode(packet);
            return new Event(eventType, payload);
        }
        logger.warn("no event in packet " + packet.getData());
        return null;
    }

    public static boolean isEvent(Packet packet) {
        String evt = packet.getValue("event");
        return evt != null;
    }
}
