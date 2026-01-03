package de.yard.threed.javacommon;

import de.yard.threed.core.HttpBundleResourceLoader;
import de.yard.threed.core.StringUtils;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeResourceLoader;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleResolver;
import de.yard.threed.core.resource.BundleResolverFactory;
import de.yard.threed.core.resource.ResourcePath;
import de.yard.threed.outofbrowser.FileSystemBundleResourceLoader;
import de.yard.threed.outofbrowser.SimpleBundleResolver;

import java.util.List;

public class JavaBundleResolverFactory implements BundleResolverFactory {

    private String path;

    private JavaBundleResolverFactory(String path) {
        this.path = path;
    }

    public static JavaBundleResolverFactory bySimplePath(String path) {
        return new JavaBundleResolverFactory(path);
    }

    @Override
    public BundleResolver build() {
        return new SimpleBundleResolver(path, new DefaultResourceReader());
    }

    /**
     * Build a resource loader for loading a bundle. Uses bundleresolver to know where to look for the bundle.
     * 15.12.23: Not the perfect location, but not too bad.
     */
    public static NativeResourceLoader buildResourceLoader(String bundlename, String location, List<BundleResolver> bundleResolver) {

        if (location != null && StringUtils.startsWith(location, "http")) {
            return new HttpBundleResourceLoader(location + "/" + bundlename);
        }
        if (location != null) {
            throw new RuntimeException("absolute bundle only per http");
        }
        ResourcePath bundlebasedir = BundleResolver.resolveBundle(bundlename, bundleResolver);
        if (bundlebasedir == null) {
            getLogger().error("Bundle could not be resolved: " + bundlename);
            return null;
        }
        // resolver can also resolve to http
        if (StringUtils.startsWith(bundlebasedir.getPath(), "http")) {
            // bundlename is already contained in basedir
            return new HttpBundleResourceLoader(bundlebasedir.getPath());
        }
        return new FileSystemBundleResourceLoader(bundlebasedir);
    }

    private static Log getLogger(){
        return Platform.getInstance().getLog(JavaBundleResolverFactory.class);
    }
}
