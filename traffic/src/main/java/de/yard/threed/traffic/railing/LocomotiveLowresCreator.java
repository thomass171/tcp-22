package de.yard.threed.traffic.railing;



import de.yard.threed.core.geometry.ProceduralModelCreator;
import de.yard.threed.core.loader.PortableModelList;
import de.yard.threed.engine.avatar.VehiclePmlFactory;


/**
 * Used by reflection!
 * 
 * Created on 03.01.19.
 */
public class LocomotiveLowresCreator implements ProceduralModelCreator {
    @Override
    public PortableModelList createModel() {
        PortableModelList locomotive = VehiclePmlFactory.buildLocomotiveLowres();
        return locomotive;
    }
}
