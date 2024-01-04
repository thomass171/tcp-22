package de.yard.threed.engine.platform.common;

import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.Bundle;

import java.util.List;

/**
 * Only executed once.
 */
public class SceneInitCallback extends InitExecutor {
    Log logger = Platform.getInstance().getLog(SceneInitCallback.class);

    public SceneInitCallback() {
    }

    @Override
    public InitExecutor run() {
        logger.debug("execute");
        AbstractSceneRunner.getInstance().initScene();
        AbstractSceneRunner.getInstance().postInit();
        return new ConnectedCallback();
    }

}
