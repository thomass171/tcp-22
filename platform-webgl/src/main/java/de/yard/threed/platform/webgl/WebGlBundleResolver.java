package de.yard.threed.platform.webgl;

import de.yard.threed.core.StringUtils;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleResolver;
import de.yard.threed.core.resource.ResourcePath;

import java.util.ArrayList;
import java.util.List;

/**
 * A resolver that knows on which HTTP server a specific bundle can be found. The default resolver
 * points to HostPageBaseURL(origin).
 * Custom using a configuration like "b1,b2,b3@http://xx.yy:ppp/context".
 */
public class WebGlBundleResolver extends BundleResolver {
    static Log logger = Platform.getInstance().getLog(WebGlBundleResolver.class);

    String[] bundlelist;
    String url;

    public WebGlBundleResolver() {
        logger.info("Building default WebGlBundleResolver using HostPageBaseURL/origin");

        bundlelist = null;
        url = null;
    }

    /**
     * path is like "b1,b2,b3@http://xx.yy:ppp/context"
     *
     * @param path
     */
    public WebGlBundleResolver(String path) {
        logger.info("Building WebGlBundleResolver for " + path);
        if (!parse(path)) {
            bundlelist = new String[]{};
            url = "";
        }
    }

    private boolean parse(String path) {
        if (!StringUtils.contains(path, "@")) {
            logger.warn("no @ in " + path);
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
            String bundlebasedir = "bundles/" + bundleName;
            return new ResourcePath(bundlebasedir);
        }
        if (StringUtils.indexOf(bundlelist, bundleName) != -1) {
            return new ResourcePath(url + "/" + bundleName);
        }
        return null;
    }

    public static List<BundleResolver> buildFromPath(String bundlepathFromEnv) {
        List<BundleResolver> l = new ArrayList<BundleResolver>();

        if (bundlepathFromEnv != null) {
            String[] parts = StringUtils.split(bundlepathFromEnv, ":");
            for (int i = 0; i < parts.length; i++) {
                // base64 natively uses '+','=' and '/' and might replace these by URL conform letters like '-'. Very confusing
                // and finally not very helpful. So also accept pure (URL encoded) strings. But these conflict with ':' separator.
                // So for now stay with base64.
                String subPart = parts[i];
                logger.debug("Found bundle sub path " + subPart);
                if (false && StringUtils.contains(subPart,"@http")){
                    l.add(new WebGlBundleResolver(subPart));
                } else {
                    l.add(new WebGlBundleResolver(WebGlCommon.atob(subPart)));
                }
            }
        }
        return l;
    }
}
