package de.yard.threed.engine.platform.common;

import de.yard.threed.core.BuildResult;
import de.yard.threed.core.GeneralParameterHandler;
import de.yard.threed.core.ModelBuildDelegate;
import de.yard.threed.core.ModelPreparedDelegate;
import de.yard.threed.core.loader.LoaderGLTF;
import de.yard.threed.core.loader.PreparedModel;
import de.yard.threed.core.platform.Config;
import de.yard.threed.core.platform.NativeSceneNode;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.ResourceLoader;
import de.yard.threed.core.resource.ResourcePath;
import de.yard.threed.engine.SceneNode;


import de.yard.threed.core.platform.Log;
import de.yard.threed.core.loader.PortableModel;
import de.yard.threed.engine.AbstractMaterialFactory;
import de.yard.threed.engine.loader.PortableModelBuilder;
import de.yard.threed.engine.platform.EngineHelper;
import de.yard.threed.engine.platform.ProcessPolicy;

/**
 * 15.9.17: Build a model from a model definition 'file' (but no FG XML). Splitted into two steps:
 * 1) Read model and convert to PortableModelList
 * 2) Prepare/Build model from PortableModelList
 * This is an alternative to building a model in the platform natively. However, a platform might internally also use this.
 * 18.10.23: No more 'ac' or 'btg', should only be gltf these days. So skip registry.
 * 13.2.24: Decoupled from bundle and async. Might also load async via HTTP without bundle.
 * 22.08.24: The phrase 'bundle' removed.
 * <p>
 * Was in ModelFactory once.
 */
public class ModelLoader {
    static Log logger = Platform.getInstance().getLog(ModelLoader.class);

    // MA31: Üble Kruecke zm Entkoppeln
    public static ProcessPolicy processPolicy;

    /**
     * Async load of a GLTF model.
     * 10.11.23: Code section extracted from AsyncHelper.
     * Do the two steps read and prepare/build.
     * This method is also used by some platforms for platform internal model loading.
     */
    public static void buildModel(ResourceLoader resourceLoader, ResourcePath opttexturepath, int buildoptions, ModelBuildDelegate delegate, AbstractMaterialFactory materialFactory) {

        readGltfModel(resourceLoader, new GeneralParameterHandler<PortableModel>() {
            @Override
            public void handle(PortableModel lr) {
                BuildResult r;
                if (lr == null) {
                    // read failed (already logged).  14.2.24: No longer set a node in this case
                    r = new BuildResult((NativeSceneNode) null/*(new SceneNode()).nativescenenode*/);
                    delegate.modelBuilt(r);
                } else {
                    // resourceLoader is used for async texture load (with internal delegate).
                    PreparedModel preparedModel = PortableModelBuilder.prepareModel(lr, resourceLoader, opttexturepath, materialFactory);
                    r = ModelLoader.buildModelFromPreparedModel(preparedModel, buildoptions);
                    AbstractSceneRunner.getInstance().systemTracker.modelBuilt(resourceLoader.nativeResource.getFullQualifiedName());
                    delegate.modelBuilt(r);
                }
            }
        });
    }

    /**
     * @param resourceLoader
     * @param opttexturepath
     * @param delegate
     */
    public static void prepareModel(ResourceLoader resourceLoader, ResourcePath opttexturepath, ModelPreparedDelegate delegate,
                                    PreparedModelCache preparedModelCache) {

        // 2.9.24: TODO cache doesn't prevent multiple GLTF load. improve?
        readGltfModel(resourceLoader, new GeneralParameterHandler<PortableModel>() {
            @Override
            public void handle(PortableModel lr) {

                PreparedModel preparedModel = null;
                if (preparedModelCache != null) {
                    preparedModel = preparedModelCache.get(lr.getName());
                }
                if (preparedModel == null) {
                    preparedModel = PortableModelBuilder.prepareModel(lr, resourceLoader, opttexturepath);
                    if (preparedModelCache != null) {
                        preparedModelCache.put(lr.getName(), preparedModel);
                    }
                }
                delegate.modelPrepared(preparedModel);
            }
        });
    }

    /**
     * Only reads the model without building a 3D Object.
     * <p>
     * This is async loading due to the nature of ResourceLoader, maybe inside async. Use case is internal usage from inside async(?)
     * Also used from external projects.
     */
    public static void readGltfModel(ResourceLoader file, GeneralParameterHandler<PortableModel> delegate) {
        // The load is async (probably in any case). So no exception possible to handle.
        LoaderGLTF.load(file, delegate/*,new ResourcePath(file.getUrl().getName())*/);
    }

    /**
     * 10.11.23: Method name 'buildModelFromBundle' moved up.
     *
     * @return
     */
    public static BuildResult buildModelFromPreparedModel(PreparedModel lr, int buildOptions) {

        if (lr == null) {
            logger.warn("PortableModel is null. Building empty node");
            return new BuildResult(new SceneNode().nativescenenode);
        }
         /* 27.7.24 why handle this?? if (lr.getObjectCount() == 0) {
            logger.warn("empty model " + modelfile/*.getName()* / );
            return new BuildResult(new SceneNode().nativescenenode);
        }*/

        //22.12.17: Das object(0) soll sich zu einer root node entwickeln und dann auch umbenannt werden. NeeNee. Die root node entsteht erst hier.
        SceneNode model = PortableModelBuilder.buildModel(lr);

        // 3.1.18: Die Extension "ac" gilt auch fuer implizites GLTF und acpp. Das isz zwar FG spezifisch und gehört deshalb nicht unbedingt hier hin.
        // Aber die Model werden nun mal hier geladen. Fuer explizites GLTF gilt das aber nicht. Die ACPolicy soll wirklich nur dann
        // greifen, wenn ein GLTF als ac Proxy geladen wird.
        // 9.1.18: Wird jetzt wegen filenamemapping ac->gltf schon platform entschieden
        // 9.3.21: MA31: Die  ACProcessPolicy gibts hier nicht (mehr). Da duerfte ein Plugin erforderlich sein.
        if ((buildOptions & EngineHelper.LOADER_APPLYACPOLICY) > 0/* extension.equals("ac")/*||extension.equals("acpp")gibts so nicht*/) {
            if (processPolicy == null) {
                // force ACPolicy
                throw new RuntimeException("no policy");
            }
            //model = new ACProcessPolicy(null).process(model, null, null);
            model = processPolicy.process(model, null);
        }

        if (Config.loaderdebuglog) {
            logger.debug("model tree:" + model.dump("", 0));
        }
        return new BuildResult(model.nativescenenode);
    }
}
