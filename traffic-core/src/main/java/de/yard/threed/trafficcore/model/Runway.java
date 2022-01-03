package de.yard.threed.trafficcore.model;

import de.yard.threed.core.Degree;
import de.yard.threed.core.LatLon;

/**
 * Uebernommen aus osm.Runway.
 * <p>
 * 5.5.20
 */
public class Runway {
    private String name, fromNumber, toNumber;
    //TODO das mit der enternode ist doch zu statisch?
    public String enternodefromgroundnet;
    //coordinates on centerline from threshold to threshold
    // lat/lon in degrees
    public double fromLat, fromLon, toLat, toLon;
    double width;
    int len;

    public Runway(double fromLat, double fromLon, String fromNumber, double toLat, double toLon, String toNumber, double width) {
        this.fromLat = fromLat;
        this.fromLon = fromLon;
        this.fromNumber = fromNumber;
        this.toLat = toLat;
        this.toLon = toLon;
        this.toNumber=toNumber;
        this.width = width;
        //TODO anders
        enternodefromgroundnet = "188";
    }

    public LatLon getFrom(){
        return LatLon.fromDegrees(fromLat,fromLon);
    }

    public LatLon getTo(){
        return LatLon.fromDegrees(toLat,toLon);
    }

    public String getFromNumber() {
        return fromNumber;
    }

    public String getToNumber() {
        return toNumber;
    }

    public double getWidth() {
        return width;
    }

    public String getName() {
        return fromNumber+"/"+toNumber;
    }
}
