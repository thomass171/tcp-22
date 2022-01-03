package de.yard.threed.traffic.geodesy;

/**
 * Created on 29.03.18.
 */
public interface ElevationProvider {
    /**
     * Might return null;
     * @return
     */
    Double getElevation(double latitudedeg,double longitudedeg);
}
