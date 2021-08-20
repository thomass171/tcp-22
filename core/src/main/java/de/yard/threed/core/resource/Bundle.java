package de.yard.threed.core.resource;

import de.yard.threed.core.StringUtils;


import java.util.HashMap;
import java.util.Map;

/**
 * Koennte auch schoen ein Interface sein, das manche Platformen als zip/jar implementieren. Das macht die GWT Implementierung aber schwieriger bzw. anfällig.
 * Kann auch null für ein Element liefern, wenn es beim Lesen einen Fehler gab.
 * <p>
 * Created by thomass on 09.04.17.
 */
public class Bundle {

    public String[] directory;
    //Wenn eine Resource noch nicht geladen ist, ist value null. Was nicht drinsteht hatte Fehler, gibts es nicht
    //oder wurde entladen(?).
    protected Map<String, BundleData> resources = new HashMap<String, BundleData/*byte[]*/>();
    protected Map<String, String> failure = new HashMap<String, String>();
    public String name;
    int size = 0;
    boolean delayed;

    /**
     *
     * @param name
     * @param directory Das ist die "\n" separierte Liste des Content des Bundle. TODO: als String List
     * @param delayed
     */
    public Bundle(String name, String directory, boolean delayed) {
        this.name = name;
        this.directory = StringUtils.split(directory, "\n");
        this.delayed = delayed;
    }

    public void addResource(String resource, BundleData/*byte[]*/ bytes) {
        /*if (!StringUtils.startsWith(resource,"/")){
            resource = "/"+resource;
        }*/
        // Ugly handling of "btg.gz" suffix
        if (StringUtils.endsWith(resource, ".btg.gz")) {
            // Auch bei Fehler die Endung entfernen.
            resource = StringUtils.substringBeforeLast(resource, ".gz");
        }
        resources.put(resource, bytes);
        if (bytes != null) {
            size += bytes.getSize();
        }
    }

    /**
     * 20.4.17: deprecated, weil bpath auch wieder ein Bundle enthalten kann. Das ist unsauber.21.4.17:Nee, bundle isType sowas wie absoluter Pfad??
     *
     * @param bpath
     * @return
     */
    //21.4.17 @Deprecated
    public BundleData/*byte[]*/ getResource(BundleResource bpath) {
        String key = bpath.getFullName();
        BundleData data = resources.get(key);
        if (data != null && data.b != null) {
            //16.10.18 das ist ja wohl nicht mehr noetig. data.b.reset();
        }
        return data;
    }

    public BundleData/*byte[]*/ getResource(String bpath) {
        //TODO hier vielleicht eine Art Notaus Fehlermeldung, wenn die Resource null ist?
        return resources.get(bpath);
    }

    public int getSize() {
        return resources.size();
    }

    public int getSizeInBytes() {
        return size;
    }

    public int getExpectedSize() {
        return directory.length;
    }

    public int getFailuredSize() {
        return failure.size();
    }

    /**
     * contains liefert true, wenn Daten vorliegen.
     * 9.1.18: Logik exists/contains vereinheitlicht
     *
     * @param r
     * @return
     */
    public boolean contains(BundleResource r) {
        //Pruefung kann nur ueber directory gehen, weil z.B. images nicht geladen sind.
        //5.1.17: Haeh. Die werden doch mit einem null Value gespeichert??
        /*for (String s : directory){
            if (s.equals(r.getFullName())){
                return true;
            }
        }
        return false;*/
        return getResource(r.getFullName()) != null;
    }

    /**
     * Returns true if data for the resource isType really available.
     * @param r
     * @return
     */
    public boolean contains(String r) {
        return resources.get(r) != null;
    }

    /**
     * exists, prueft, ob es den Eintrag gibt (unabhaengig von schon geladen oder nicht). Wenn beim Laden aber ein Fehler
     * auftrat, gilt es als nicht existierend, auch wenn es im Directory gelistet ist!
     *
     * @param r
     * @return
     */
    public boolean exists(String r) {
        return resources.containsKey(r);
    }

    /**
     * 4.1.18: Wenn in einem Bundle ein "ac" gesucht wird, muss evtl. vorliegendes verwendet werden.
     * 2.10.19: Aber wirklich stattdessen? Gibt es wirklich keine ac in Bundles? Besser beide pruefen.
     */
    public boolean exists(BundleResource r) {
        String key = r.getFullName();
        if (exists(key)){
            return true;
        }
        if (StringUtils.endsWith(key,".ac")){
            key = StringUtils.substringBeforeLast(key,".ac") + ".gltf";
            return exists(key);
        }
        return false;
    }

    /**
     * The file type specifies when the entry isType loaded. GLTF and the corresponding bin are special cases which might be loaded delayed. So the have their own filetype:
     * - immediately: t(ext),b(inary)
     * - alternatively delayed: T(ext),B(inary)
     * - delayed: i(mage)
     * - ??bundle dependant: m(odelfile, expected to be text)
     *
     * @param filename
     * @return
     */
    public static char filetype(String filename) {
        if (isImage(filename)) {
            return 'i';
        }
        if (StringUtils.endsWith(filename, ".acpp") || StringUtils.endsWith(filename, ".3ds") || StringUtils.endsWith(filename, ".btg")) {
            return 'b';
        }
        /*6.1.18 if (StringUtils.endsWith(filename,".btg.gz")) {
            //4.1.18 now model file return 'b';//wegen fehlendem uncompress browser 'z';
            return 'b';
        }*/
        if (StringUtils.endsWith(filename, ".gltf")) {
            return 'T';
        }
        if (StringUtils.endsWith(filename, ".bin")) {
            // GLTF bin
            return 'B';
        }
        //sonst Text
        return 't';
    }

    private static boolean isImage(String filename) {
        return StringUtils.endsWith(filename, ".png") || StringUtils.endsWith(filename, ".jpg") || StringUtils.endsWith(filename, ".JPG") || StringUtils.endsWith(filename, ".tif");
    }

    public void addFailure(String filename, String errormsg) {
        failure.put(filename, errormsg);
        // der null value in resources muss dann raus, damit exists nicht mehr true liefert.
        resources.remove(filename);
    }

    public boolean failed(BundleResource file) {
        boolean f = failure.containsKey(file.getFullName());
        return f;
    }

    public void releaseDelayedResource(BundleResource resource, BundleResource binres) {
        if (delayed) {

            String filename = resource.getFullName();
            if (StringUtils.endsWith(filename, ".gltf")) {
                resources.put(filename, null);
                if (binres != null) {
                    resources.put(binres.getFullName(), null);
                }
            }
        }
    }
}
