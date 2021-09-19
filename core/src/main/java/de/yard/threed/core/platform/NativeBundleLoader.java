package de.yard.threed.core.platform;

import de.yard.threed.core.Pair;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleLoadDelegate;
import de.yard.threed.core.resource.BundleResource;

import java.util.List;

/**
 * Wenn beim Lesen eines einzelnen Elements ein Fehler auftritt, bleibt das im Bundle halt leer. Es erfolgt aber kein Abbruch.
 */
public interface NativeBundleLoader {
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
