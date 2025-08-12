package de.yard.threed.traffic;

import de.yard.threed.core.LatLon;
import de.yard.threed.core.NumericValue;
import de.yard.threed.core.geometry.Primitives;
import de.yard.threed.core.geometry.Rectangle;
import de.yard.threed.core.geometry.SimpleGeometry;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.Material;
import de.yard.threed.engine.Mesh;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.Texture;
import de.yard.threed.graph.GraphNode;
import de.yard.threed.trafficcore.EllipsoidCalculations;
import de.yard.threed.trafficcore.SimpleEllipsoidCalculations;
import de.yard.threed.trafficcore.geodesy.MapProjection;

import java.util.ArrayList;
import java.util.List;

/**
 * 9.5.24 just a draft.
 */
public class MoonSceneryBuilder implements AbstractSceneryBuilder {
    Log logger = Platform.getInstance().getLog(MoonSceneryBuilder.class);
    public static double MOON_RADIUS = (3474.0 / 2.0) * 1000;
    // for testing
    public static List<LatLon> updatedPositions = new ArrayList<>();

    @Override
    public void init(SceneNode destinationNode) {
        int segments = 512;

        SimpleGeometry geo = Primitives.buildSphereGeometry(MOON_RADIUS, segments, segments);
        Material mat = Material.buildLambertMaterial((Texture.buildBundleTexture("data", "images/river.jpg")), null, false);
        //mat.setWireframe(true);
        Mesh mesh = new Mesh(geo, mat);
        SceneNode node = new SceneNode();
        node.setMesh(mesh);
        //node.setName(name);
        destinationNode.attach(node);
    }

    @Override
    public void updateForPosition(LatLon position) {
        updatedPositions.add(position);
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
        return new SimpleEllipsoidCalculations(MOON_RADIUS);
    }

    @Override
    public Double getElevation(LatLon position) {
        logger.warn("not implemented. Using fix elevation 137");
        return 137.0;
    }
}
