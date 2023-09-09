package de.yard.threed.engine.platform.common;

import de.yard.threed.core.BuildResult;
import de.yard.threed.core.platform.Config;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleData;
import de.yard.threed.core.resource.ResourcePath;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.engine.LoaderRegistry;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.core.Util;



import de.yard.threed.core.platform.Log;
import de.yard.threed.engine.loader.InvalidDataException;
import de.yard.threed.engine.loader.PortableModelList;
import de.yard.threed.engine.platform.EngineHelper;
import de.yard.threed.engine.platform.ProcessPolicy;

/**
 * 15.9.17: Laden eines einfachen Models in der Platform, wenn die Platform es nicht nativ machen kann/soll.
 * War vorher in der ModelFactory.
 */
public class ModelLoader {
    static Log logger = Platform.getInstance().getLog(ModelLoader.class);

    // MA31: Üble Kruecke zm Entkoppeln
    public static ProcessPolicy processPolicy;

    /**
     * 21.12.17: Soll auch nur aus Platform (AsyncHelper) verwendet werden, wird jetzt aber noch von aussen aufgerufen.
     * Ist nur fuer simple Model, nicht XML.
     * 22.12.17: Jetzt auch mit catch gekapselt, damit bei einem Fehler nicht ein async Loader besetehn bleibt und ewig wieder aufgerufen wird.
     * Ein Fehler steht dann im Result.
     * Never returns null. Oder besser: Liefert null, wenn die Daten im Bundle nicht vorliegen. Dann kann der Aufrufer sie
     * nachladen. Oder der Aufrufer besorgt die Daten.
     *
     * @return
     */
    public static BuildResult buildModelFromBundle(PortableModelList lr, BundleResource modelfile, ResourcePath opttexturepath, int loaderoptions) {
        String extension = modelfile.getExtension();
         try {
            /*3.1.17 ModelBuilder ldr = LoaderRegistry.getLoaderFactory(extension);
            if (ldr != null) {
                // 21.12.17: die Registry wird doch gar nicht mehr verwendet. Darum kommt er hier auch nie hin. Das geht jetzt alles per findloaderbysuffix
                Util.nomore();
                ModelBuildResult result = ldr.build(modelfile, null);
                return result;
            }*/
            //15.9.17: hier kommt er doch überhaupt nicht mehr hin, oder wann? Naja, wenn er keine Loader findet. 
            //Hier kommt er z.B. fuer 3ds rein, und auch für die preprocessed. Ich glaube, ausser XML gibt/gab es gar keine Loader.
            //Hmm. richtig rund ist das nicht. Es gibt ja auch noch die LoaderRegistry.
            //6.1.18  PreprocessedLoadedFile lr = readModelFromBundle(modelfile, true, loaderoptions);
            if (lr == null) {
                // Fehler. Ist bereits gelogged.
                return new BuildResult(new SceneNode()/*"readModelFromBundle failed. See log for details"*/.nativescenenode);
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
                model = lr./*ppfile.*/createPortableModelBuilder().buildModel(modelfile.bundle, /*modelfile.getPath()*/ opttexturepath/*, true*/);

            } else {
                //21.12.17  model = loader.buildModel(modelfile.bundle, modelfile.getPath(), loader.objects.get(0), opttexturepath, true);
            }
            // 3.1.18: Die Extension "ac" gilt auch fuer implizites GLTF und acpp. Das isz zwar FG spezifisch und gehört deshalb nicht unbedingt hier hin.
            // Aber die Model werden nun mal hier geladen. Fuer explizites GLTF gilt das aber nicht. Die ACPolicy soll wirklich nur dann
            // greifen, wenn ein GLTF als ac Proxy geladen wird.
            // 9.1.18: Wird jetzt wegen filenamemapping ac->gltf schon platform entschieden
            // 9.3.21: MA31: Die  ACProcessPolicy gibts hier nicht (mehr). Da duerfte ein Plugin erforderlich sein.
            if ((loaderoptions & EngineHelper.LOADER_APPLYACPOLICY) > 0/* extension.equals("ac")/*||extension.equals("acpp")gibts so nicht*/) {
                if (processPolicy == null){
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
        } catch (java.lang.Exception e) {
            logger.error("Exception caught:", e);
            return new BuildResult(e.getMessage());
        }
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
     *
     * @return
     */
    public static PortableModelList readModelFromBundle(BundleResource file, boolean preferpp, int loaderoptions) {
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
            PortableModelList processor = LoaderRegistry.loadBySuffix(file, (ins), false);
            return processor;
        } catch (InvalidDataException e) {
            //7.4.17: Es gibt schon mal Fehler in Modelfiles. Das wird gelogged (auch tiefer mit Zeilennummer). Auf den Stacktrace verzichten wir mal.
            logger.error("loader threw InvalidDataException " + e.getMessage() + " for file " + file.getFullName());
        }
        return null;
    }

    /**
     * Statt ac evtl. acpp/gltf verwenden. Aber auch nur, wenn das gltf mit bin im Bundle grundsaetzlich vorliegt.
     * 
     * @param file
     * @param preferpp
     * @param loaderoptions
     * @return
     */
    public static BundleResource mapFilename(BundleResource file, boolean preferpp, int loaderoptions) {
        if (preferpp || ((loaderoptions & EngineHelper.LOADER_USEGLTF) > 0)) {
            // ac special handling. Not for explicitly loaded gltf files
            String extension = file.getExtension();
            if (extension.equals("ac") || extension.equals("btg")) {
                boolean usegltf = (loaderoptions & EngineHelper.LOADER_USEGLTF) > 0;
                boolean usedgltf = false;
                if (usegltf) {

                    BundleResource ppfile = new BundleResource(file.bundle, file.path, file.getBasename() + ".gltf");
                    if (ppfile.bundle.exists(ppfile)) {
                        if (Config.modelloaddebuglog) {
                            logger.debug("using gltf instead of " + extension);
                        }
                        file = ppfile;
                        usedgltf = true;
                    }else{
                        logger.warn("not existing gltf "+ppfile.getFullName());
                    }
                }
                if (!usedgltf) {
                    // dann doch acpp versuchen
                    BundleResource ppfile = new BundleResource(file.bundle, file.path, file.name + "pp");
                    if (ppfile.bundle.exists(ppfile)) {
                        logger.debug("using acpp instead of " + extension);
                        //21.4.17geht nicht mehr. 22.12.17: Häh, geht doch.
                        file = ppfile;
                    }
                }
            }
        }
        return file;
    }
}
