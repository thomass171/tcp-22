package de.yard.threed.outofbrowser;

import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResolver;
import de.yard.threed.core.resource.ResourcePath;

public class SimpleBundleResolver extends BundleResolver {

    public String bundledir;
    // needed for efficient texture loading without multiple lookup? private Map<String, ResourcePath> basedirs = new HashMap<String, ResourcePath>();
    NativeResourceReader resourceReader;

    public SimpleBundleResolver(String dir, NativeResourceReader resourceReader) {
        this.resourceReader = resourceReader;
        bundledir = dir;
    }

    @Override
    public ResourcePath resolveBundle(String bundleName) {

        String basedir = bundledir + "/" + bundleName;
        String dirfile = basedir + "/" + BundleRegistry.getDirectoryName(bundleName, false);
        if (resourceReader.exists(dirfile)) {
            return new ResourcePath(basedir);
        }
        return null;
    }
}
