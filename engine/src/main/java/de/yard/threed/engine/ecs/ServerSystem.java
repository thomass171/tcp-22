package de.yard.threed.engine.ecs;

import de.yard.threed.core.Event;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.EventType;
import de.yard.threed.engine.platform.common.RequestType;

/**
 *
 * system to provide information about inital events (static content, eg. scenery) and entity state changes
 * to a client in client-server mode.
 *
 * Seems to be better to have a dedicated system instead of integrating it in SystemManager.
 *
 * To some degree this is the counterpart of {@link ClientSystem).
 * <p>
 * Created by thomass on 16.09.20.
 */

public class ServerSystem extends DefaultEcsSystem {
    private static Log logger = Platform.getInstance().getLog(ServerSystem.class);

    /**
     *
     */
    public ServerSystem() {
        super(new String[]{});
    }


    public ServerSystem(String[] strings, RequestType[] requestTypes, EventType[] eventTypes) {
        super(strings, requestTypes, eventTypes);
    }

    public static ServerSystem buildForInitialEventsForClient(EventType[] initialEvents){
        return new ServerSystem();
    }

    private void publishEntityStates(){

    }
}
