package de.yard.threed.engine.ecs;

import de.yard.threed.core.Event;
import de.yard.threed.core.EventType;
import de.yard.threed.core.GeneralHandlerMap;
import de.yard.threed.core.Packet;
import de.yard.threed.core.Payload;
import de.yard.threed.core.Util;
import de.yard.threed.core.platform.NativeSocket;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.platform.Log;
import de.yard.threed.engine.Transform;
import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.engine.platform.common.RequestType;

import java.util.List;


/**
 *
 * <p>
 * Common super class for connecting a client and server event bus.
 * <p>
 * 28.12.22: Is it really useful to have this as a dedicated system? Shouldn't the platform just extend the event bus? So no longer a ECS system.
 * But still a {@link ServerSystem} and {@link ClientSystem} are used.
 * Only for sending events/requests. Receiving is done in SceneServerRenderer and ClientBusConnector.
 * <p>
 * Created by thomass on 16.02.21.
 */
public abstract class DefaultBusConnector {
    static Log logger = Platform.getInstance().getLog(DefaultBusConnector.class);

    public static final int DEFAULT_PORT = 5809;

    // Events on a scene node level for something like an inspector. Currently not used.
    public static boolean nodeSyncEnabled = false;
    public static EventType EVENT_MODELLOADED = EventType.register(1004, "EVENT_MODELLOADED");
    public static EventType EVENT_NODECREATED = EventType.register(1005, "EVENT_NODECREATED");
    public static EventType EVENT_NODEPARENTCHANGED = EventType.register(1006, "EVENT_NODEPARENTCHANGED");
    public static EventType EVENT_NODECHANGED = EventType.register(1007, "EVENT_NODECHANGED");

    // Events on entity level appear to be more efficient
    public static boolean entitySyncEnabled = true;
    public static EventType EVENT_ENTITYSTATE = EventType.register(1008, "EVENT_ENTITYSTATE");
    //public static EventType EVENT_ENTITYMODELCREATED = EventType.register("EVENT_ENTITYMODELCREATED");
    //public static EventType EVENT_ENTITYCHANGED = EventType.register("EVENT_ENTITYCHANGED");

    /**
     * Send event to network for all clients (or server).
     */
    public void pushEvent(Event evt) {
        pushEvent(evt, null);
    }

    /**
     * Send event to network (optionally to specific client).
     */
    public void pushEvent(Event evt, String clientId) {
        for (NativeSocket socket : getSockets(clientId)) {
            socket.sendPacket(encodeEvent(evt));
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

    public static Event buildEntitiyStateEvent(EcsEntity entity) {
        Payload payload = new Payload()
                .add("entityid", entity.getId())
                .add("buildername", (entity.getBuilderName() != null) ? entity.getBuilderName() : "");
        if (entity.getSceneNode() != null) {
            Transform transform = entity.getSceneNode().getTransform();
            payload = payload.add("position", transform.getPosition())
                    .add("rotation", transform.getRotation())
                    .add("scale", transform.getScale());
        }
        return new Event(EVENT_ENTITYSTATE, payload);
    }

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
