package de.yard.threed.trafficcore.model;

import de.yard.threed.core.Degree;
import de.yard.threed.core.LatLon;

import java.util.List;

/**
 * Only basic information of an airport that is needed to land there and typically can be found in eg. OSM).
 * That is runways, rough location to know the terrain tile to load for getting elevation.
 * <p>
 * But not groundnet, viewpoints, parking positions.
 * <p>
 * For simplicity for now also groundnet. But no graphs.
 */
public class Airport {

    private String icao;
    private String groundNetXml;
    private Runway[] runways;
    // lat/lon in degrees
    private double centerLat;
    private double centerLon;

    public Airport(String icao, double centerLat, double centerLon) {
        this.icao = icao;
        this.centerLat = centerLat;
        this.centerLon = centerLon;

    }

    public String getIcao() {
        return icao;
    }

    public String getGroundNetXml() {
        return groundNetXml;
    }

    public void setGroundNetXml(String groundNetXml) {
        this.groundNetXml = (groundNetXml == null) ? null : groundNetXml.replace("\n", "");
    }

    public Runway[] getRunways() {
        return runways;
    }

    public void setRunways(Runway[] runways) {
        this.runways = runways;
    }

    public LatLon getCenter() {
        return LatLon.fromDegrees(centerLat, centerLon);
    }

    /*public void setCenter(LatLon center) {
        this.center = center;
    }*/
}
