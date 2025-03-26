package de.yard.threed.traffic;

import de.yard.threed.core.BuildResult;
import de.yard.threed.core.Util;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.platform.EngineHelper;
import de.yard.threed.engine.platform.ResourceLoaderFromBundle;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;

import de.yard.threed.traffic.config.VehicleDefinition;
import de.yard.threed.trafficcore.model.Vehicle;

/**
 * 10.11.21: For non XML model
 */
public class SimpleVehicleLoader implements VehicleLoader {

    private static Log logger = Platform.getInstance().getLog(SimpleVehicleLoader.class);

    /**
     * @return
     */
    @Override
    public void loadVehicle(Vehicle vehicle, VehicleDefinition config, VehicleLoadedDelegate loaddelegate) {
        logger.debug("Load vehicle " + config.getName());

        AbstractSceneRunner.instance.loadBundle(config.getBundlename(), (Bundle bundle) -> {

            BundleResource br = BundleResource.buildFromFullString(config.getModelfile());
            br.bundle = bundle;

            // 18.10.23: No more 'ac', so only gltf any more.
            Platform.getInstance().buildNativeModelPlain(new ResourceLoaderFromBundle(br), null, (BuildResult result) -> {
                if (result.getNode() != null) {

                    SceneNode currentaircraft = new SceneNode(result.getNode());

                    SceneNode lowresNode = null;
                    String lowresfile = config.getLowresFile();
                    if (lowresfile != null) {
                        BundleResource lowresbr = BundleResource.buildFromFullString(lowresfile);
                        lowresbr.bundle = bundle;
                      /*  SGPropertyNode lowresdestinationProp = new SGPropertyNode(config.getName() + "LowRes-root");
                        List<SGAnimation> lowresanimationList = new ArrayList<SGAnimation>();
                        lowresNode = de.yard.threed.flightgear.traffic.ModelFactory.buildModelFromBundleXml(lowresbr, lowresdestinationProp, animationList);*/
                        Util.notyet();
                    }
                    // In der base node KANN auch eine Rotation sein (manche AIs). Darum ist es doch unbrauchbar
                    // und wird gekapselt.
                    SceneNode nn = buildVehicleNode(currentaircraft, config.getZoffset());
                    SceneNode basenode = VehicleLauncher.getModelNodeFromVehicleNode(nn);
                    loaddelegate.vehicleLoaded(nn, new SimpleVehicleLoaderResult(), lowresNode);
                } else {
                    logger.error("model built failed for "/* + finalpendingbmodelpath.getFullName()*/);
                }

            }, 0/*EngineHelper.LOADER_USEGLTF should not be needed here*/);

            logger.debug("vehicle " + config.getName() + " loaded");

        });
    }

    /**
     * Node eines Vehicle, die zum Model des Vehicle einen z-Offset (in der Höhe) hat.
     * Diese Node hier ist die Ground/Offset Node, den Modelbezug liefert ModelNode.
     * Die ModelNode und vehicle sind indirekt Child der groundNode.
     * <p>
     * 6.11.21: deprecated because z is not always the correct axis?
     * Skizze 37
     */
    public static SceneNode buildVehicleNode(SceneNode currentaircraft, double zoffset) {
        SceneNode basenode = new SceneNode(currentaircraft);
        basenode.setName("basenode");
        SceneNode node = new SceneNode(basenode);
        //6.11.21 name is likely to change later to "offsetnode/vehiclecontainer"(or not??)
        //15.3.25 No, name doesn't change later
        node.setName("zoffsetnode");
        Vector3 p = node.getTransform().getPosition();
        p = new Vector3(p.getX(), p.getY(), p.getZ() + zoffset);
        node.getTransform().setPosition(p);
        SceneNode nn = new SceneNode(node);
        nn.setName("vehicle-container");
        return nn;
    }


}
