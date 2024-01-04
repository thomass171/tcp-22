package de.yard.threed.tools;

import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleLoadDelegate;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;

import java.util.function.BooleanSupplier;

public class SyncBundleLoader {

    public static void loadBundleAndWait(String bundlename) {
        AbstractSceneRunner.getInstance().loadBundle(bundlename, new BundleLoadDelegate() {
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
