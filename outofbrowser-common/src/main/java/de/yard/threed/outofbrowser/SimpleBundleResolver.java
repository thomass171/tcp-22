package de.yard.threed.outofbrowser;

import de.yard.threed.core.StringUtils;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResolver;
import de.yard.threed.core.resource.ResourcePath;

import java.util.ArrayList;
import java.util.List;

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

    public static List<BundleResolver> buildFromPath(String bundlepathFromEnv, NativeResourceReader resourceReader) {
        List<BundleResolver> l = new ArrayList<BundleResolver>();

        if (bundlepathFromEnv != null) {
            String[] parts = StringUtils.split(bundlepathFromEnv, ":");
            for (int i = 0; i < parts.length; i++) {
                l.add(new SimpleBundleResolver(parts[i], resourceReader));
            }
        }
        return l;
    }
}
