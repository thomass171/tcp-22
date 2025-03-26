package de.yard.threed.traffic;


import de.yard.threed.core.geometry.ProceduralModelCreator;
import de.yard.threed.core.loader.PortableModel;
import de.yard.threed.engine.avatar.VehiclePmlFactory;

/**
 * Used by reflection!
 *
 */
public class MobiCreator implements ProceduralModelCreator {
    @Override
    public PortableModel createModel() {
        PortableModel mobi = VehiclePmlFactory.buildMobi();
        return mobi;
    }
}
