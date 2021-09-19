package de.yard.threed.platform.webgl;

import de.yard.threed.core.resource.BundleResolver;
import de.yard.threed.core.resource.ResourcePath;

public class WebGlBundleResolver extends BundleResolver {

    public WebGlBundleResolver() {
    }

    @Override
    public ResourcePath resolveBundle(String bundleName) {

        String bundlebasedir = "bundles/" + bundleName;
        return new ResourcePath(bundlebasedir);
    }
}
