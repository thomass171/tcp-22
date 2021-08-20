package de.yard.threed.engine.ecs;

import de.yard.threed.core.Event;
import de.yard.threed.core.EventType;
import de.yard.threed.core.GeneralHandlerMap;
import de.yard.threed.core.Packet;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.BaseEventRegistry;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeSocket;
import de.yard.threed.engine.platform.common.*;


/**
 * Connection to a MP server. And back to the client.
 * <p>
 * Common super class for client and server.
 * <p>
 * Created by thomass on 16.02.21.
 */
public abstract class BusConnectorSystem extends DefaultEcsSystem {
    static Log logger = Platform.getInstance().getLog(BusConnectorSystem.class);

    GeneralHandlerMap<String> eventHandler = new GeneralHandlerMap<String>();

    public BusConnectorSystem(RequestType[] requestTypes, EventType[] eventTypes) {
        super(requestTypes, eventTypes);
    }

    /**
     * no "updatepergroup"
     *
     * @param entity always null
     * @param group
     * @param tpf
     */
    public void update(EcsEntity entity, EcsGroup group, double tpf) {

    }


    /**
     * Send event to network.
     *
     * @param evt
     */
    @Override
    public void process(Event evt) {
        //logger.debug("got event " + event.getType());
        Packet packet = buildPacket(evt);
        if (packet != null) {
            getSocket().sendPacket(packet);
        }
    }

    protected abstract NativeSocket getSocket();


    public static Packet buildPacket(RequestType requestType, String[] args) {
        Packet packet = new Packet();
        if (requestType.getLabel().equals(UserSystem.USER_REQUEST_LOGIN.getLabel())) {
            packet.add("event", UserSystem.USER_REQUEST_LOGIN.getLabel());
            packet.add("id", args[0]);
        } else {
            logger.warn("unhandled request " + requestType);
            return null;
        }

        return packet;
    }

    public Packet buildPacket(Event evt) {
        EventType eventType = evt.getType();

        Packet packet = new Packet();
        if (eventType.getLabel().equals(UserSystem.USER_EVENT_LOGGEDIN.getLabel())) {
            packet.add("event", UserSystem.USER_EVENT_LOGGEDIN.getLabel());
            //packet.add("id",args[0]);
        } else if (eventType.getLabel().equals(UserSystem.USER_EVENT_JOINED.getLabel())) {
            packet.add("event", UserSystem.USER_EVENT_JOINED.getLabel());
            //packet.add("id",args[0]);
        } else if (eventType.getLabel().equals(BaseEventRegistry.BASE_EVENT_ENTITY_CHANGE.getLabel())) {
            packet.add("event", BaseEventRegistry.BASE_EVENT_ENTITY_CHANGE.getLabel());
            packet.add(evt.getPayloadAsMap());
            //packet.add("id",args[0]);
        } else {
            logger.warn("buildPacket: unhandled event " + eventType);
            return null;
        }

        return packet;
    }


}
