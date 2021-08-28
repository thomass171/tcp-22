package de.yard.threed.core.resource;


import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by thomass on 10.04.17.
 */
public class BundleRegistry {
    //logger geht hier nicht static, weil das zu frueh ist. Darum erst apeater bei Gebrauch anlegen.
    static Log logger;
    // TODO use HashTable.  HashTable.put isType thread safe. C# missing
    static Map<String, Bundle> bundles = new HashMap<String, Bundle>();
    //Ueber Provider lassen sich relative Resourcen über Bundle hinweg suchen.
    static private List<BundleResourceProvider> providerlist = new ArrayList<BundleResourceProvider>();
    public static String TERRAYSYNCPREFIX = "Terrasync-";
    public static String FGROOTCOREBUNDLE = "fgrootcore";
    //12.10.18: FGHOME Bundle ist leer und wird auch nicht mehr gebraucht, weil FG eh nicht 1:1 portiert wird
    //Wenn es da mal wichtige Daten gibt, bilden die halt ein neues Bundle
    //public static String FGHOMECOREBUNDLE = "fghomecore";
    public static boolean bundledebuglog = false;
    // 26.7.21: Rekonstruiert aus Historie: Muss true sein, damit FG_HOME nicht mehr gesetzt sein muss (ca 2018).
    public static boolean customTerraSync = true;

    public static Bundle getBundle(String bundlename) {
        return bundles.get(bundlename);
    }

    /**
     * Wird evtl. multithreaded aufgerufen.
     *
     * @param bundlename
     * @param bundle
     */
    public static void registerBundle(String bundlename, Bundle bundle) {
        if (logger==null){
            logger = Platform.getInstance().getLog(BundleRegistry.class);
        }
        bundles.put(bundlename, bundle);
        logger.info("Bundle load complete: " + bundle.name + "(" + (bundle.getSizeInBytes() / 1000000) + " MB)");

    }

    public static void addProvider(BundleResourceProvider provider) {
        providerlist.add(provider);
    }

    public static void removeAircraftSpecific() {
        for (int i=providerlist.size()-1;i>=0;i--){
            if (providerlist.get(i).isAircraftSpecific()){
                providerlist.remove(i);
            }
        }
    }

    public static int getProviderCount(){
        return providerlist.size();
    }

    public static String[] getBundleNames() {
        return (String[]) bundles.keySet().toArray(new String[0]);
    }

    /**
     * 1) Current context(bundle)
     * 2) search provider list
     * <p>
     * Returns null if resource not found.
     * context isType current "location" in a bundle.
     * Da muss aber der Pfad drin gesetzt sein. Da kann man auch direkt den Pfad uebergeben. TODO
     * <p>
     * // FG hat im CurrentAircraftDirProvider einen Nebeneffekt, der absolute Pfade beachtet. Ob das Absicht ist?
     * Es ist nicht erkennbar, wo FG absolute Pfade abdeckt. Darum hier extra eingebaut. Das gibts in FG nicht.
     * 12.6.17: Ist das nicht jetzt über die absolute Suche im Bundle geloest?
     * 4.1.18: Wenn in einem Bundle ein "ac" gesucht wird, muss der exists auch bei vorliegendem gltf true liefern.
     */
    public static BundleResource findPath(String resource, BundleResource current/*currentbundle*/) {
        if (current != null && current.bundle != null) {
            //TODO improved path append
            /*if (current.getPath()==null){
                throw new RuntimeException("no path");
            }*/
            BundleResource br = new BundleResource(current.getPath(), resource);
            if (bundleexists(current.bundle,br)) {
                br.bundle = current.bundle;
                return br;
            }
            //12.6.17: absolute Suche im Bundle
            br = new BundleResource(resource);
            if (bundleexists(current.bundle,br)) {
                br.bundle = current.bundle;
                return br;
            }
        }

        // TODO fileseparator
      /*  if (StringUtils.indexOf(aResource, "/") != -1) {
            SGPath r = new SGPath(aResource);
            if (r.exists()) {
                return r;
            }
        }*/

        for (BundleResourceProvider rp : providerlist) {
            BundleResource path = rp.resolve(resource/*, null*/);
            if (path != null) {
                return path;
            }
        }

        return null;
    }

    static public boolean bundleexists(Bundle b, BundleResource br){
        if (b.exists(br)){
            return true;
        }
        if (br.getExtension().equals("ac")){
            br = new BundleResource(br.bundle,StringUtils.substringBeforeLast(br.getFullName(),".ac")+".gltf");
            return b.exists(br);
        }
        return false;
    }
    /**
     * 9.6.17: Provisorium hier erstmal zentral untergebracht.
     *
     * @return
     */
    /*MA31 public static Bundle getTerrasyncBundle(String bucket) {
        return BundleRegistry.getBundle(FlightGear.getBucketBundleName(bucket));
    }*/

    /**
     * Liefert das "Verzeichnis", in dem das Directory erwartet wird.
     *
     * @param bundlename
     * @param perhttp
     * @return
     */
    public static String getBundleBasedir(String bundlename, boolean perhttp) {
        if (StringUtils.startsWith(bundlename, BundleRegistry.TERRAYSYNCPREFIX)) {
            // bucket or model bundle
            String effname = StringUtils.substringAfter(bundlename, "-");
            if (perhttp) {
                if (customTerraSync) {
                    return "bundles/TerraSync";
                }else{
                    return "TerraSync";
                }
            }
            if (customTerraSync){
                return Platform.getInstance().bundledir + "/TerraSync";
            }else {
                // 25.7.21: Dieser Zweig soll wohl seit 2018 gar nicht mehr genutzt werden.
                String fghome = Platform.getInstance().getSystemProperty("FG_HOME");
                if (fghome == null) {
                    //26.7.21:Besser aussteigen als nachher ewig die URsache zu suchen
                    throw new RuntimeException("fghome is null");
                }
                return fghome + "/TerraSync";
            }

        }
        /*30.1.18: normales Bundle  if (bundlename.equals(SGMaterialLib.BUNDLENAME) || StringUtils.startsWith(bundlename, BundleRegistry.FGROOTCOREBUNDLE)) {
            if (perhttp) {
                return "fg_root";
            }
            String fgroot = Platform.getInstance().getSystemProperty("FG_ROOT");
            if (fgroot == null){
                //loggen geht hier nicht.
                //System.out.println("FG_ROOT not set as system property");
            }
            return fgroot;
        }
       if (StringUtils.startsWith(bundlename, BundleRegistry.FGHOMECOREBUNDLE)) {
            if (perhttp) {
                return "fg_home";
            }
            String fghome = Platform.getInstance().getSystemProperty("FG_HOME");
            if (fghome == null){
                //loggen geht hier nicht.
                //System.out.println("FG_HOME not set as system property");
            }
            return fghome;
        }*/
        // Dann ist es ein Standardbundle.
        if (perhttp) {
            return "bundles/" + bundlename;
        }
        return Platform.getInstance().bundledir + "/" + bundlename;

    }

    public static String getDirectoryName(String bundlename, boolean b) {
        if (StringUtils.startsWith(bundlename, BundleRegistry.TERRAYSYNCPREFIX)) {
            // bucket or model bundle
            String effname = StringUtils.substringAfter(bundlename, "-");
            return "directory-" + effname + ".txt";
        }
         /*30.1.18: normales Bundle if (bundlename.equals(SGMaterialLib.BUNDLENAME)) {
            return "directory-" + SGMaterialLib.BUNDLENAME + ".txt";
        }
        if (bundlename.equals(BundleRegistry.FGHOMECOREBUNDLE)) {
            return "directory-fghomecore.txt";
        }
        if (bundlename.equals(BundleRegistry.FGROOTCOREBUNDLE)) {
            return "directory-fgrootcore.txt";
        }*/
        // Dann ist es ein Standardbundle.
        return "directory.txt";

    }

    public static void clear() {
        bundles.clear();
        //30.9.19: Auch provider
        providerlist.clear();
       // logger.debug("Bundles cleared");
    }

    public static void unregister(String bundlename) {
        //bundles.get(bundlename).
        bundles.remove(bundlename);
    }
}
