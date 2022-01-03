package de.yard.threed.traffic;

import de.yard.threed.traffic.geodesy.GeoCoordinate;

import de.yard.threed.graph.GraphComponent;

public class NodeCoord extends GraphComponent {
    public GeoCoordinate coor;
    public double/*Elevation*/ elevation;
            
    public NodeCoord(GeoCoordinate coor, double/*Elevation*/ elevation) {
        this.coor = coor;
        this.elevation=elevation;
    }
}
