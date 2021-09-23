package de.yard.threed.platform.webgl;

import de.yard.threed.core.StringUtils;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleResolver;
import de.yard.threed.core.resource.ResourcePath;

import java.util.ArrayList;
import java.util.List;

public class WebGlBundleResolver extends BundleResolver {
    Log logger = Platform.getInstance().getLog(WebGlBundleResolver.class);

    String[] bundlelist;
    String url;

    public WebGlBundleResolver() {
        logger.debug("Building default WebGlBundleResolver ");

        bundlelist = null;
        url = null;
    }

    /**
     * path is like "b1,b2,b3@http://xx.yy:ppp/context"
     *
     * @param path
     */
    public WebGlBundleResolver(String path) {
        logger.debug("Building WebGlBundleResolver for " + path);
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
                l.add(new WebGlBundleResolver(WebGlCommon.atob(parts[i])));
            }
        }
        return l;
    }
}
