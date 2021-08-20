package de.yard.threed.engine;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.platform.Log;
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
     * @return
     */
    public static SceneNode asyncModelLoad(BundleResource br) {
        return asyncModelLoad(br, 0);
    }

    /**
     * 7.6.18: Ich glaube, das ist jetzt DIE Convenience Methode, ein Model zu laden. Zumindest eine davon, wenn
     * destination so ok ist und man den Delegte selber nicht braucht.
     * 29.12.18: Geht aber auch nicht mit XML.
     * 
     * @param br
     * @param loaderoptions
     * @return
     */
    public static SceneNode asyncModelLoad(BundleResource br, int loaderoptions) {
        SceneNode destination = new SceneNode();
        EngineHelper.buildNativeModel(br, null, (result) -> {
            if (result.getNode() == null) {
                logger.error("no node created:"+result.message());
            } else {
                destination.attach(new SceneNode(result.getNode()));
            }
        }, loaderoptions);
        return destination;
    }

}
