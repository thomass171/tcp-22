package de.yard.threed.traffic.flight;

import de.yard.threed.core.Vector3;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.apps.ModelSamples;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.traffic.TrafficSystem;
import de.yard.threed.traffic.VehicleBuiltDelegate;
import de.yard.threed.traffic.VehicleComponent;
import de.yard.threed.traffic.VehicleLauncher;
import de.yard.threed.traffic.config.VehicleDefinition;

/**
 * Extracted from VehicleLauncher.
 * <p>
 * 27.12.21
 */
public class DoormarkerDelegate implements VehicleBuiltDelegate {

    @Override
    public void vehicleBuilt(EcsEntity ecsEntity, VehicleDefinition config) {
        String type = config.getType();
        String modeltype = config.getModelType();

        // 29.12.21 mal immer unabhaengig von devmode. Klarer für Tests, und ohne devmode ist ja gar nicht erforderlich. Spaeter mal mit option/setting??
        if (/*((Platform) Platform.getInstance()).isDevmode() && */VehicleComponent.VEHICLE_AIRCRAFT.equals(type)) {
            // ein kleiner doormarker im local space.
            SceneNode marker = ModelSamples.buildAxisHelper(8, 0.3f);
            marker.setName("localdoormarker");

            // 27.12.21 TrafficWorldConfig.getInstance().getAircraftConfiguration(config.getModelType()).getCateringDoorPosition();

            //20.11.23 GroundServiceAircraftConfig aircraftConfig = TrafficHelperExt.getAircraftConfigurationByDataProvider(config.getModelType());
            String modelType=config.getModelType();
            if (modelType == null){
                // 4.12.23 Avoid NPE
                modelType="738";
            }
            VehicleDefinition aircraftConfig = ((TrafficSystem) SystemManager.findSystem(TrafficSystem.TAG)).getVehicleConfig(null, modelType);
            Vector3 dp = aircraftConfig.getCateringDoorPosition();
            marker.getTransform().setPosition(dp);


            //27.12.21:Ob ich hier wirklich dieselbe modelnode bekomme, die ich sonst hatte? Zumindest bei c172p ist es wie vorher.
            SceneNode modelNode = VehicleLauncher.getModelNodeFromVehicleNode(ecsEntity.getSceneNode()/*offsetNode*/);
            modelNode.attach(marker);
        }
    }
}
