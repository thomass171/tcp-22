package de.yard.threed.sceneserver;

import de.yard.threed.core.Event;
import de.yard.threed.core.Packet;
import de.yard.threed.core.WriteException;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeSocket;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.ecs.DefaultBusConnector;
import de.yard.threed.engine.ecs.SystemManager;

import java.util.List;

/**
 * Connection of a server eventbus to clients.
 * <p>
 *
 * <p>
 * 28.12.22: Is it really useful to have this as a dedicated system? Shouldn't the platform just extend the event bus? So no longer a ECS system.
 * Sending events to network is done in {@link SystemManager}
 * <p>
 * Created by thomass on 16.02.21.
 */
public class SceneServerBusConnector extends DefaultBusConnector {
    static Log logger = Platform.getInstance().getLog(SceneServerBusConnector.class);

    public SceneServerBusConnector() {
    }

    @Override
    public void pushPacket(Packet packet, String clientId) {
        //TODO respect clientid
        for (ClientConnection cc : ClientListener.getInstance().getClientConnections()) {

                cc.sendPacket(packet);

        }
    }

    @Override
    public boolean isServer() {
        return true;
    }
}
