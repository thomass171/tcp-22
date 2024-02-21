package de.yard.threed.engine.platform.common;

import de.yard.threed.core.BuildResult;
import de.yard.threed.core.Pair;
import de.yard.threed.core.buffer.NativeByteBuffer;
import de.yard.threed.core.loader.AbstractLoader;
import de.yard.threed.core.loader.InvalidDataException;
import de.yard.threed.core.loader.LoaderGLTF;
import de.yard.threed.core.platform.AsyncDelegator;
import de.yard.threed.core.platform.AsyncHttpResponse;
import de.yard.threed.core.platform.AsyncJobDelegate;
import de.yard.threed.core.platform.Config;
import de.yard.threed.core.platform.NativeFuture;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.*;
import de.yard.threed.core.platform.Log;
import de.yard.threed.engine.SceneNode;


import de.yard.threed.core.loader.PortableModelList;

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
 * 21.12.23: No more for bundle loading in general.
 * 19.2.24: No longer for model building.
 */
public class AsyncHelper {
    static Log logger = Platform.getInstance().getLog(AsyncHelper.class);

    /**
     * public fuer Tests
     */
    static public void processAsync(/*ResourceManager rm,*/ /*21.12.23NativeBundleLoader bundleLoader*/) {

        //Sicherheitshalber mal prefen. Wird aber offenbar auch in webgl verwendet.Siehe header.
        if (Platform.getInstance().hasOwnAsync()) {
            //throw new RuntimeException("invalid usage of AsyncHelper");
        }

        //21.12.23 not needed/used? External usage
        /*4.1.24for (AsyncInvoked<AsyncHttpResponse> asyncInvoked : AbstractSceneRunner.getInstance().invokedLater) {
            asyncInvoked.run();
        }
        AbstractSceneRunner.getInstance().invokedLater.clear();*/


        // 15.12.23 Futures are now done in AbstractSceneRunner itself.

    }

    /**
     * Versuchen ein Model zu laden. Geht nur wenn die Daten vorliegen.
     * <p>
     * Liefert null, wenn das Model nicht geladen wurde, weil noch Daten fehlen. Wenn der Load/Build aber
     * scheiterte, wird trotzdem ein wenn auch leeres Result geliefert.
     * <p>
     * Das geht aber doch wohl nur fuer "einfache", nicht XML Model.
     * 18.10.23: No more 'ac', so only gltf any more.
     *
     * @param file
     * @param opttexturepath
     * @param loaderoptions
     * @return
     */
    private static BuildResult attemptModelLoad(BundleResource file, ResourcePath opttexturepath, int loaderoptions, int delegateid/*21.12.23 , NativeBundleLoader bundleLoader*/) {
        logger.debug("processing async model build for " + file);
        if (file.bundle == null) {
            logger.error("bundle not set for file " + file.getName());
            return new BuildResult("failure");
        }
        // wenn die Daten im Bundle (noch) nicht vorliegen, diesen Request skippen
        BundleData ins = file.bundle.getResource(file);
        if (ins == null) {
            logger.debug(file.getName() + " not found (or null) in bundle " + file.bundle.name);
            // * 21.12.23: There is no longer a delayed bundle loading.
            /*21.12.23if (delayedContentload( file, bundleLoader)) {
                return new BuildResult("failure");
            }*/
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
                logger.debug("binres " + binres.getName() + " not found in bundle " + file.bundle.name);
                // Content evtl. async nachladen
                // * 21.12.23: There is no longer a delayed bundle loading.
                /*21.12.23 if (delayedContentload( binres, bundleLoader)) {
                    return new BuildResult("failure");
                }*/
                return null;
            }
        }

        /*14.2.24 still needed? But logging and releaseDelayedResource?
         BuildResult r = ModelLoader.buildModelFromBundle(file, opttexturepath, loaderoptions);


        // 16.1.18: The one and only info log for building a model.
        String nodeisnull = (r.getNode() == null) ? " but node isType null." : "";
        logger.info("Model " + file.getFullName() + " built. Loading took ?" + /*lr.loaddurationms +* / " ms." + nodeisnull + " delegateid=" + delegateid);
        // Den Bundleinhalt evtl. wieder freigeben. 25.1.18: Erstmal rausgenommen bis klar ist, ob das bei shared modeln nicht zu built Fehlern führt.
        // Wegen Resourcen doch wieder.
        file.bundle.releaseDelayedResource(file, binres);
        return r;*/
        return null;
    }

    /**
     * eigentlich nur fuer Tests
     */
    public static void cleanup() {

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


