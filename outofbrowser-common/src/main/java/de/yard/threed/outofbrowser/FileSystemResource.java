package de.yard.threed.outofbrowser;


import de.yard.threed.core.StringUtils;
import de.yard.threed.core.Util;
import de.yard.threed.core.resource.NativeResource;
import de.yard.threed.core.resource.ResourcePath;

/**
 * Eine Resource als simple Datei im FS.
 * Irgendwie doppelt zu JAFile.
 * Ja, aber nur irgendiwe. Aber diese Klasse wird in der Engine so wie BundledResource verwendet. Hier darf z.B. kein Java.io verwendet werden.
 * 04.07.21:Das muesste ja eigentlich in die platform? Merkwuerdige Konstruktion. Wird viel in SG verwendet, obwohl es NativeResource (Bundle) gibt.
 * Darum kommt das mal nach FG. MA36. Doch java-common, weils zu viel genutzt wird.
 * 7.8.21: Konzeptionell ist das doch eher ein Hilfsmittel. Naja, wie auch immer, das ist eigentlich schon
 * platformunaghaengig, ausser GWT;aber auch C#. Darum von java-common nach module-outofbrowser.
 *
 * Created by thomass on 19.04.16.
 */
public class FileSystemResource implements NativeResource {
    String name;
    ResourcePath path;

    /**
     * Der Name kann relativ/absolut sein und auf eine Datei oder ein Verzeichnis verweisen. Also sehr allgemeingueltig.
     *
     * @param name
     */
    public FileSystemResource(String name) {
        if (StringUtils.contains(name, "\\")) {
            // obscure windows path separator.
            name = StringUtils.replaceAll(name, "\\", "/");
        }
        this.name = name;
    }

    /**
     * Optional mit Pfadprefix
     *
     * @param name
     */
    public FileSystemResource(ResourcePath path, String name) {
        this(name);
        this.path = path;
    }

    //@Override
    public boolean exists() {
        //MA36
        Util.nomore();
        return true;
        //return ((Platform)Platform.getInstance()).exists(this);
    }

    //@Override
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
        return false;
    }

    /**
     * Hat keinen bundlepath
     *
     * @return
     */
    @Override
    public ResourcePath getBundlePath() {
        return null;
    }

    @Override
    public String getFullName() {
        if (path != null) {
            return path.path + "/" + name;
        }
        return name;
    }

    @Override
    public String getExtension() {
        int index = StringUtils.lastIndexOf(name, ".");
        if (index == -1) {
            return "";
        }
        // Der "." koennte auch irgendwo im Pfad sein, aber name ist ohne Pfad
        return StringUtils.substring(name, index + 1);
    }
    
    public static FileSystemResource buildFromFullString(String filename) {
        int index = StringUtils.lastIndexOf(filename, "/");
        if (index != -1) {
            String path = StringUtils.substring(filename, 0, index);
            String fname = StringUtils.substring(filename, index + 1);
              /*  if (file instanceof BundleResource) {
                    effectivepath = new BundleResource(new ResourcePath(path), fname);
                } else {
                    effectivepath = */
            return new FileSystemResource(new ResourcePath(path), fname);

        }
        return new FileSystemResource(filename);
    }
}
