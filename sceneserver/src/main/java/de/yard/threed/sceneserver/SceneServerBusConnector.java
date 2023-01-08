package de.yard.threed.sceneserver;

import de.yard.threed.core.Event;
import de.yard.threed.core.EventType;
import de.yard.threed.core.GeneralHandlerMap;
import de.yard.threed.core.Packet;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.BaseEventRegistry;
import de.yard.threed.engine.ecs.BusConnector;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.EcsGroup;
import de.yard.threed.engine.ecs.UserSystem;
import de.yard.threed.engine.platform.common.RequestType;


/**
 * Connection to a MP server. And back to the client.
 * <p>
 * Common super class for client and server.
 * <p>
 * * 28.12.22: Is it really useful to have this as a dedicated system? Shouldn't the platform just extend the event bus? So no longer a ECS system.
 *
 * Created by thomass on 16.02.21.
 */
public class SceneServerBusConnector implements BusConnector {
    static Log logger = Platform.getInstance().getLog(SceneServerBusConnector.class);

    ServerSocket serverSocket;

    GeneralHandlerMap<String> eventHandler = new GeneralHandlerMap<String>();

    public SceneServerBusConnector(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

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
    public void update(EcsEntity entity, EcsGroup group, double tpf) {

    }


    /**
     * Send event to network.
     *
     * @param evt
     */
    //@Override
    public void process(Event evt) {
        //logger.debug("got event " + event.getType());
        Packet packet = buildPacket(evt);
        if (packet != null) {
            serverSocket.sendPacket(packet);
        }
    }

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
