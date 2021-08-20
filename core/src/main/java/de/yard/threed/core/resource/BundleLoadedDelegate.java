package de.yard.threed.core.resource;

/**
 * Created by thomass on 12.04.17.
 */
@FunctionalInterface
public interface BundleLoadedDelegate {
    void bundleLoaded(String bundlename);
}
