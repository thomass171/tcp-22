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
 * Created by thomass on 11.12.15.
 */
public interface NativeResource {

    /**
     * 21.12.16: Liefert den Pfad im Bundle, seit es den bundlepath gibt.
     * @return
     */
    public ResourcePath getPath();

    public String getName();

    /**
     * isbundled wird trotz bundlepath gebraucht, weil der auch null sein kann.
     * @return
     */
    boolean isBundled();

    /**
     * Liefert den uebergeordneten Pfad zu dem "buindle", oder jar, oder sonst was.
     * @return
     */
    ResourcePath getBundlePath();

    /**
     * path+name
     * @return
     */
    String getFullName();
    
    String getExtension();
}
