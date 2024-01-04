package de.yard.threed.core.resource;

import de.yard.threed.core.StringUtils;
import de.yard.threed.core.platform.Platform;

/**
 *
 * An absolute or relative URL of a resource. Relative relies on some platform default (eg. browser origin).
 * 2.1.24: Renamed from UrlResource.
 * <p>
 * Created by thomass on 07.11.23.
 */
public class URL /*implements NativeResource*/ {
    //Real filename without path
    public String name;
    //sounds good, but also creates complexity public ResourcePath contextPath;
    //sounds good, but also creates complexity public String server = null;

    public URL(String name) {
        /*int index = StringUtils.lastIndexOf(name, "/");
        if (index != -1) {
            String path = StringUtils.substring(name, 0, index);
            String fname = StringUtils.substring(name, index + 1);
            this.contextPath = new ResourcePath(path);
            this.name = fname;
        } else {
            this.name = name;
            this.contextPath = new ResourcePath("");
        }*/
        this.name = name;
    }

    /*public URL(ResourcePath path, String name) {
        this(name);
        if (path == null) {
            path = new ResourcePath("");
        }
        if (this.contextPath != null) {
            this.contextPath = path.append(this.contextPath);
        } else {
            this.contextPath = path;
        }
    }*/

    //@Override
   /* public ResourcePath getPath() {
        return contextPath;
    }*/

    //@Override
    public String getName() {
        return name;
    }

    public String getUrl() {
        return name;
    }

    /*@Override
    public boolean isBundled() {
        return false;
    }*/

    /*@Override
    public ResourcePath getBundlePath() {
        return null;
    }*/

    /**
     * server isn't part of full name.
     * Needed?
     */
    //@Override
    /*public String getFullName() {
        if (contextPath != null && contextPath.path != null && StringUtils.length(contextPath.path) > 0) {
            return contextPath.path + "/" + name;
        }
        return name;
    }*/

    /*public String getFullQualifiedName() {
        String b = server;
        if (b == null) {
            // neither set ??
            b = "";
        } else {
            b += "/";
        }
        if (contextPath != null && contextPath.path != null && StringUtils.length(contextPath.path) > 0) {
            return b + contextPath.path + "/" + name;
        }
        return b + name;
    }*/

    /**
     * without "."
     *
     * @return
     */
    //@Override
    public String getExtension() {
        int index = StringUtils.lastIndexOf(name, ".");
        if (index == -1) {
            return "";
        }
        // Der "." koennte auch irgendwo im Pfad sein, aber name ist ohne Pfad
        return StringUtils.substring(name, index + 1);
    }

    @Override
    public String toString() {
        /*String s = name;
        if (contextPath != null) {
            s = "(" + contextPath.path + ")" + s;
        }
        if (server != null) {
            s = server + "/" + s;
        }
        return s;*/
        return getName();
    }

    public boolean isHttp() {
        return StringUtils.startsWith(name, "http");
    }

    /**
     * Name without path and extension.
     *
     * @return
     */
    public String getBasename() {
        String ext = getExtension();
        if (StringUtils.length(ext) == 0) {
            return name;
        }
        return StringUtils.substring(name, 0, StringUtils.indexOf(name, "." + ext));
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
        return new URL(bundlebasedir + "/" + bundleResource.getFullName());
    }

}
