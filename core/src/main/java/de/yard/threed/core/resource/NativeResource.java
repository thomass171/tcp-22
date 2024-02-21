package de.yard.threed.core.resource;


/**
 * 19.4.16: A pointer to a resource. Not necessarily a file in a local filesystem, might be also located in a bundle, tar/zip archive (Android?),
 * a CLASSPATH, an asset (Unity/JME) or on a server. So this sounds similar to a URL/URI, but URLs have no hierarchy like filesystems.
 * It is very abstract.
 *
 *
 * 27.5.2016: exists() and getParent() removed. Sound nice, but are difficult to implement. Probably need resource to be loaded.
 *
 * path ist eigentlich doppelt zu name, was ja auch ein Pfad sein kann.
 * 
 * 01.06.2016: path is just an optional prefix, when resource isn't bundled. isBundled() hat Prio, dann wird
 * path ignoriert.
 * 21.12.16: class for path instead of string. But also Bundle can havd a path. The might name
 * ist idealerweise (muss aber nicht) the file name. Darum noch eine Ebene bundlepath. Damit isType isbundled() aber nicht obselet.
 *
 * 26.8.23: Maybe needs refactoring: Shouldn't this be independent from bundle? What really is a path? And name sometimes als contains a path?
 * Maybe a BundleResource is its own interface? Maybe rethink the use cases. Some use cases have file system traversal! Maybe addPath() in ResourcePath?
 * Maybe we need hierarchical resources and non hierarchical (URL). So NativeResource can be the hierarchical.
 *
 * 19.2.24: Decoupled from bundle. and its hierarchical with base,path,name splitting. Hierarchical is important eg. for FG texture loading.
 * Created by thomass on 11.12.15.
 */
public interface NativeResource {

    /**
     * 21.12.16: Returns path in a logical unit. bundlepath is for location of the logical unit.
     * @return
     */
    public ResourcePath getPath();

    public String getName();

    /**
     * isbundled wird trotz bundlepath gebraucht, weil der auch null sein kann.
     * 19.2.24: use case is not quite clear, maybe for probing in FG.
     * @return
     */
    boolean isBundled();

    /**
     * Returns location of logical unit.
     * @return
     */
    //19.2.24 ResourcePath getBundlePath();

    /**
     * path+name
     * No leading "./","/" to be ready to be used as key.
     *
     * @return
     */
    String getFullName();
    String getFullQualifiedName();

        /**
         * without ".". Return "" if there is no extension.
         */
    String getExtension();

    /**
     * Name without path and extension.
     */
    String getBasename();

    /**
     * A helper needed for textures (which might not be loaded by this loader).
     * And in general just an abstraction for a BundleResource (also for FS).
     */
    URL getUrl();

}
