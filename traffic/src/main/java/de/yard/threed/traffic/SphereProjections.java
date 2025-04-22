package de.yard.threed.traffic;

import de.yard.threed.graph.GraphProjection;
import de.yard.threed.traffic.geodesy.GraphMapProjection;
import de.yard.threed.trafficcore.geodesy.SimpleMapProjection;

public class SphereProjections {

    // only in 2D for generic tiles and map style tiles.
    public SimpleMapProjection projection;

    // only set for 3D?
    // 21.3.24 Isn't this for groundnet and thus icao dependent?
    public GraphProjection/*Flight3D*/ backProjection;

    public SphereProjections(SimpleMapProjection projection,GraphProjection/*Flight3D*/ backProjection){
        this.projection=projection;
        this.backProjection=backProjection;

    }
}
