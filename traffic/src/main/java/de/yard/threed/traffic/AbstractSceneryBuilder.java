package de.yard.threed.traffic;

import de.yard.threed.core.LatLon;
import de.yard.threed.core.geometry.Rectangle;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.graph.GraphNode;
import de.yard.threed.trafficcore.EllipsoidCalculations;
import de.yard.threed.trafficcore.geodesy.MapProjection;

/**
 * Might be more than just terrain but full scenery.
 *
 * Terrain might be projected? Hmm, maybe.
 * NoNoNo, 'Flat'/Tile20 has no on-demand terrain loading, so no projection here.
 * 9.5.24: Renamed AbstractTerrainBuilder to AbstractSceneryBuilder to indicate it is
 * more than just terrain but full scenery (for example buildings and trees).
 */
public interface AbstractSceneryBuilder {

    void init(SceneNode destinationNode);

    /**
     * Optional.
     */
    //void initProjection(MapProjection projection);

    /**
     * 4.5.25: Parameter changed from Vector3 to LatLon, which appears to be more appropriate for most(all?) use cases.
     * And direction appears useless.
     */
    void updateForPosition(LatLon position);

    /**
     * Gives a rough estimation for the user on how often to call updateForPosition().
     */
    Rectangle getLastTileSize();

    // Previous methods are too specific
    @Deprecated
    void buildTerrain(Object p0, Object p1, MapProjection projection);
    @Deprecated
    SceneNode buildParkingNode(GraphNode n);

    /**
     * Was in SphereSystem once, but is highly coupled to scenery because positioning in the
     * scenery requires exact the same calculations that were used for building the scenery.
     */
    EllipsoidCalculations getEllipsoidCalculations();

    /**
     * 7.5.25: Added as here the elevation really comes from.
     */
    Double getElevation(LatLon position);
}
