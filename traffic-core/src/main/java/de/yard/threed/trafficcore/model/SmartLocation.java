package de.yard.threed.trafficcore.model;

import de.yard.threed.core.StringUtils;

/**
 * 26.3.20 Enthaelt gar keinen ICAO? Das nehm ich aber optional mal mit rein.
 * <p>
 * General structure:
 * [graphname|groundnet|parkpos]:[sublocation]
 */
public class SmartLocation {
    public String location, icao;

    public SmartLocation(String location) {
        this.location = location;
    }

    public SmartLocation(String icao, String location) {
        this.icao = icao;
        this.location = location;
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


}
