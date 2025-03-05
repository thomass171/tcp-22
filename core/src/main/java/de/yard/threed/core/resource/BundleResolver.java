package de.yard.threed.core.resource;

import de.yard.threed.core.platform.Platform;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A resolver that knows where a specific bundle can be found. Typical locations are somewhere in the filesystem
 * or on a HTTP server.
 */
public abstract class BundleResolver {
    // optional sub/relative path. Should ...
    protected Map<String, String> bundlePath = new HashMap<>();

    public abstract ResourcePath resolveBundle(String bundleName);

    public void addBundlePath(String bundle, String path) {
        bundlePath.put(bundle, path);
    }

    /**
     * Returns path to location where directory is expected.
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

    protected String getBundlePath(String bundle) {
        String p = bundlePath.get(bundle);
        if (p == null) {
            return "";
        }
        return p + "/";
    }
}
