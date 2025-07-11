package de.yard.threed.traffic;


import de.yard.threed.core.LatLon;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.trafficcore.ElevationProvider;

/**
 * 26.2.24: defaultAltitude added
 * 7.5.25: Moved here from tcp-flightgear. Coupled to AbstractSceneryBuilder to be more universal. StaticElevationProvider extracted.
 * <p>
 * Created on 28.03.18.
 */
public class TerrainElevationProvider implements de.yard.threed.engine.ecs.DataProvider, ElevationProvider {
    Log logger = Platform.getInstance().getLog(TerrainElevationProvider.class);
    // optional value if detection fails. 29.5.24 Except for testing this appears nonsense
    Double defaultAltitude = null;
    AbstractSceneryBuilder sceneryBuilder;

    public TerrainElevationProvider(AbstractSceneryBuilder sceneryBuilder) {
        this.sceneryBuilder=sceneryBuilder;
    }

    @Override
    public Object getData(Object[] parameter) {
        LatLon coor = (LatLon) parameter[0];
        Double elevation = sceneryBuilder.getElevation(coor);
        //logger.debug("elevation " + elevation + " found for " + coor + ", world=" + world);
        if (elevation == null && defaultAltitude != null) {
            return defaultAltitude;
        }
        return elevation;
    }

    @Override
    public Double getElevation(double latitudedeg, double longitudedeg) {
        Double elevation = (Double) getData(new Object[]{LatLon.fromDegrees(latitudedeg, longitudedeg)});
        return elevation;
    }

    /**
     * only for testing meanwhile
     * @param v
     */
    @Deprecated
    public void setDefaultAltitude(double v) {
        defaultAltitude = v;
    }
}
