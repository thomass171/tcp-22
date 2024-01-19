package de.yard.threed.engine.platform.common;

import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.Bundle;

import java.util.List;

/**
 * Only executed once.
 * 15.12.23: Moved here from SyncBundleLoader.
 */
public class PreloadCallback extends InitExecutor {
    Log logger = Platform.getInstance().getLog(PreloadCallback.class);

    String[] bundles;
    List<Bundle> loadedBundle;

    public PreloadCallback(String[] bundles, List<Bundle> loadedBundle) {
        this.bundles = bundles;
        this.loadedBundle = loadedBundle;
    }

    @Override
    public InitExecutor run() {
        logger.debug("execute");
        if (isComplete()) {
            return new SceneInitCallback();
        }
        return this;
    }

    public boolean isComplete() {
        if (loadedBundle.size() < bundles.length) {
            logger.debug("Waiting for bundle preload. Got " + loadedBundle.size() + ", expecting " + bundles.length);
            return false;
        }
        return true;
    }

    // error handling Code from webgl. Maybe add it here.
    // There is and never really was a "pause" here.

       /*18.12.23 if (bundleLoader.preLoadCompleted()) {
                    logger.info("preloading completed");
                    String[] bnames = BundleRegistry.getBundleNames();
                    for (String bname : bnames) {
                        logger.debug("preloaded bundle: " + bname);
                    }
                    preloaded = true;

                }
                if (bundleLoader.preLoadFailure()) {
                    logger.error("preloading failed");
                    MiscWrapper.alert(bundleLoader.getPreloadError());
                    // don't continue. Just return without requesting next frame.
                    return;
                    //paused = true;
                }

                if (cnt > 1000) {
            logger.info("Aborting bundle load loop after 1000 waits");
        } else {

        }
                */
}
