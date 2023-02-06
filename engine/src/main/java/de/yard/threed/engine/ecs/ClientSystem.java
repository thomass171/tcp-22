package de.yard.threed.engine.ecs;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.platform.Log;


/**
 * Connection to a MP server. And back to the client.
 *
 * 28.12.22: Is it really useful to have this as a dedicated system? Shouldn't the platform just extend the event bus?
 * 27.1.23: But some instance is needed to handle events that are not used in monolith mode like entity change events.
 * But no base network operation (listen, socket) here.
 *
 * To some degree this is the counterpart of {@link ServerSystem).
 * <p>
 * Created by thomass on 16.02.21.
 */
public class ClientSystem extends DefaultEcsSystem/*DefaultBusConnector*/ {
    static Log logger = Platform.getInstance().getLog(ClientSystem.class);

    //protected NativeSocket socket;

    /*public ClientSystem() {
        // not component/entity related. no "updatepergroup"
        super(RequestType.register[]{
                        UserSystem.USER_REQUEST_TELEPORT,
                        UserSystem.USER_REQUEST_LOGIN,
                        UserSystem.USER_REQUEST_JOIN},
                EventType.register[]{});
    }*/

    /**
     * no "updatepergroup"
     *
     * @param entity always null
     * @param group
     * @param tpf
     */
    /*public void update(EcsEntity entity, EcsGroup group, double tpf) {
        if (socket != null) {
            processExternalEvents();
        }
    }

    //@Override
    protected NativeSocket getSocket() {
        return socket;
    }*/



    //@Override
  /*  public boolean processRequest(Request request) {
        logger.debug("got request " + request.getType());
        if (request.getType().equals(UserSystem.USER_REQUEST_LOGIN)) {
            // connect/login to server
            socket = Platform.getInstance().connectToServer();
            return true;
        }
        if (request.getType().equals(UserSystem.USER_REQUEST_TELEPORT)) {
            //pendingRequest = (IntHolder) request.getPayloadByIndex(0);

        }
        return false;
    }*/

    /**
     * Ueber socket eingehende Events in den lokalen Eventbus einstellen.
     */
    /*private void processExternalEvents() {
        Packet packet;
        while ((packet = socket.getPacket()) != null) {
            //Event evt = Packet.buildEvent(packet);
            //SystemManager.sendEvent(evt);
        }
    }*/
}
