package de.yard.threed.core.resource;

import de.yard.threed.core.StringUtils;
import de.yard.threed.core.platform.Platform;

/**
 *
 * An absolute or relative URL of a resource. Relative relies on some platform default (eg. browser origin).
 * Independent from bundles and HTTP. "baseUrl" might point to a filesystem.
 * 2.1.24: Renamed from UrlResource.
 * <p>
 * Created by thomass on 07.11.23.
 */
public class URL implements NativeResource {
    String baseUrl;
    ResourcePath path;
    //Real filename without path
    private String name;

    public URL(String baseUrl, ResourcePath path, String name) {

        this.baseUrl = baseUrl;
        this.path = path;
        this.name = name;
    }

    /**
     * No longer the full URL but only the last part.
     */
    @Override
    public String getName() {
        return name;
    }

    @Override
    public URL getUrl() {
        return this;
    }

    public String getAsString() {
        return baseUrl + "/" + getFullName();
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    @Override
    public String getFullName() {
        if (path != null && path.getPathForKey() != null && StringUtils.length(path.getPathForKey()) > 0) {
            return path.getPathForKey() + "/" + name;
        }
        return name;
    }

    @Override
    public boolean isBundled() {
        return false;
    }

    @Override
    public String getFullQualifiedName() {
        String b = baseUrl;
        if (b == null) {
            // neither set ??
            b = "";
        } else {
            b += "/";
        }
        if (getPath() != null && StringUtils.length(getPath().getPath()) > 0) {
            return b + getPath().getPath() + "/" + name;
        }
        return b + name;
    }

    @Override
    public String getExtension() {
        int index = StringUtils.lastIndexOf(name, ".");
        if (index == -1) {
            return "";
        }
        return StringUtils.substring(name, index + 1);
    }

    @Override
    public String toString() {
        return getAsString();
    }

    public boolean isHttp() {
        return StringUtils.startsWith(baseUrl, "http");
    }

    @Override
    public String getBasename() {

        String ext = getExtension();
        if (StringUtils.length(ext) == 0) {
            return name;
        }
        return StringUtils.substring(name, 0, StringUtils.indexOf(name, "." + ext));
    }

    @Override
    public ResourcePath getPath() {
        return path;
    }

    /**
     * Needs bundle to be set! Might be a temporary solution.
     * Code from PlatformWebGl and JavaBundleHelper
     *
     * @param bundleResource
     * @return
     */
    public static URL fromBundleResource(BundleResource bundleResource) {
        // bundle traditionally was expected to be set. But be more prepared now.
        Bundle bundle = bundleResource.bundle;
        if (bundle == null) {
            Platform.getInstance().getLog(URL.class).warn("fromBundleResource:bundle not set for file " + bundleResource.getFullName() +
                    ",bundlename=" + bundleResource.getBundlename());
            //return null;//defaulttexture;
            if (bundleResource.getBundlename() == null) {
                // no good situation. Shouldn't happen. But be prepared and return null.
                //throw new RuntimeException("no bundle information for " + bundleResource);
                Platform.getInstance().getLog(URL.class).error("no bundle information for " + bundleResource);
                return null;
            } else {
                bundle = BundleRegistry.getBundle(bundleResource.getBundlename());
            }
        }

        // don't use filename/bundlebasedir for HTTP part, ie. resolving again via bundle name might end in default bundle resolver point to origin.
        String bundlebasedir;
        if (bundle.getBasePath().startsWith("http")) {
            bundlebasedir = bundle.getBasePath();
        } else {
            // why should we resolve again?
            //bundlebasedir = BundleResolver.resolveBundle(bundleResource.bundle.name, Platform.getInstance().bundleResolver).getPath();
            bundlebasedir = bundle.getBasePath();
        }
        return new URL(bundlebasedir, bundleResource.getPath(), bundleResource.getName());
    }

    /*19.2.24 public static URL fromReference(URL url, String reference) {
        String base = StringUtils.substringBeforeLast(url.getUrl(),"/");
        return new URL(base + "/" + reference);
    }*/
}
