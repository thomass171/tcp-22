package de.yard.threed.core.resource;

import de.yard.threed.core.StringUtils;
import de.yard.threed.core.Util;

/**
 * Not needed?
 * Created by thomass on 07.11.23.
 */
public class UrlResource implements NativeResource {
    //Real filename without path
    public String name;
    public ResourcePath contextPath;
    public String server = null;

    public UrlResource(String name) {
        int index = StringUtils.lastIndexOf(name, "/");
        if (index != -1) {
            String path = StringUtils.substring(name, 0, index);
            String fname = StringUtils.substring(name, index + 1);
             this.contextPath = new ResourcePath(path);
            this.name = fname;
        } else {
            this.name = name;
            this.contextPath = new ResourcePath("");
        }
    }

    public UrlResource(ResourcePath path, String name) {
        this(name);
        if (path == null) {
            path = new ResourcePath("");
        }
        if (this.contextPath != null) {
            this.contextPath = path.append(this.contextPath);
        } else {
            this.contextPath = path;
        }
    }

    @Override
    public ResourcePath getPath() {
        return contextPath;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isBundled() {
        return false;
    }

    @Override
    public ResourcePath getBundlePath() {
        return null;
    }

    /**
     * server isn't part of full name.
     */
    @Override
    public String getFullName() {
        if (contextPath != null && contextPath.path != null && StringUtils.length(contextPath.path) > 0) {
            return contextPath.path + "/" + name;
        }
        return name;
    }

    public String getFullQualifiedName() {
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
    }

    /**
     * without "."
     *
     * @return
     */
    @Override
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
        String s = name;
        if (contextPath != null) {
            s = "(" + contextPath.path + ")" + s;
        }
        if (server != null) {
            s = server + "/" + s;
        }
        return s;
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
}
