package de.yard.threed.trafficcore.geodesy;

import de.yard.threed.core.LatLon;
import de.yard.threed.core.Vector2;

/**
 * abstract map projection superclass with configurable coordinate origin
 * 
 * Derived from Osm2World.
 */
public abstract class OriginMapProjection implements MapProjection {

    protected LatLon origin;

    public OriginMapProjection(LatLon origin){
        this.origin=origin;
    }
    
    @Override
    public LatLon getOrigin() {
        return origin;
    }

    /**
     * Nur wegen C# override hier abstract
     * @param coor
     * @return
     */
    public abstract Vector2 project(LatLon coor);
    public abstract   LatLon unproject(Vector2 loc);
    /*public abstract FlightLocation unprojectToFlightLocation(Vector2 pos, double alt, Vector2 direction);*/
}
