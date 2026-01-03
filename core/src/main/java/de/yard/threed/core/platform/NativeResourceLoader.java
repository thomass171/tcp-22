package de.yard.threed.core.platform;


/**
 * Load a single resource file of a bundle. Used for both loading a bundle (eg. XML files) and
 * later loading of content (eg. textures).
 *
 * Derived from ResourceManager and NativeResourceReader.
 * Working async.
 * 
 * 11.12.2023: Used for platform independent bundle loading, but also other??. So doesn't use BundleResource(??)
 * 18.12.2025: Renamed from NativeBundleResourceLoader to make clear it doesn't really relate to bundles. The implementation
 * should be aware of where the resource comes from.
 * Date: 05.08.21
 */
public interface NativeResourceLoader {

    /**
     * 'resource' is relative to some 'basepath' that is known in the implementation.
     */
    void loadFile(String resource, AsyncJobDelegate<AsyncHttpResponse> asyncJobDelegate);

    /**
     * The location plus bundle name
     *
     */
    String getBasePath();
}
