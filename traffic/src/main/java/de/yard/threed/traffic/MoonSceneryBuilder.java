package de.yard.threed.traffic;

import de.yard.threed.core.Vector3;
import de.yard.threed.core.geometry.Rectangle;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.graph.GraphNode;
import de.yard.threed.traffic.geodesy.MapProjection;

/**
 * 9.5.24 just a draft.
 */
public class MoonSceneryBuilder implements AbstractSceneryBuilder {
    @Override
    public void init(SceneNode destinationNode) {

    }

    @Override
    public void updateForPosition(Vector3 position, Vector3 direction) {

    }

    @Override
    public Rectangle getLastTileSize() {
        return null;
    }

    @Override
    public void buildTerrain(Object p0, Object p1, MapProjection projection) {

    }

    @Override
    public SceneNode buildParkingNode(GraphNode n) {
        return null;
    }

    @Override
    public EllipsoidCalculations getEllipsoidCalculations() {
        return new SimpleEllipsoidCalculations(1000);
    }
}
