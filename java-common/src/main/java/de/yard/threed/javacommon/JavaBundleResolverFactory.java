package de.yard.threed.javacommon;

import de.yard.threed.core.resource.BundleResolver;
import de.yard.threed.core.resource.BundleResolverFactory;
import de.yard.threed.outofbrowser.SimpleBundleResolver;

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
}
