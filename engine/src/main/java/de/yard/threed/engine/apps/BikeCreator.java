package de.yard.threed.engine.apps;


import de.yard.threed.core.geometry.ProceduralModelCreator;
import de.yard.threed.engine.avatar.VehiclePmlFactory;
import de.yard.threed.core.loader.PortableModelList;

/**
 * Used by reflection!
 * 
 * Created on 17.05.19.
 */
public class BikeCreator implements ProceduralModelCreator {
    @Override
    public PortableModelList createModel() {
        PortableModelList bike = VehiclePmlFactory.buildBike();
        return bike;
    }
}
