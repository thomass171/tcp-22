package de.yard.threed.trafficcore;

/**
 * Created on 29.03.18.
 */
@FunctionalInterface
public interface ElevationProvider {
    /**
     * Might return null;
     * @return
     */
    Double getElevation(double latitudedeg,double longitudedeg);
}
