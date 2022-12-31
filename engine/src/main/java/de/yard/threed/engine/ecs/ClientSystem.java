package de.yard.threed.engine.ecs;

import de.yard.threed.core.EventType;
import de.yard.threed.core.Packet;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.platform.Log;

import de.yard.threed.core.platform.NativeSocket;
import de.yard.threed.engine.platform.common.*;


/**
 * Connection to a MP server. And back to the client.
 * Ob man das auch im server einsetzen kann, ist aber unklar. Ein listen() geht hier z.B. nicht.
 * Ich lass das mal. Das Gegenstueck ist jetzt NetworkSystem. Aber mit Superklasse.
 *
 * 28.12.22: Is it really useful to have this as a dedicated system? Shouldn't the platform just extend the event bus?
 *
 * <p>
 * Created by thomass on 16.02.21.
 */
public class ClientSystem extends BusConnectorSystem {
    static Log logger = Platform.getInstance().getLog(ClientSystem.class);

    protected NativeSocket socket;

    public ClientSystem() {
        // not component/entity related. no "updatepergroup"
        super(new RequestType[]{
                        UserSystem.USER_REQUEST_TELEPORT,
                        UserSystem.USER_REQUEST_LOGIN,
                        UserSystem.USER_REQUEST_JOIN},
                new EventType[]{});
    }

    /**
     * no "updatepergroup"
     *
     * @param entity always null
     * @param group
     * @param tpf
     */
    public void update(EcsEntity entity, EcsGroup group, double tpf) {
        if (socket != null) {
            processExternalEvents();
        }
    }

    @Override
    protected NativeSocket getSocket() {
        return socket;
    }

    @Override
    public boolean processRequest(Request request) {
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
    }

    /**
     * Ueber socket eingehende Events in den lokalen Eventbus einstellen.
     */
    private void processExternalEvents() {
        Packet packet;
        while ((packet = socket.getPacket()) != null) {
            //Event evt = Packet.buildEvent(packet);
            //SystemManager.sendEvent(evt);
        }
    }
}
