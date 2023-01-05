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
 * Connection to a scene server. And back to the client.
 * <p>
 * Common super class for client and server.
 * <p>
 * * 28.12.22: Is it really useful to have this as a dedicated system? Shouldn't the platform just extend the event bus? So no longer a ECS system.
 *
 * Created by thomass on 16.02.21.
 */
public interface BusConnector/*System extends DefaultEcsSystem*/ {
    static Log logger = Platform.getInstance().getLog(BusConnector.class);

    //NativeSocket socket;

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
     * Send event to network.
     *
     * @param evt
     */
    //@Override
     void process(Event evt) ;


}
