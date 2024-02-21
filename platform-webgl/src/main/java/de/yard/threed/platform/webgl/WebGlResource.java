package de.yard.threed.platform.webgl;

import de.yard.threed.core.Util;
import de.yard.threed.core.resource.NativeResource;
import de.yard.threed.core.resource.ResourcePath;
import de.yard.threed.core.StringUtils;
import de.yard.threed.core.resource.URL;

/**
 * Diese Implementierung ist natuerlich reichlich schlicht. Aber für simple Dinge reicht es erstmal.
 *
 * 19.4.16 Umbenannt von WebGlFile nach WebGlResource, weil dies ja keine explizite Implementierung für FS Zugriff ist.
 * 21.12.16:  So ganz das Whare ist das evtl. noch nicht.
 * Created by thomass on 23.02.16.
 */
public class WebGlResource implements NativeResource {
    private final String path;

    public WebGlResource(String path) {
        this.path = path;
    }

    //@Override
    public boolean exists() {
         Util.notyet();
        return false;
    }

    //@Override
    public String getParent() {
        return (String) Util.notyet();
    }

    //@Override
    public ResourcePath getPath() {
        return (ResourcePath) Util.notyet();
    }

    @Override
    public String getName() {
        return path;
    }

    @Override
    public boolean isBundled() {
        return false;
    }

    public ResourcePath getBundlePath() {
        return null;
    }

    @Override
    public String getFullName() {
        return getName();
    }

    @Override
    public String getFullQualifiedName() {
        Util.notyet();
        return null;
    }

    @Override
    public String getExtension() {
        int index = StringUtils.lastIndexOf(path, ".");
        if (index == -1) {
            return "";
        }
        // Der "." koennte auch irgendwo im Pfad sein, aber name ist ohne Pfad
        return StringUtils.substring(path, index + 1);
    }

    @Override
    public String getBasename() {
        Util.notyet();
        return null;
    }

    @Override
    public URL getUrl() {
        Util.notyet();
        return null;
    }
}
