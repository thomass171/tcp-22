package de.yard.threed.outofbrowser;

import de.yard.threed.core.StringUtils;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;


/**
 *
 * A sync helper for tests and tools.
 *
 * 2.8.21: Extracted from AsyncHelper/BundleLoaderExceptGwt. Not used in GWT.
 */
public class SyncBundleLoader {
    static Log logger = Platform.getInstance().getLog(SyncBundleLoader.class);

    /**
     * static weils nur einmal beim setup aufgerufen wird.
     *
     * @param bundles
     * @param rm
     */
    public static void preLoad(String[] bundles,NativeResourceReader rm) {
        for (String bundlename : bundles) {
            boolean delayed = false;
            //wegen C#
            String bname = bundlename;
            if (StringUtils.endsWith(bname, "-delayed")) {
                //TODO das mit delay ist ne kruecke
                bname = StringUtils.replaceAll(bname, "-delayed", "");
                delayed = true;
            }
            //3.8.21 hier kam fruehe bei einem Fehler ueber AsyncJobCallbackImpl eine Exception
            /*BundleLoaderExceptGwt.*/String e = loadBundleSyncInternal(bname, null, delayed, /*2.8.21 new AsyncJobCallbackImpl(),*/ rm/*AbstractSceneRunner.getInstance().getResourceManager()*/);
            if (e!=null) {
                throw new RuntimeException(e);
            }
        }

    }

    /**
     * Synchrones Laden eines Bundle. Bekommt trotzdem den Listener, um eine
     * identische Anmutung zu haben.
     * Nur fuer Unittests. Und auch die preinitbundle. Aber nicht fuer Apps.
     * 12.6.17: Deprecated weil über platfrom? Oder nur von da? Kann erstmal so bleiben, nicht deprecated.
     * 20.7.17: Ich lass auch einen null listener zu.
     * MA18: Ohne AsyncJobCallback.
     * <p>
     * 3.8.21:loadlistener war wohl eh immer null. Return error message in case of error
     * TODO remove loadjob and move to SyncBundleLoader
     * @param bundlename
     */
    public static String loadBundleSyncInternal(String bundlename, String registername, boolean delayed, /*3.8.21 AsyncJobCallback loadlistener,*/ NativeResourceReader rm) {
        LoadJob loadjob = new LoadJob(bundlename, registername, delayed, /*loadlistener,*/ rm);
        try {
            loadjob.execute();
        } catch (java.lang.Exception e) {
            // schon geloggt
            /*if (loadlistener != null) {
                loadlistener.onFailure(e.getMessage());
            }*/
            return e.getMessage();
        }
        /*if (loadlistener != null) {
            loadlistener.onSuccess();
        }*/
        return null;
    }

}