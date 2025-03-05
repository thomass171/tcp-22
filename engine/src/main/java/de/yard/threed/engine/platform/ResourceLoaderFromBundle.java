package de.yard.threed.engine.platform;

import de.yard.threed.core.StringUtils;
import de.yard.threed.core.buffer.SimpleByteBuffer;
import de.yard.threed.core.platform.AsyncHttpResponse;
import de.yard.threed.core.platform.AsyncJobDelegate;
import de.yard.threed.core.platform.NativeFuture;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleData;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.resource.ResourceLoader;
import de.yard.threed.core.resource.ResourcePath;
import de.yard.threed.core.resource.URL;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;

/**
 * Just get the needed resource from a bundle, which is quite straightforward.
 * 23.2.25:Why is the loader specific for one resource? Why isn't resource parameter of loadResource()?
 * Maybe due to the nature how it is build in the platform. A 'root' element might not be easy to define for all loader (HTTP?).
 */
public class ResourceLoaderFromBundle extends ResourceLoader implements NativeFuture<AsyncHttpResponse> {

    public BundleResource bundleResource;

    public ResourceLoaderFromBundle(BundleResource bundleResource) {
        super(bundleResource);
        this.bundleResource = bundleResource;
    }

    @Override
    public void loadResource(AsyncJobDelegate<AsyncHttpResponse> delegate) {

        AbstractSceneRunner.getInstance().addFuture(this, delegate);
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
