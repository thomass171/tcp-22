package de.yard.threed.core.resource;

import de.yard.threed.core.StringUtils;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;

/**
 * A resolver that knows on which HTTP server a specific bundle can be found
 * using a configuration like "b1,b2,b3@http://xx.yy:ppp/context".
 * <p>
 * Might fall back to some platform default like HostPageBaseURL(origin).
 */
public class HttpBundleResolver extends BundleResolver {

    protected String[] bundlelist;
    protected String url;

    /**
     * Default constructor without URL that always resolves (needs a default platform URL).
     */
    public HttpBundleResolver() {
        this.url = null;
    }

    /**
     * path is like "b1,b2,b3@http://xx.yy:ppp/context"
     * 1.3.25 '@' no optional for redirecting in general to HOSTDIE in webgl
     *
     * @param path
     */
    public HttpBundleResolver(String path) {
        getLog().info("Building HttpBundleResolver for " + path);
        if (!parse(path)) {
            bundlelist = new String[]{};
            url = "";
        }
    }

    /**
     * 3.3.25: Additional more specific constructor.
     */
    public HttpBundleResolver(String url, String[] bundlelist) {
        this.url = url;
        this.bundlelist = bundlelist;
    }

    protected boolean parse(String path) {
        if (!StringUtils.contains(path, "@")) {
            getLog().warn("no @ in " + path);
            return false;
        }
        String[] parts = StringUtils.split(path, "@");
        url = parts[1];
        bundlelist = StringUtils.split(parts[0], ",");
        return true;
    }

    @Override
    public ResourcePath resolveBundle(String bundleName) {

        if (url == null) {
            // default to HostPageBaseURL/origin
            String bundlebasedir = "bundles/" + getBundlePath(bundleName) + bundleName;
            return new ResourcePath(bundlebasedir);
        }
        if (StringUtils.indexOf(bundlelist, bundleName) != -1) {
            return new ResourcePath(url + "/" + getBundlePath(bundleName) + bundleName);
        }
        if (bundlelist.length == 1 && bundlelist[0].equals("*")) {
            return new ResourcePath(url + "/" + getBundlePath(bundleName) + bundleName);
        }

        return null;
    }

    protected Log getLog() {
        return Platform.getInstance().getLog(HttpBundleResolver.class);
    }
}
