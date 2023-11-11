package de.yard.threed.core.platform;

import de.yard.threed.core.Pair;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleLoadDelegate;
import de.yard.threed.core.resource.BundleResource;

import java.util.List;

/**
 *
 * 7.11.23: The phrase 'Native' is confusing here, because a bundle loader isn't implemented by a platform like other 'Native' objects.
 */
public interface NativeBundleLoader {

    /**
     * In case of a severe error the loading might just abort and the delegate isn't called at all (but it should).
     * In case of an error during loading a single element, the element will just be missing in the bundle (7.11.23 is this a good idea?).
     * So at the end a bundle might be empty.
     * 8.11.23: delayed should no longer be an option?
     */
    void asyncBundleLoad(String bundlename, BundleLoadDelegate bundleLoadDelegate, boolean delayed);

    /**
     * ResourceManager im constructor ware schoener, geht aber noch nicht.
     *
     */
    List<Pair<BundleLoadDelegate, Bundle>> processAsync();

    /**
     * Immediately load single file of a bundle. (10.9.21:Nachladen bei delayed?)
     * Daehmlicher name, oder?
     */
    void completeBundle(BundleResource file/*, ResourceManager rm*/);

    /**
     * Nur interessant bei echtem async (GWT)
     * @param file
     * @return
     */
    boolean isLoading(BundleResource file);
}
