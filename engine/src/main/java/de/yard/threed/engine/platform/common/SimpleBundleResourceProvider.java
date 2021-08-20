package de.yard.threed.engine.platform.common;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleResourceProvider;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.platform.Log;

/**
 * Related to one specific bundle.
 * (like Simgear BasePathProvider)
 * Created by thomass on 12.04.17.
 */
public class SimpleBundleResourceProvider implements BundleResourceProvider {
    static Log logger = Platform.getInstance().getLog(SimpleBundleResourceProvider.class);
    
    String bundlename;
    public SimpleBundleResourceProvider(String bundlename) {
        this.bundlename=bundlename;
    }

    @Override
    public BundleResource resolve(String resource/*, Bundle currrentbundle*/) {
        Bundle bundle = BundleRegistry.getBundle(bundlename);
        if (bundle == null){
            logger.error("Bundle not found: "+bundlename);
            return null;
        }
        BundleResource br = new BundleResource(resource);
        if (bundle.exists(br)){
            br.bundle=bundle;
            return br;
        }
        return null;
    }

    @Override
    public boolean isAircraftSpecific() {
        return false;
    }
}
