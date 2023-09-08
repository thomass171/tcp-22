package de.yard.threed.traffic;


import de.yard.threed.traffic.flight.FlightLocation;

/**
 * PointOfInterest
 * <p>
 * Created by thomass on 05.01.17.
 * 
 * TODO mit PoiConfig mergen.
 */
@Deprecated
public class POI {
    public String name, comment;
    public FlightLocation location;

    public POI(String name, String comment, FlightLocation location) {
        this.name = name;
        this.comment = comment;
        this.location = location;
    }
}
