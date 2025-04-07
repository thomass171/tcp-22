package de.yard.threed.trafficcore.model;

import de.yard.threed.core.GeoCoordinate;
import de.yard.threed.core.LatLon;
import de.yard.threed.core.StringUtils;
import de.yard.threed.core.Util;
import de.yard.threed.core.Vector3;

/**
 * 26.3.20 No ICAO here? Das nehm ich aber optional mal mit rein.
 * <p>
 * General structure:
 * [graphname|groundnet|parkpos]:[sublocation]
 * [geo]:[latlon]
 * [coordinate]:[vector3]
 *
 * 09.03.25: 'icao' set to deprecated. But isn't it needed for groundnet? Top level 'graphname' ever used?
 */
public class SmartLocation {
    public String location;
    @Deprecated
    public String icao;

    private SmartLocation(String location) {
        this.location = location;
    }

    public SmartLocation(String icao, String location) {
        this.icao = icao;
        this.location = location;
    }

    /**
     * 7.3.25: Builder instead of public constructor.
     * TODO needs decoding
     */
    public static SmartLocation fromString(String location) {
        return new SmartLocation(location);
    }

    public String getParkPos() {
        if (StringUtils.startsWith(location, "parkpos:")) {
            return StringUtils.substringAfter(location, "parkpos:");
        }
        return null;
    }

    public String getSubLocation() {
        if (StringUtils.contains(location, ":")) {
            return StringUtils.substringAfter(location, ":");
        }
        return null;
    }

    public String getGroundnetLocation() {
        if (StringUtils.startsWith(location, "groundnet:")) {
            return StringUtils.substringAfter(location, "groundnet:");
        }
        return null;
    }

    /**
     * Elevation is just optional and might need an elevationProvider
     */
    public GeoCoordinate getGeoCoordinate() {
        if (StringUtils.startsWith(location, "geo:")) {
            return GeoCoordinate.parse(StringUtils.substringAfter(location, "geo:"));
        }
        return null;
    }

    public Vector3 getCoordinate() {
        if (StringUtils.startsWith(location, "coordinate:")) {
            String s = StringUtils.substringAfter(location, "coordinate:");
            //if (StringUtils.split(s,",").length == 2){
                return Util.parseVector3(s);
            //}
        }
        return null;
    }

    @Override
    public String toString() {
        return "location=" + location + ",icao=" + icao;
    }

    public boolean needsGraph() {
        if (StringUtils.startsWith(location, "parkpos:")) {
            return true;
        }
        if (StringUtils.startsWith(location, "groundnet:")) {
            return true;
        }
        return false;
    }
}
