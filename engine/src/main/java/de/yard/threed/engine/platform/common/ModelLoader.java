package de.yard.threed.engine.platform.common;

import de.yard.threed.core.BuildResult;
import de.yard.threed.core.loader.AbstractLoader;
import de.yard.threed.core.loader.LoaderGLTF;
import de.yard.threed.core.platform.Config;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleData;
import de.yard.threed.core.resource.ResourcePath;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.engine.LoaderRegistry;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.core.Util;


import de.yard.threed.core.platform.Log;
import de.yard.threed.core.loader.InvalidDataException;
import de.yard.threed.core.loader.PortableModelList;
import de.yard.threed.engine.loader.PortableModelBuilder;
import de.yard.threed.engine.platform.EngineHelper;
import de.yard.threed.engine.platform.ProcessPolicy;

/**
 * 15.9.17: Build a model from a model definition 'file'. Splitted into two steps:
 * 1) Read model and convert to PortableModelList
 * 2) Build model from PortableModelList
 * This is an alternative to building a model in the platform natively. However, a platform  might internally also use this
 * via AsyncHelper.
 * <p>
 * War vorher in der ModelFactory.
 */
public class ModelLoader {
    static Log logger = Platform.getInstance().getLog(ModelLoader.class);

    // MA31: Üble Kruecke zm Entkoppeln
    public static ProcessPolicy processPolicy;

    /**
     * 10.11.23: Code section extracted from AsyncHelper.
     * Do the two steps read and build.
     */
    public static BuildResult buildModelFromBundle( BundleResource file, ResourcePath opttexturepath, int loaderoptions) {

        PortableModelList lr;
        try {
            lr = readGltfModelFromBundle(file, true, loaderoptions);
            if (lr == null) {
                // Fehler. Ist bereits gelogged.
                return new BuildResult((new SceneNode()).nativescenenode);
            }
        } catch (java.lang.Exception e) {
            logger.error("Exception caught:", e);
            return new BuildResult(e.getMessage());
        }
        logger.debug("gltf loaded");
        BuildResult r = ModelLoader.buildModelFromPortableModelList/*Bundle*/(lr, file, opttexturepath, loaderoptions);
        return r;
    }

    /**
     * Ein natives Model (ac,obj etc, nicht FG xml) aus einem Bundle einlesen.
     * Muss wegen btg eigentlich den Loader liefern, weil das doof ist aber jetzt das preprocessed model.
     * 21.4.17: Wenn das Bundle in file nicht eingetragen ist, koennte hier ein resolve gwemacht werden??
     * Liefert null bei Fehler (already logged).
     * 15.9.17: Soll nur aus Platform verwendet werden, wird jetzt aber noch von aussen aufgerufen. Das hat für
     * Analysezwecke und Tests aber durchaus seine Berechtigung. Aber nur dann.
     * Liest das Model nur ein, ohne es als 3D Objekt anzulegen.
     * 21.12.17: Wenn mal auf gltf umgestellt ist und die obj Loader nicht mehr da sind, wird das zur Laufzeit ueber die
     * Platform auch nicht mehr ladbar sein.
     * <p>
     * This is a direct sync loading. No engine/platform involved. Use case is internal usage from inside async.
     * 18.10.23: No more 'ac', so only gltf any more.
     * 10.11.23: Moved from AsyncHelper to here.
     * @return
     */
    public static PortableModelList readGltfModelFromBundle(BundleResource file, boolean preferpp, int loaderoptions) {
        if (file.bundle == null) {
            logger.warn("no bundle set");
            return null;
        }

        // file = mapFilename(file,preferpp,loaderoptions);

        // special handling of btg.gz files. Irgendwie Driss
        // 12.6.17: Andererseits werden pp files hier auch transparent geladen. Aber das macht eh schon der Bundleloader, damit der unzip in der platform ist.
        // 23.12.17:TODO ins erst im Loader selber lesen, wie GLTF es macht.
        BundleData ins = file.bundle.getResource(file);
        if (ins == null) {
            logger.error(file.getName() + " not found in bundle " + file);
            return null;
        }

        try {
            //18.10.23: Should only be gltf these days. So skip registry.
            //PortableModelList processor = LoaderRegistry.loadBySuffix(file, (ins), false);
            AbstractLoader loader = LoaderGLTF.buildLoader(file,/*i/*ns.s,*/  file.path);
            PortableModelList processor = loader.preProcess();
            return processor;
        } catch (InvalidDataException e) {
            //7.4.17: Es gibt schon mal Fehler in Modelfiles. Das wird gelogged (auch tiefer mit Zeilennummer). Auf den Stacktrace verzichten wir mal.
            logger.error("loader threw InvalidDataException " + e.getMessage() + " for file " + file.getFullName());
        }
        return null;
    }
    /**
     * 21.12.17: Soll auch nur aus Platform (AsyncHelper) verwendet werden, wird jetzt aber noch von aussen aufgerufen.
     * Ist nur fuer simple Model, nicht XML.
     * 22.12.17: Jetzt auch mit catch gekapselt, damit bei einem Fehler nicht ein async Loader besetehn bleibt und ewig wieder aufgerufen wird.
     * Ein Fehler steht dann im Result.
     * Never returns null. Oder besser: Liefert null, wenn die Daten im Bundle nicht vorliegen. Dann kann der Aufrufer sie
     * nachladen. Oder der Aufrufer besorgt die Daten.
     * 10.11.23: Method name 'buildModelFromBundle' moved up.
     *
     * @return
     */
    public static BuildResult buildModelFromPortableModelList/*Bundle*/(PortableModelList lr, BundleResource modelfile, ResourcePath opttexturepath, int loaderoptions) {
        String extension = modelfile.getExtension();

        if (lr == null) {
            logger.warn("PortableModelList is null. Building empty node");
            return new BuildResult(new SceneNode().nativescenenode);
        }
            /*if (lr.getNode() != null) {
                // schon fertig, z.B. bei XML. 21.12.17: Das gibt es hier doch nicht mehr. Der read hat noch keine Model gebaut.
                Util.nomore();
                return new ModelBuildResult(lr.getNode());
            }*/
        //30.12.17:BTG als gltf geht jetzt hier durch. NeeNee, der braucht wegen Material ja Sonderbehandlung
        //31.12.17 btg ist Sonderfall wie stg
        if (extension.equals("btg")) {

            //bloeder Sonderfall. Der kommt hier aber ueberhaupt nicht hin, zumindest noch nicht.
            Util.notyet();
        }
            /*21.12.17 LoadedFile loader = lr.loadedfile;
            if (loader == null && lr.ppfile == null) {
                // Fehler. Ist bereits gelogged.
                return null;
            }*/

        SceneNode model = null;
      
        /*10.4.17 if (objindex != 0) {
            // 23.1.17: Das ist dehr spezielle fur die FG ACs mit den kids. TODO besser.
            model = loader.buildModel(loader.objects.get(0).kids.get(objindex),opttexturepath,true);
        } else {*/

        if (lr.getObjectCount() == 0) {
            logger.warn("empty model " + modelfile.getName());
            return new BuildResult(new SceneNode().nativescenenode);
        }
        if (lr/*.ppfile*/ != null) {
            //22.12.17: Das object(0) soll sich zu einer root node entwickeln und dann auch umbenannt werden. NeeNee. Die root node entsteht erst hier.
            model = new PortableModelBuilder(lr/*ppfile.*/).buildModel(modelfile.bundle, /*modelfile.getPath()*/ opttexturepath/*, true*/);

        } else {
            //21.12.17  model = loader.buildModel(modelfile.bundle, modelfile.getPath(), loader.objects.get(0), opttexturepath, true);
        }
        // 3.1.18: Die Extension "ac" gilt auch fuer implizites GLTF und acpp. Das isz zwar FG spezifisch und gehört deshalb nicht unbedingt hier hin.
        // Aber die Model werden nun mal hier geladen. Fuer explizites GLTF gilt das aber nicht. Die ACPolicy soll wirklich nur dann
        // greifen, wenn ein GLTF als ac Proxy geladen wird.
        // 9.1.18: Wird jetzt wegen filenamemapping ac->gltf schon platform entschieden
        // 9.3.21: MA31: Die  ACProcessPolicy gibts hier nicht (mehr). Da duerfte ein Plugin erforderlich sein.
        if ((loaderoptions & EngineHelper.LOADER_APPLYACPOLICY) > 0/* extension.equals("ac")/*||extension.equals("acpp")gibts so nicht*/) {
            if (processPolicy == null) {
                // force ACPolicy
                throw new RuntimeException("no policy");
            }
            //model = new ACProcessPolicy(null).process(model, null, null);
            model = processPolicy.process(model, null);
        }
        //}
        if (Config.loaderdebuglog) {
            logger.debug("model tree:" + model.dump("", 0));
        }
        /*}*/
        return new BuildResult(model.nativescenenode);
    }


}
