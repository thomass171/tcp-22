package de.yard.threed.engine;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.resource.ResourceLoader;
import de.yard.threed.engine.platform.EngineHelper;

/**
 * 9.3.21: FG (XML) part separated. Still needed in engine?
 *
 * Created by thomass on 02.06.16.
 */
public class ModelFactory {
    //static Platform pf = ((Platform)Platform.getInstance());
    static Log logger = Platform.getInstance().getLog(ModelFactory.class);


    /**
     * 21.12.17: So wie der XML Reader arbeitet. Eine Node liefern, in die dann async das Model eingehangen wird.
     * Dies ist nur ein Helper um ueber Platform zu laden. Das geht nicht mit XML Models!
     *
     * 15.2.24: Decoupled from bundle(Resource)
     */
    public static SceneNode asyncModelLoad(ResourceLoader resourceLoader) {
        return asyncModelLoad(resourceLoader, 0);
    }

    /**
     * 7.6.18: This is THE convenience method for loading a model with destination node
     * instead of delegate.
     * 18.10.23: core loader no more 'ac', so only gltf any more. ac file mapping extracted to tcp-flightgear.
     * 15.2.24: Decoupled from bundle(Resource)
     * @param loaderoptions
     * @return
     */
    public static SceneNode asyncModelLoad(ResourceLoader resourceLoader, int loaderoptions) {
        SceneNode destination = new SceneNode();
        Platform.getInstance().buildNativeModelPlain(resourceLoader, null, (result) -> {
            if (result.getNode() == null) {
                logger.error("no node created:"+result.message());
            } else {
                destination.attach(new SceneNode(result.getNode()));
            }
        }, loaderoptions);
        return destination;
    }

}
