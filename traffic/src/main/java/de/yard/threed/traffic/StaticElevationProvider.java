package de.yard.threed.traffic;

import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.trafficcore.ElevationProvider;

/**
 * 7.5.25: Always providing a fixed elevation. Extracted from TerrainElevationProvider
 */
public class StaticElevationProvider implements de.yard.threed.engine.ecs.DataProvider, ElevationProvider {
    Log logger = Platform.getInstance().getLog(StaticElevationProvider.class);
    private Double altitude = null;

    private StaticElevationProvider(Double altitude) {
            this.altitude = altitude;
    }

    @Override
    public Object getData(Object[] parameter) {
            return altitude;
    }

    /**
     */
    public static StaticElevationProvider buildForStaticAltitude(double altitude) {
        return new StaticElevationProvider(altitude);
    }

    @Override
    public Double getElevation(double latitudedeg, double longitudedeg) {
        return altitude;
    }

    public double getAltitude() {
        return (double) altitude;
    }
}
