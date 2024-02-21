package de.yard.threed.core.resource;

import de.yard.threed.core.platform.AsyncHttpResponse;
import de.yard.threed.core.platform.AsyncJobDelegate;
import de.yard.threed.core.platform.NativeFuture;

/**
 * Abstraction for loading a resource from apps point of view. This is not about bundle loading but the
 * next step. Retrieving a resource from a loaded bundle is just one option. The other is HTTP. As a consequence,
 * the operation is async in any case. This is very general, not only for platforms. Also used for GLTF loading for example.
 */
public abstract class ResourceLoader {

    public NativeResource nativeResource;

    public ResourceLoader(NativeResource nativeResource) {
        this.nativeResource = nativeResource;
    }

    public abstract void loadResource(AsyncJobDelegate<AsyncHttpResponse> delegate);

    /**
     * Only name changes, keeps path and base.
     */
    public abstract ResourceLoader fromReference(String reference);

    /**
     * changes path and name, keeps base.
     * Needed for FG texture.
     */
    public abstract ResourceLoader fromRootReference(ResourcePath path, String reference);

    public URL getUrl(){
        return nativeResource.getUrl();
    }
}