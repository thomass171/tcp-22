package de.yard.threed.engine.platform;

import de.yard.threed.core.StringUtils;
import de.yard.threed.core.platform.AsyncHttpResponse;
import de.yard.threed.core.platform.AsyncJobDelegate;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeBundleResourceLoader;
import de.yard.threed.core.platform.NativeFuture;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleData;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.resource.ResourceLoader;
import de.yard.threed.core.resource.ResourcePath;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;

/**
 * The resources needed might not been available in the bundle, so these need to be loaded first.
 */
public class ResourceLoaderFromDelayedBundle extends ResourceLoader implements NativeFuture<AsyncHttpResponse> {
    Log logger = Platform.getInstance().getLog(ResourceLoaderFromDelayedBundle.class);
    public BundleResource bundleResource;
    NativeBundleResourceLoader bundleResourceLoader;

    public ResourceLoaderFromDelayedBundle(BundleResource bundleResource, NativeBundleResourceLoader bundleResourceLoader) {
        super(bundleResource);
        this.bundleResource = bundleResource;
        this.bundleResourceLoader = bundleResourceLoader;
        // resourceLoader = Platform.getInstance().buildResourceLoader(StringUtils.substringAfterLast(bundlename, "/"), bundlebasedir);
    }

    @Override
    public void loadResource(AsyncJobDelegate<AsyncHttpResponse> delegate) {

        if (bundleResource.bundle.contains(bundleResource)) {
            // the easy case
            AbstractSceneRunner.getInstance().addFuture(this, delegate);
        } else {
            NativeFuture<AsyncHttpResponse> instance = this;
            // extend bundle first, then continue as usual.
            bundleResourceLoader.loadFile(bundleResource.getFullName(), new AsyncJobDelegate<AsyncHttpResponse>() {
                @Override
                public void completed(AsyncHttpResponse response) {
                    // adding to bundle maybe not required? But helpful for testing
                    PlatformBundleLoader.addLoadedBundleData(response, bundleResource.bundle, bundleResource.getFullName(), logger);
                    AbstractSceneRunner.getInstance().addFuture(instance, delegate);
                }
            });
        }
    }

    @Override
    public ResourceLoader fromReference(String reference) {
        BundleResource br = new BundleResource(bundleResource.bundle, bundleResource.getPath(), reference);
        return new ResourceLoaderFromDelayedBundle(br, bundleResourceLoader);
    }

    @Override
    public ResourceLoader fromRootReference(ResourcePath texturebasepath, String texturename) {
        BundleResource br = new BundleResource(texturebasepath, texturename);
        br.bundle = bundleResource.bundle;
        return new ResourceLoaderFromDelayedBundle(br, bundleResourceLoader);
    }

    @Override
    public boolean isDone() {
        return true;
    }

    @Override
    public AsyncHttpResponse get() {
        Bundle bundle = bundleResource.bundle;
        if (bundle == null) {
            BundleRegistry.getBundle(bundleResource.getBundlename());
        }
        BundleData data = bundle.getResource(bundleResource);
        if (data != null) {
            return new AsyncHttpResponse(0, null, data.b, 0);
        } else {
            return new AsyncHttpResponse(-1, "");
        }
    }
}
