package de.yard.threed.engine.platform;

import de.yard.threed.core.StringUtils;
import de.yard.threed.core.buffer.SimpleByteBuffer;
import de.yard.threed.core.platform.*;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleData;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.resource.ResourceLoader;
import de.yard.threed.core.resource.ResourcePath;
import de.yard.threed.core.resource.URL;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;

/**
 * Just get the needed resource from an already loaded bundle, which is quite straightforward if the content wasn't skipped.
 * As of 18.12.2025 this might happen for GTLFs and bins. So the resources needed might not been available in the bundle,
 * so these need to be loaded first. For that purpose ResourceLoaderFromDelayedBundle merged here.
 * <p>
 * 23.2.25:Why is the loader specific for one resource? Why isn't resource parameter of loadResource()?
 * Maybe due to the nature how it is build in the platform. A 'root' element might not be easy to define for all loader (HTTP?).
 */
public class ResourceLoaderFromBundle extends ResourceLoader implements NativeFuture<AsyncHttpResponse> {
    Log logger = Platform.getInstance().getLog(ResourceLoaderFromBundle.class);
    public BundleResource bundleResource;
    NativeResourceLoader originalResourceLoader;

    public ResourceLoaderFromBundle(BundleResource bundleResource) {
        super(bundleResource);
        if (bundleResource.bundle == null) {
            throw new RuntimeException("Bundle should have already been loaded for using this resourceloader");
        }
        // Don't check that bundle is registered. The registry is just an option for frequently used bundle. One time usage
        // bundles (eg. tarrasync) are not registered likely.

        this.bundleResource = bundleResource;
        this.originalResourceLoader = bundleResource.bundle.getOriginalResourceLoader();
    }

    @Override
    public void loadResource(AsyncJobDelegate<AsyncHttpResponse> delegate) {

        if (bundleResource.bundle.contains(bundleResource)) {
            // the easy case
            AbstractSceneRunner.getInstance().addFuture(this, delegate);
        } else {
            NativeFuture<AsyncHttpResponse> instance = this;
            // extend bundle first, then continue as usual.
            originalResourceLoader.loadFile(bundleResource.getFullName(), new AsyncJobDelegate<AsyncHttpResponse>() {
                @Override
                public void completed(AsyncHttpResponse response) {
                    // adding to bundle maybe not required? But helpful for testing. But it is consistent to add it.
                    // 19.12.25: Of course add it. That's why we reached this callback. Other resource loadings will never reach it.
                    PlatformBundleLoader.addLoadedBundleData(response, bundleResource.bundle, bundleResource.getFullName(), logger, true);
                    AbstractSceneRunner.getInstance().addFuture(instance, delegate);
                }
            });
        }
    }

    @Override
    public ResourceLoader fromReference(String reference) {
        BundleResource br = new BundleResource(bundleResource.bundle, bundleResource.getPath(), reference);
        return new ResourceLoaderFromBundle(br);
    }

    @Override
    public ResourceLoader fromRootReference(ResourcePath texturebasepath, String texturename) {
        BundleResource br = new BundleResource(texturebasepath, texturename);
        br.bundle = bundleResource.bundle;
        return new ResourceLoaderFromBundle(br);
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
