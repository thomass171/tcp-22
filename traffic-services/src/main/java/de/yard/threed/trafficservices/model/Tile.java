package de.yard.threed.trafficservices.model;

import de.yard.threed.core.LatLon;
import de.yard.threed.core.geometry.Polygon;
import lombok.Data;

import java.nio.file.Path;

public interface Tile {
    String getName();
    Polygon<LatLon> getOutline();

}
