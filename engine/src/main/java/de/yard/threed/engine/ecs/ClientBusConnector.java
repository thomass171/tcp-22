package de.yard.threed.engine.ecs;

import de.yard.threed.core.Packet;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeSocket;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.ecs.DefaultBusConnector;

import java.util.ArrayList;
import java.util.List;


/**
 * Connection of a client (eventbus) to a server.
 * <p>
 * <p>
 * Created by thomass on 16.02.23.
 */
public class ClientBusConnector extends DefaultBusConnector {
    static Log logger = Platform.getInstance().getLog(ClientBusConnector.class);

    NativeSocket socket;

    public ClientBusConnector(NativeSocket socket) {
        this.socket = socket;
    }

    @Override
    public List<NativeSocket> getSockets(String clientId) {
        List<NativeSocket> list = new ArrayList<NativeSocket>();
        list.add(socket);
        return list;
    }

    /**
     * The client part for getting available packets from queue. Non blocking.
     * Server uses SceneServerRenderer for now.
     */
    public Packet getPacket() {
        return socket.getPacket();
    }

}
