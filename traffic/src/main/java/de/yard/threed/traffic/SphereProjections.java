package de.yard.threed.traffic;

import de.yard.threed.graph.GraphProjection;
import de.yard.threed.traffic.geodesy.SimpleMapProjection;

public class SphereProjections {

    // only in 2D for generic tiles and map style tiles.
    public SimpleMapProjection projection;

    // only set for 3D?
    public GraphProjection/*Flight3D*/ backProjection;

    public SphereProjections(SimpleMapProjection projection,GraphProjection/*Flight3D*/ backProjection){
        this.projection=projection;
        this.backProjection=backProjection;

    }
}
