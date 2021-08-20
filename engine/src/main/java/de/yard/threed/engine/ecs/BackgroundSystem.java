package de.yard.threed.engine.ecs;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.EventType;
import de.yard.threed.engine.platform.common.RequestType;

/**
 * Provide information about static content, eg. scenery
 *
 * <p>
 * Created by thomass on 16.09.20.
 */

public class BackgroundSystem extends DefaultEcsSystem {
    private static Log logger = Platform.getInstance().getLog(BackgroundSystem.class);

    /**
     *
     */
    public BackgroundSystem() {
        super(new String[]{});
    }


    public BackgroundSystem(String[] strings, RequestType[] requestTypes, EventType[] eventTypes) {
        super(strings, requestTypes, eventTypes);
    }
}
