package de.yard.threed.engine.apps;


import de.yard.threed.core.geometry.ProceduralModelCreator;
import de.yard.threed.engine.avatar.VehiclePmlFactory;
import de.yard.threed.core.loader.PortableModel;

/**
 * Used by reflection!
 * 
 * Created on 17.05.19.
 */
public class BikeCreator implements ProceduralModelCreator {
    @Override
    public PortableModel createModel() {
        PortableModel bike = VehiclePmlFactory.buildBike();
        return bike;
    }
}
