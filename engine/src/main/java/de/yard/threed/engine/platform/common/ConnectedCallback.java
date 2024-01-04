package de.yard.threed.engine.platform.common;

import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeSocket;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.engine.ecs.ClientBusConnector;

import java.util.List;

/**
 * Only executed once.
 */
public class ConnectedCallback extends InitExecutor {
    Log logger = Platform.getInstance().getLog(ConnectedCallback.class);

    public ConnectedCallback() {
    }

    @Override
    public InitExecutor run() {
        logger.debug("execute");
        if (isComplete()) {
            // nothing more inits, lets enter renderloop
            return null;
        }
        return this;
    }

    public boolean isComplete() {
        // wait for socket. preload and init() are done here.
        if (AbstractSceneRunner.getInstance().getBusConnector() != null) {
            ClientBusConnector cbc = AbstractSceneRunner.getInstance().getBusConnector();
            NativeSocket socket =  cbc.getSocket();
            logger.debug("check socket");
            if (socket.isPending()) {
                logger.debug("Waiting for socket to connect");
                return false;
            }
        }

        return true;
    }
}
