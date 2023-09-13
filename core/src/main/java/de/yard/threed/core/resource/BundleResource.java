package de.yard.threed.core.resource;

import de.yard.threed.core.StringUtils;
import de.yard.threed.core.Util;

/**
 * Eine mitgelieferte Resource in einem Bundle (Unity, classpath, jar, zip)
 * 21.12.16: Ueberschneidet sich jetzt etwas mit ResourcePath.
 * Aber auch in einem Bundle kann es einen Pfad geben. Der Name
 * ist idealerweise (muss aber nicht) nur der Dateiname. Darum noch eine Ebene bundlepath. Damit isType isbundled() obselet.
 * 10.04.17: Jetzt DER Locator in einem Bundle. Kann optional auch einen Bundlename enthalten (nach resolve, aber auch davor).
 * Auf saubere Tennung des Pfads achten.
 * Ein angegebenes Bundle ist sowas wie ein absoluter Pfad im Unterschied zu einem relativen.
 * <p>
 * 29.11.21: Bad design having bundle here instead of just bundle name? Depends on the use case; before loading a resource or
 * after loading. Loading might include a lookup. Apparently, this class was intended for the second.
 * <p/>
 * 16.8.23: Needs refactoring probably. Maybe split to a new "loaded BundledResource" class and a ResourceLocator.
 * And name sometimes contains a HTTP part. That is confusing. Probably the HTTP part shouldn't be there, because its specific to
 * the environment (like a drive name), not the resource.
 * Created by thomass on 19.04.16.
 */
public class BundleResource implements NativeResource {
    //Real filename without path
    public String name;
    // Der Pfad innerhalb des Bundle. kann null sein. nicht mehr.
    public ResourcePath path;
    // Optional element to allow using this class *before* loading a resource.
    public String bundlename = null;
    public Bundle bundle;

    public BundleResource(String name) {
        int index = StringUtils.lastIndexOf(name, "/");
        if (index != -1) {
            String path = StringUtils.substring(name, 0, index);
            String fname = StringUtils.substring(name, index + 1);
              /*  if (file instanceof BundleResource) {
                    effectivepath = new BundleResource(new ResourcePath(path), fname);
                } else {
                    effectivepath = */
            this.path = new ResourcePath(path);
            this.name = fname;

        } else {

            this.name = name;
            this.path = new ResourcePath("");
        }
    }

    public BundleResource(ResourcePath path, String name) {
        this(name);
        if (path == null) {
            path = new ResourcePath("");
        }
        if (this.path != null) {
            this.path = path.append(this.path);
        } else {
            this.path = path;
        }
    }

    public BundleResource(Bundle bundle, String name) {
        this(name);
        this.bundle = bundle;
    }

    public BundleResource(Bundle bundle, ResourcePath path, String name) {
        this(path, name);
        this.bundle = bundle;
    }

    //@Override
    public boolean exists() {
        //TODO
        return true;
    }

    // @Override
    public String getParent() {
        return null;
    }

    @Override
    public ResourcePath getPath() {
        return path;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isBundled() {
        return true;
    }

    @Override
    public ResourcePath getBundlePath() {
        return null;
    }

    /**
     * 12.6.17: Bundlename isn't part of full name.
     *
     * @return
     */
    @Override
    public String getFullName() {
        if (path != null && path.path != null && StringUtils.length(path.path) > 0) {
            return path.path + "/" + name;
        }
        return name;
    }

    public String getFullQualifiedName() {
        String b = bundlename;
        if (b == null && bundle != null) {
            b = bundle.name;
        }
        if (b == null) {
            // neither set ??
            b = "";
        } else {
            b += ":";
        }
        if (path != null && path.path != null && StringUtils.length(path.path) > 0) {
            return b + path.path + "/" + name;
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
        if (path != null) {
            s = "(" + path.path + ")" + s;
        }
        if (bundle != null) {
            s = bundle.name + ":" + s;
        }
        return s;
    }

    public static BundleResource buildFromFullString(String filename) {
        return new BundleResource(filename);
    }

    /**
     * Never returns null. 30.11.21:Still in use(!?).
     *
     * @return
     */
    public static BundleResource buildFromFullStringAndBundlename(String bundlename, String filename) {
        Util.nomore();
        return null;//TODO MA36 new BundleResource(BundleRegistry.getBundle(bundlename),filename);
    }

    /**
     * Returns null is case of invalid fullQualifiedString.
     */
    public static BundleResource buildFromFullQualifiedString(String fullQualifiedString) {
        if (!StringUtils.contains(fullQualifiedString, ":")) {
            return null;
        }
        BundleResource br = new BundleResource(StringUtils.substringAfter(fullQualifiedString, ":"));
        br.bundlename = StringUtils.substringBefore(fullQualifiedString, ":");
        return br;
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
