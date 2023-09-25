package de.yard.threed.traffic;

import de.yard.threed.core.Vector3;
import de.yard.threed.core.geometry.Rectangle;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.graph.GraphNode;
import de.yard.threed.traffic.geodesy.MapProjection;

/**
 * Might be more than just terrain but full scenery.
 *
 * Terrain might be projected? Hmm, maybe.
 * NoNoNo, 'Flat'/Tile20 has no on-demand terrain loading, so no projection here.
 */
public interface AbstractTerrainBuilder {

    void init(SceneNode destinationNode);

    /**
     * Optional.
     */
    //void initProjection(MapProjection projection);

    void updateForPosition(Vector3 position, Vector3 direction);

    /**
     * Gives a rough estimation for the user on how often to call updateForPosition().
     */
    Rectangle getLastTileSize();

    // Previous methods are too specific
    @Deprecated
    void buildTerrain(Object p0, Object p1, MapProjection projection);
    @Deprecated
    SceneNode buildParkingNode(GraphNode n);
}
