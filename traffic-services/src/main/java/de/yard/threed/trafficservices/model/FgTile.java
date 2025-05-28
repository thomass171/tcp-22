package de.yard.threed.trafficservices.model;

import de.yard.threed.core.Degree;
import de.yard.threed.core.IntHolder;
import de.yard.threed.core.LatLon;
import de.yard.threed.core.Util;
import de.yard.threed.core.geometry.Polygon;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Path;
import java.util.List;

/**
 * A flightgear tile.
 * <p>
 * Extracted from SGBucket.cxx
 */
@Slf4j
public class FgTile implements Tile {

    long tileIndex;
    short lon;        // longitude index (-180 to 179)
    short lat;        // latitude index (-90 to 89)
    /*unsigned char*/ int x;          // x subdivision (0 to 7)
    /*unsigned char*/ int y;          // y subdivision (0 to 7)

    /**
     * standard size of a bucket in degrees (1/8 of a degree)
     */
    static float SG_BUCKET_SPAN = 0.125f;

    /**
     * half of a standard SG_BUCKET_SPAN
     */
    static float SG_HALF_BUCKET_SPAN = (0.5f * SG_BUCKET_SPAN);

    public FgTile(long bindex) {
        tileIndex = bindex;
        long index = bindex;

        lon = (short) (index >> 14);
        index -= lon << 14;
        lon -= 180;

        lat = (short) (index >> 6);
        index -= lat << 6;
        lat -= 90;

        y = (int) (index >> 3);
        index -= y << 3;

        x = (int) index;
    }

    public static Tile buildFromPath(Path p) {
        String name = p.getFileName().toString();
        log.debug("buildFromPath name={}", name);
        return new FgTile(Long.valueOf(StringUtils.substringBeforeLast(name, ".stg")));
    }

    /**
     * @return the center lon of a tile.
     */
    private double get_center_lon() {
        double span = sg_bucket_span(lat + y / 8.0 + SG_HALF_BUCKET_SPAN);

        if (span >= 1.0) {
            return lon + get_width() / 2.0;
        } else {
            return lon + x * span + get_width() / 2.0;
        }
    }

    /**
     * @return the center lat of a tile.
     */
    private double get_center_lat() {
        return lat + y / 8.0 + SG_HALF_BUCKET_SPAN;
    }

    /**
     * @return the width of the tile in degrees.
     */
    public double get_width() {
        return sg_bucket_span(get_center_lat());
    }

    /**
     * @return the height of the tile in degrees.
     */
    public double get_height() {
        return SG_BUCKET_SPAN;
    }

    /**
     * @return the center of the bucket in geodetic coordinates.
     */
    public LatLon get_center() {
        return LatLon.fromDegrees(get_center_lat(), get_center_lon());
    }

    /**
     * @return
     */
    public LatLon get_corner(int num) {
        double lonFac = (((num + 1) & 2) != 0) ? 0.5 : -0.5;
        double latFac = (((num) & 2) != 0) ? 0.5 : -0.5;
        return LatLon.fromDegrees(get_center_lat() + latFac * get_height(), get_center_lon() + lonFac * get_width());
    }

    // return the horizontal tile span factor based on latitude
    private static double sg_bucket_span(double l) {
        if (l >= 89.0) {
            return 12.0;
        } else if (l >= 86.0) {
            return 4.0;
        } else if (l >= 83.0) {
            return 2.0;
        } else if (l >= 76.0) {
            return 1.0;
        } else if (l >= 62.0) {
            return 0.5;
        } else if (l >= 22.0) {
            return 0.25;
        } else if (l >= -22.0) {
            return 0.125;
        } else if (l >= -62.0) {
            return 0.25;
        } else if (l >= -76.0) {
            return 0.5;
        } else if (l >= -83.0) {
            return 1.0;
        } else if (l >= -86.0) {
            return 2.0;
        } else if (l >= -89.0) {
            return 4.0;
        } else {
            return 12.0;
        }
    }

    @Override
    public String getName() {
        //".stg"??
        return "" + tileIndex;
    }

    @Override
    public Polygon<LatLon> getOutline() {
        Polygon<LatLon> p = new Polygon<LatLon>();
        // CCW starting south west
        p.addPoint(get_corner(0));
        p.addPoint(get_corner(1));
        p.addPoint(get_corner(2));
        p.addPoint(get_corner(3));
        p.closed = false;
        return p;
    }
}

