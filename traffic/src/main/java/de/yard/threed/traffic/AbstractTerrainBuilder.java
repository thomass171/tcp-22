package de.yard.threed.traffic;

import de.yard.threed.engine.SceneNode;
import de.yard.threed.graph.GraphNode;
import de.yard.threed.traffic.geodesy.MapProjection;

public interface AbstractTerrainBuilder {
    void buildTerrain(Object p0, Object p1, MapProjection projection);

    SceneNode buildParkingNode(GraphNode n);
}
