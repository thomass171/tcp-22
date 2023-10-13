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
    // 3.10.23: TODO does not belong here
    public static String TERRAYSYNCPREFIX = "Terrasync-";
    public static boolean bundledebuglog = false;


    public static Bundle getBundle(String bundlename) {
        return bundles.get(bundlename);
    }

    /**
     * Wird evtl. multithreaded aufgerufen.
     *
     * @param bundlename
     * @param bundle
     */
    public static void registerBundle(String bundlename, Bundle bundle, boolean delayed) {
        if (logger == null) {
            logger = Platform.getInstance().getLog(BundleRegistry.class);
        }
        bundles.put(bundlename, bundle);
        logger.info("Bundle load complete: " + bundle.name + "(" + (bundle.getSizeInBytes() / 1000000) + " MB),delayed=" + delayed);

    }



    public static String[] getBundleNames() {
        return (String[]) bundles.keySet().toArray(new String[0]);
    }



    static public boolean bundleexists(Bundle b, BundleResource br) {
        if (b.exists(br)) {
            return true;
        }
        if (br.getExtension().equals("ac")) {
            br = new BundleResource(br.bundle, StringUtils.substringBeforeLast(br.getFullName(), ".ac") + ".gltf");
            return b.exists(br);
        }
        return false;
    }


    //TODO move to resolver?
    //3.10.23: Its no resolver task, should??? TODO does not belong here. Only for FG? Maybe move to Bundle and extend Bundle?
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
        // logger.debug("Bundles cleared");
    }

    public static void unregister(String bundlename) {
        //bundles.get(bundlename).
        bundles.remove(bundlename);
    }
}
