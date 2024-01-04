package de.yard.threed.javacommon;

import de.yard.threed.core.HttpBundleResourceLoader;
import de.yard.threed.core.StringUtils;
import de.yard.threed.core.buffer.NativeByteBuffer;
import de.yard.threed.core.platform.AsyncHttpResponse;
import de.yard.threed.core.platform.AsyncJobDelegate;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeBundleResourceLoader;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResolver;
import de.yard.threed.core.resource.BundleResolverFactory;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.resource.NativeResource;
import de.yard.threed.core.resource.ResourcePath;
import de.yard.threed.outofbrowser.FileSystemBundleResourceLoader;
import de.yard.threed.outofbrowser.FileSystemResource;
import de.yard.threed.outofbrowser.SimpleBundleResolver;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
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
     * 15.12.23: Not the perfect location, but not too bad.
     */
    public static NativeBundleResourceLoader buildResourceLoader(String bundlename, String location, List<BundleResolver> bundleResolver) {

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
