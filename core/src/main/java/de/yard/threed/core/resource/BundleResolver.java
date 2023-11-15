package de.yard.threed.core.resource;

import de.yard.threed.core.platform.Platform;

import java.util.List;

/**
 * A resolver that knows where a specific bundle can be found. Typical locations are somewhere in the filesystem
 * or on a HTTP server.
 */
public abstract class BundleResolver {
    public abstract ResourcePath resolveBundle(String bundleName);

    /**
     * Liefert das "Verzeichnis", in dem das Directory erwartet wird.
     * Returns null if bundle couldn't be resolved (was RuntimeException until 15.11.23).
     */
    public static ResourcePath resolveBundle(String bundleName, List<BundleResolver> bundleResolverList) {

        for (BundleResolver bundleResolver : bundleResolverList) {
            ResourcePath rp = bundleResolver.resolveBundle(bundleName);
            if (rp != null) {
                return rp;
            }
        }
        Platform.getInstance().getLog(BundleResolver.class).warn("bundle " + bundleName + " not found with " + bundleResolverList.size() + " resolver");
        return null;
    }
}
