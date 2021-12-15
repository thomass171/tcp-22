package de.yard.threed.core.resource;

import de.yard.threed.core.XmlException;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeDocument;
import de.yard.threed.core.platform.Platform;

public class BundleHelper {
    static Log logger = Platform.getInstance().getLog(BundleHelper.class);

    /**
     * Bundle must be available, ie. already loaded.
     */
    public static BundleData loadDataFromBundle(BundleResource br) {
        Bundle bundle = br.bundle;
        if (bundle == null) {
            bundle = BundleRegistry.getBundle(br.bundlename);
            if (bundle == null) {
                logger.error("no bundle:" + br.bundlename);
                return null;
            }
        }
        BundleData data = bundle.getResource(br);
        return data;
    }
}
