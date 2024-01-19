package de.yard.threed.tools;

import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleLoadDelegate;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;

/**
 * Like bundle loader used in unit tests.
 * 4.1.24
 */
public class SyncBundleLoader {

    /**
     * Has no scene runner so needs to create its own.
     * But PlatformInternals are not needed.
     */
    public static void loadBundleAndWait(String bundlename) {

        AbstractSceneRunner sceneRunner = new AbstractSceneRunner(null) {
            @Override
            public void startRenderLoop() {
                throw new RuntimeException("not implemented");
            }

            @Override
            public void sleepMs(int millis) {
                try {
                    Thread.sleep(millis);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        sceneRunner.loadBundle(bundlename, new BundleLoadDelegate() {
            @Override
            public void bundleLoad(Bundle bundle) {
                BundleRegistry.registerBundle(bundlename, bundle);
            }
        });

        while (BundleRegistry.getBundle(bundlename) == null) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
