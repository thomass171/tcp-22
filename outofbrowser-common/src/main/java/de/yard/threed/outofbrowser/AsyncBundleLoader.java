package de.yard.threed.outofbrowser;

import de.yard.threed.core.Pair;
import de.yard.threed.core.platform.Config;
import de.yard.threed.core.platform.NativeBundleLoader;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.*;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.resource.BundleRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Loading bundles in an async way that is similar to the loading in Javascript/browser/GWT.
 * <p>
 * <p>
 * 2.8.21: Extracted from AsyncHelper/BundleLoaderExceptGwt. Not used in GWT.
 */
public class AsyncBundleLoader implements NativeBundleLoader {
    static Log logger = Platform.getInstance().getLog(AsyncBundleLoader.class);
    // Wird verwendet, um das einbauen der async/MT geladenen Model im Hauptthread zu machen (wegen JME).
    // kein asyncjob, weils intern ist. Koennte evtl. trozdem zusammengefuehrt werden. Schick ist das nicht
    //static Vector<Integer> modelbuilddelegates = new Vector<Integer>();
    //static Vector<ModelBuildData> modelbuildvalues = new Vector<ModelBuildData>();
    //static Vector<Integer> bundleloaddelegates = new Vector<Integer>();
    static Vector<BundleLoadData> bundleloadvalues = new Vector<BundleLoadData>();
    NativeResourceReader resourceReader;

    public AsyncBundleLoader(NativeResourceReader resourceReader) {
        this.resourceReader = resourceReader;
    }

    /**
     * MA18: Fuer ein Bundle.
     */
    public void asyncBundleLoad(String bundlename, BundleLoadDelegate bundleLoadDelegate, boolean delayed) {
        if (Config.isAsyncdebuglog()) {
            logger.debug("scheduling async bundle load for " + bundlename);
        }
        //Sicherheitshalber mal prefen.
        if (Platform.getInstance().hasOwnAsync()) {
            throw new RuntimeException("invalid usage of AsyncHelper");
        }
        //bundleloaddelegates.add(delegateid);
        bundleloadvalues.add(new BundleLoadData(bundlename, bundleLoadDelegate/*2.8.21id*/, delayed));
    }

    /**
     * public fuer Tests
     */
    @Override
    public List<Pair<BundleLoadDelegate, Bundle>> processAsync() {

        //Sicherheitshalber mal prefen. Wird aber offenbar auch in webgl verwendet.Siehe header.
        if (Platform.getInstance().hasOwnAsync()) {
            throw new RuntimeException("invalid usage of AsyncHelper");
        }

        List<Pair<BundleLoadDelegate, Bundle>> result = new ArrayList<Pair<BundleLoadDelegate, Bundle>>();

        for (int i = 0; i < bundleloadvalues.size(); i++) {
            BundleLoadData d = bundleloadvalues.get(i);
            String bundlename = d.bundlename;
            if (Config.isAsyncdebuglog()) {
                logger.debug("processing async bundle load for " + bundlename);
            }
            Bundle b;
            if ((b = BundleRegistry.getBundle(bundlename)) != null) {
                //dann nicht mehrfach laden
                if (Config.isAsyncdebuglog()) {
                    logger.debug("Bundle already loaded");
                }
            } else {
                /*rm.  3.8.21 ging mal ueber BundleLoaderExceptGwt*/
                //5.8.21 loadBundle(bundlename, d.delayed, rm);
                ResourcePath bundlebasedir = BundleResolver.resolveBundle(bundlename, Platform.getInstance().bundleResolver);
                SyncBundleLoader.loadBundleSyncInternal(bundlename, null, d.delayed, resourceReader, bundlebasedir);
                // TODO MT sicher machen und Fehlerbehandlung
                // Es gibt keine PArameter. Das Bundle ist einfach da, oder nicht.
                // 2.3.18: Wenns in der Signatur steht, erwartet man das aber, darum doch.
                b = BundleRegistry.getBundle(bundlename);
            }
            //3.8.21 AbstractSceneRunner.getInstance().bundledelegateresult.put(d.delegateid, b);
            result.add(new Pair<BundleLoadDelegate, Bundle>(d.delegateid, b));
        }
        //bundleloaddelegates.clear();
        bundleloadvalues.clear();

        /*for (AsyncInvoked<AsyncHttpResponse> asyncInvoked : AbstractSceneRunner.getInstance().invokedLater) {
            asyncInvoked.run();
        }
        AbstractSceneRunner.getInstance().invokedLater.clear();*/
        return result;
    }

    /**
     * eigentlich nur fuer Tests
     */
    public static void cleanup() {
        // modelbuilddelegates.clear();

        //modelbuildopttexturepath.clear();
        //bundleloaddelegates.clear();
        bundleloadvalues.clear();
    }

    /**
     * Vier Methoden aus BundleLoaderExceptGwt
     * asynchron und multithreaded.
     * MA18: Ohne AsyncJobCallback und nicht MT.
     */
    /*5.8.21 public static void loadBundle(String bundlename/*, AsyncJobCallback loadlistener* /, boolean delayed, ResourceManager rm) {

        //Platform.getInstance().addAsyncJob(new LoadJob(bundlename, loadlistener));
        loadBundleSyncInternal(bundlename, null, delayed, /*null,* / rm/*AbstractSceneRunner.getInstance().getResourceManager()* /);
    }*/


    /**
     * Nicht relevant, weil sync geladen wird.
     *
     * @param file
     * @return
     */
    @Override
    public boolean isLoading(BundleResource file) {
        return false;
    }

    /**
     * Immediately load single file of a bundle.
     */
    public void completeBundle(BundleResource file/*, ResourceManager rm*/) {
        Bundle bundle = file.bundle;
        //ResourceManager rm = AbstractSceneRunner.getInstance().getResourceManager();

        //10.9.21 String bundlebasedir = BundleRegistry.getBundleBasedir(bundle.name, false);
        ResourcePath bundlebasedir = BundleResolver.resolveBundle(bundle.name, Platform.getInstance().bundleResolver);
        if (bundlebasedir == null) {
            throw new RuntimeException("bundle base dir not set for bundle " + bundle.name);
        }
        String resource = bundlebasedir + "/" + BundleRegistry.getDirectoryName(bundle.name, false);
        String filename = file.getFullName();
        resource = bundlebasedir.getPath() + "/" + filename;
        SyncBundleLoader.loadBundleData(bundle, resource, filename, false, resourceReader/*AbstractSceneRunner.getInstance().getResourceManager()*/);

    }


}


class BundleLoadData {
    String bundlename;
    //2.8.21 warum war das wohl die Id
    //int delegateid;
    BundleLoadDelegate delegateid;
    boolean delayed;

    public BundleLoadData(String bundlename, BundleLoadDelegate/*int*/ delegateid, boolean delayed) {
        this.bundlename = bundlename;
        this.delayed = delayed;
        this.delegateid = delegateid;
    }
}

