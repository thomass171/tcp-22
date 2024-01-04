package de.yard.threed.core.platform;


/**
 * Load a single resource file of a bundle. Used for both loading a bundle (eg. XML files) and
 * later loading of content (eg. textures).
 *
 * Derived from ResourceManager and NativeResourceReader.
 * Working async.
 * 
 * 11.12.2023: Used for platform independent bundle loading, but also other??. So doesn't use BundleResource(??)
 * Date: 05.08.21
 */
public interface NativeBundleResourceLoader {

    /**
     * 'resource' is relative to some 'basepath'.
     */
    void loadFile(String resource, AsyncJobDelegate<AsyncHttpResponse> asyncJobDelegate);

    /**
     * The location plus bundle name
     *
     */
    String getBasePath();

    //not until it is really needed. public abstract boolean exists(String resource);

    //??public abstract String getBasedir();
}
