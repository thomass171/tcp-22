package de.yard.threed.core.resource;

import java.util.List;

/**
 *
 */
public abstract class BundleResolver {
    public abstract ResourcePath resolveBundle(String bundleName);

    /**
     * Liefert das "Verzeichnis", in dem das Directory erwartet wird.
     * 
     * @param bundleName
     * @param bundleResolverList
     * @return
     */
    public static ResourcePath resolveBundle(String bundleName, List<BundleResolver> bundleResolverList) {

        for (BundleResolver bundleResolver : bundleResolverList) {
            ResourcePath rp = bundleResolver.resolveBundle(bundleName);
            if (rp != null) {
                return rp;
            }
        }
        throw new RuntimeException("bundle " + bundleName + " not found with " + bundleResolverList.size() + " resolver");
    }
}
