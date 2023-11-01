package de.yard.threed.traffic.railing;



import de.yard.threed.core.geometry.ProceduralModelCreator;
import de.yard.threed.core.loader.PortableModelList;
import de.yard.threed.engine.avatar.VehiclePmlFactory;



/**
 * Used by reflection from loc.pcm!
 * 
 * Created on 03.01.19.
 */
public class LocomotiveCreator implements ProceduralModelCreator {
    @Override
    public PortableModelList createModel() {
        PortableModelList locomotive = VehiclePmlFactory.buildLocomotive();
        return locomotive;
    }
}
