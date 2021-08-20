package de.yard.threed.engine.osm;

/**
 * Created by thomass on 14.09.15.
 */
public class Node {
    float lat,lon;

    public Node(float lat, float lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public float getProjectedLat() {
        return lat;
    }

    public float getProjectedLon() {
        return lon;
    }
}
