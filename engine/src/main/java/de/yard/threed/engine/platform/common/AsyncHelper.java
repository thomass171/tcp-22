package de.yard.threed.engine.platform.common;

import de.yard.threed.core.BuildResult;
import de.yard.threed.core.Pair;
import de.yard.threed.core.Util;
import de.yard.threed.core.buffer.NativeByteBuffer;
import de.yard.threed.core.platform.AsyncHttpResponse;
import de.yard.threed.core.platform.AsyncJobDelegate;
import de.yard.threed.core.platform.Config;
import de.yard.threed.core.platform.NativeBundleLoader;
import de.yard.threed.core.platform.NativeFuture;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.*;
import de.yard.threed.core.platform.Log;
import de.yard.threed.engine.SceneNode;


import de.yard.threed.engine.loader.LoaderGLTF;
import de.yard.threed.engine.loader.PortableModelList;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Utils für die Nachbildung von async Operationen fuer Platformen, die wohl MT können, aber nicht mögen.(JME, Unity, OpenGL?).
 * Aus Gruenden der Einheitlichkeit wird es auch von GWT verwendet!
 * Aber nur fuer model build und invoke later, nicht fuer bundle laden. Total verwirrend.
 * <p>
 * 3.12.18: Kann das nicht komplett in den AbstractSceneRunner?
 * 5.7.21: Ob das wirklich eine gute Idee ist? Evtl., aber nicht so dringlich.
 * 2.8.21: Bundle load extracted to AsyncBundleLoader.
 */
public class AsyncHelper {
    static Log logger = Platform.getInstance().getLog(AsyncHelper.class);
    // Wird verwendet, um das einbauen der async/MT geladenen Model im Hauptthread zu machen (wegen JME).
    // kein asyncjob, weils intern ist. Koennte evtl. trozdem zusammengefuehrt werden. Schick ist das nicht
    //static Vector<Integer> modelbuilddelegates = new Vector<Integer>();
    static Vector<ModelBuildData> modelbuildvalues = new Vector<ModelBuildData>();
    //static Vector<Integer> bundleloaddelegates = new Vector<Integer>();
    //2.8.21 static Vector<BundleLoadData> bundleloadvalues = new Vector<BundleLoadData>();

    /**
     * Fuer ein Model.
     *
     * @param value
     * @param delegateid
     */
    public static void asyncModelBuild(BundleResource value, ResourcePath opttexturepath, int loaderoptions, int delegateid) {
        // modelbuilddelegates.add(delegateid);
        if (loaderoptions == 0) {
            //debug hook
            loaderoptions = 0;
        }
        modelbuildvalues.add(new ModelBuildData(value, opttexturepath, loaderoptions, delegateid));

    }

    /**
     * MA18: Fuer ein Bundle.
     */
    /*2.8.21 public static void asyncBundleLoad(String bundlename, int delegateid, boolean delayed) {
        if (Config.isAsyncdebuglog()) {
            logger.debug("scheduling async bundle load for " + bundlename);
        }
        //Sicherheitshalber mal prefen.
        if (Platform.getInstance().hasOwnAsync()) {
            throw new RuntimeException("invalid usage of AsyncHelper");
        }
        //bundleloaddelegates.add(delegateid);
        bundleloadvalues.add(new BundleLoadData(bundlename, delegateid, delayed));
    }*/

    /**
     * public fuer Tests
     */
    static public void processAsync(/*ResourceManager rm,*/ NativeBundleLoader bundleLoader) {

        //Sicherheitshalber mal prefen. Wird aber offenbar auch in webgl verwendet.Siehe header.
        if (Platform.getInstance().hasOwnAsync()) {
            //throw new RuntimeException("invalid usage of AsyncHelper");
        }
        //TODO threadsafe machen. und huebsch? Naja, MT ist es ja nicht.
        // models
        for (int i = modelbuildvalues.size() - 1; i >= 0; i--) {
            ModelBuildData data = modelbuildvalues.get(i);
            BundleResource modelresource = data.value;
            ResourcePath opttexturepath = data.opttexturepath;
            BuildResult r = attemptModelLoad(/*rm,*/ modelresource, opttexturepath, data.loaderoptions, data.delegateid, bundleLoader);
            if (r != null) {
                // MT sicher machen? ist aber eigentlich nicht MT.
                AbstractSceneRunner.getInstance().delegateresult.put(data.delegateid, r);
                modelbuildvalues.remove(i);
            } else {
                //no warning
                if (Config.isAsyncdebuglog()) {
                    logger.debug("no bundle content yet for " + modelresource.getFullName());
                }
            }

        }
        //modelbuilddelegates.clear();
        // modelbuildvalues.clear();
        //modelbuildopttexturepath.clear();

        // bundles
       /*2.8.21  for (int i = 0; i < bundleloadvalues.size(); i++) {
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
                rm.loadBundle(bundlename, d.delayed);
                // TODO MT sicher machen und Fehlerbehandlung
                // Es gibt keine PArameter. Das Bundle ist einfach da, oder nicht.
                // 2.3.18: Wenns in der Signatur steht, erwartet man das aber, darum doch.
                b = BundleRegistry.getBundle(bundlename);
            }
            AbstractSceneRunner.getInstance().bundledelegateresult.put(d.delegateid, b);

        }
        //bundleloaddelegates.clear();
        bundleloadvalues.clear();*/

        for (AsyncInvoked<AsyncHttpResponse> asyncInvoked : AbstractSceneRunner.getInstance().invokedLater) {
            asyncInvoked.run();
        }
        AbstractSceneRunner.getInstance().invokedLater.clear();

        //TODO make whole block  MT safe
        List<Pair<NativeFuture, AsyncJobDelegate>> remainingFutures = new ArrayList<>();
        for (Pair<NativeFuture, AsyncJobDelegate> p : AbstractSceneRunner.getInstance().futures) {
            if (p.getFirst().isDone()) {
                p.getSecond().completed(p.getFirst().get());
            } else {
                remainingFutures.add(p);
            }
        }
        AbstractSceneRunner.getInstance().futures = remainingFutures;
    }

    /**
     * Versuchen ein Model zu laden. Geht nur wenn die Daten vorliegen.
     * <p>
     * Liefert null, wenn das Model nicht geladen wurde, weil noch Daten fehlen. Wenn der Load/Build aber
     * scheiterte, wird trotzdem ein wenn auch leeres Result geliefert.
     * <p>
     * Das geht aber doch wohl nur fuer "einfache", nicht XML Model.
     *
     * @param file
     * @param opttexturepath
     * @param loaderoptions
     * @return
     */
    private static BuildResult attemptModelLoad(/*ResourceManager rm,*/ BundleResource file, ResourcePath opttexturepath, int loaderoptions, int delegateid, NativeBundleLoader bundleLoader) {
        if (Config.isAsyncdebuglog()) {
            logger.debug("processing async model build for " + file);
        }
        if (file.bundle == null) {
            logger.error("bundle not set for file " + file.getName());
            return new BuildResult("failure");
        }
        // wenn die Daten im Bundle (noch) nicht vorliegen, diesen Request skippen
        BundleData ins = file.bundle.getResource(file);
        if (ins == null) {
            if (Config.isAsyncdebuglog()) {
                logger.debug(file.getName() + " not found in bundle " + file.bundle.name);
            }
            if (delayedContentload(/*rm,*/ file, bundleLoader)) {
                return new BuildResult("failure");
            }
            return null;
        }
        // wenn es ein GLTF ist, pruefen ob das "bin" auch (schon) da ist. Dann kann das Model gebaut werden.
        // Sonst weiter warten.
        BundleResource binres = null;
        if (file.getExtension().equals("gltf")) {
            binres = LoaderGLTF.getBinResource(file);
            NativeByteBuffer binbuffer = null;
            if (file.bundle.exists(binres) && !file.bundle.contains(binres)) {
                // muesste drin sein, ist aber nicht. 10.10.18: Warum muesste es schon geladen sein?
                // Ist wohl nur verwirrend. Natürlich kann es delayed sein und wird nachgeladen. Darum hier auch kein Warning.
                if (Config.isAsyncdebuglog()) {
                    logger.debug("binres " + binres.getName() + " not found in bundle " + file.bundle.name);
                }
                // Content evtl. async nachladen
                if (delayedContentload(/*rm,*/ binres, bundleLoader)) {
                    return new BuildResult("failure");
                }
                return null;
            }
        }

        PortableModelList lr;
        try {
            lr = ModelLoader.readModelFromBundle(file, true, loaderoptions);
            if (lr == null) {
                // Fehler. Ist bereits gelogged.
                return new BuildResult((new SceneNode()).nativescenenode);
            }
        } catch (java.lang.Exception e) {
            logger.error("Exception caught:", e);
            return new BuildResult(e.getMessage());
        }
        BuildResult r = ModelLoader.buildModelFromBundle(lr, file, opttexturepath, loaderoptions);
        // 16.1.18: The one and only info log for building a model.
        String nodeisnull = (r.getNode() == null) ? " but node isType null." : "";
        logger.info("Model " + file.getFullName() + " built. Loading took " + lr.loaddurationms + " ms." + nodeisnull + " delegateid=" + delegateid);
        // Den Bundleinhalt evtl. wieder freigeben. 25.1.18: Erstmal rausgenommen bis klar ist, ob das bei shared modeln nicht zu built Fehlern führt.
        // Wegen Resourcen doch wieder.
        file.bundle.releaseDelayedResource(file, binres);
        return r;
    }

    /**
     * Check for a completed delayed content
     * Liefert true bei erkanntem Fehler. false heisst aber nicht, dass der async Load erfolgreich war, sondern nur, dass noch geladen wird.
     * Ein evtl. schon laufender Load wird geskipped.
     * TODO ein timeout wäre auch nicht schlecht.
     *
     * @param file
     * @return
     */
    private static boolean delayedContentload(/*ResourceManager rm, */BundleResource file, NativeBundleLoader bundleLoader) {
        // Content evtl. async nachladen. Aber nicht, wenn er schon mal auf einen Fehler lief.
        if (file.bundle.failed(file)) {
            logger.warn("prevoius error detected for bundle content " + file.getFullName());
            return true;
        }
        if (bundleLoader.isLoading(file)) {
            //10.10.18: sehr oft duplicate logs bei WebGL, darum nicht mehr loggen.
            //logger.warn("still waiting for " + file.getFullName());
            return false;
        }
        if (Config.isAsyncdebuglog()) {
            logger.debug("completing bundle with " + file.getFullName());
        }
        bundleLoader.completeBundle(file/*,rm*/);
        return false;
    }

    /**
     * eigentlich nur fuer Tests
     */
    public static void cleanup() {
        // modelbuilddelegates.clear();
        modelbuildvalues.clear();
        //modelbuildopttexturepath.clear();
        //bundleloaddelegates.clear();
        //*2.8.21 bundleloadvalues.clear();
    }

    public static int getModelbuildvaluesSize() {
        return modelbuildvalues.size();
    }
}

class ModelBuildData {
    public BundleResource value;
    public ResourcePath opttexturepath;
    public int loaderoptions;
    public int delegateid;

    public ModelBuildData(BundleResource value, ResourcePath opttexturepath, int loaderoptions, int delegateid) {
        this.value = value;
        this.opttexturepath = opttexturepath;
        this.loaderoptions = loaderoptions;
        this.delegateid = delegateid;
    }
}


