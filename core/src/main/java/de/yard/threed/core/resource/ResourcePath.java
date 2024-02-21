package de.yard.threed.core.resource;

import de.yard.threed.core.StringUtils;

/**
 * Die Abbildung einer Location an der Resources liegen. Kann ins Filesystem, aber auch CLASSPATH (Unity und GWT Bundles), und jar/zip Files zeigen.
 * Ersetzt damit mittelfristig die Implementierungen von NativeResource.
 * Wichtig ist, dass es wirlich nur der Pfad dorthin ist. D.h. alleine spezifiziert es eine Resource nicht.
 * <p>
 * Heisst nicht einfach Path, weil es solche Klassen öfters gibt.
 * 6.4.17: Nochmal: das ist nur ein Pfad.
 * 13.2.24: Might contain bundle prefix, maybe also http. Might be used to build an URL.
 * <p>
 * Created by thomass on 21.12.16.
 */
public class ResourcePath {
    // darf nicht mehr null sein, weil das Unsinn ist.
    // 13.2.24: Traditionally allows leading "/" or "./" (which is really used for file system access).
    // That is completely confusing, esspecially when used as key, eg. in bundle. Now has getPathForKey().
    private String path;

    /**
     * Eine bundled resource
     */
    public ResourcePath() {
        this.path = "";
    }

    public ResourcePath(String path) {
        if (path == null) {
            throw new RuntimeException("path is null");
        }
        this.path = path;
    }

    public ResourcePath append(ResourcePath p) {
        ResourcePath result = new ResourcePath(path);
        if (StringUtils.length(p.path) == 0) {
            return result;
        }

        if (StringUtils.length(path) == 0) {
            result.path = p.path;
        } else {
            result.path += "/" + p.path;
        }

        return result;
    }

    public String getPath() {
        return path;
    }

    /**
     * 14.2.24: To be more consistent in maps, eg. bundle directory.
     */
    public String getPathForKey() {
        if (StringUtils.startsWith(path, "./")) {
            return StringUtils.substring(path, 2);
        }
        if (StringUtils.startsWith(path, "/")) {
            return StringUtils.substring(path, 1);
        }
        if (path.equals(".")) {
            return "";
        }
        return path;
    }

    @Override
    public String toString() {
        return path;
    }
}
