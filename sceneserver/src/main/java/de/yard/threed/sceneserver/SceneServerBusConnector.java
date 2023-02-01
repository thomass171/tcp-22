package de.yard.threed.sceneserver;

import de.yard.threed.core.Event;
import de.yard.threed.core.EventType;
import de.yard.threed.core.GeneralHandlerMap;
import de.yard.threed.core.Packet;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeSocket;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.BaseEventRegistry;
import de.yard.threed.engine.ecs.DefaultBusConnector;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.EcsGroup;
import de.yard.threed.engine.ecs.UserSystem;
import de.yard.threed.engine.platform.common.RequestType;

import java.util.List;


/**
 * Connection to a MP server. And back to the client.
 * <p>
 * Common super class for client and server.
 * <p>
 * * 28.12.22: Is it really useful to have this as a dedicated system? Shouldn't the platform just extend the event bus? So no longer a ECS system.
 *
 * Created by thomass on 16.02.21.
 */
public class SceneServerBusConnector extends DefaultBusConnector {
    static Log logger = Platform.getInstance().getLog(SceneServerBusConnector.class);

    //ServerSocket serverSocket;

    GeneralHandlerMap<String> eventHandler = new GeneralHandlerMap<String>();

    public SceneServerBusConnector() {
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

    @Override
    public List<NativeSocket> getSockets(String clientId) {
        //ClientListener.getInstance().getMpSocket());
        return ClientListener.getInstance().getSockets();
    }


    /**
     * Send event to network.
     *
     * @param evt
     */
    //@Override
    /*12.1.23 public void process(Event evt) {
        //logger.debug("got event " + event.getType());
        Packet packet = buildPacket(evt);
        if (packet != null) {
            serverSocket.sendPacket(packet);
        }
    }*/





}
