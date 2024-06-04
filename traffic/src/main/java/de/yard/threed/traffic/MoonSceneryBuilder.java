package de.yard.threed.traffic;

import de.yard.threed.core.NumericValue;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.geometry.Primitives;
import de.yard.threed.core.geometry.Rectangle;
import de.yard.threed.core.geometry.SimpleGeometry;
import de.yard.threed.engine.Material;
import de.yard.threed.engine.Mesh;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.Texture;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.graph.GraphNode;
import de.yard.threed.traffic.geodesy.MapProjection;

/**
 * 9.5.24 just a draft.
 */
public class MoonSceneryBuilder implements AbstractSceneryBuilder {

    public static double MOON_RADIUS = (3474.0 / 2.0) * 1000;

    @Override
    public void init(SceneNode destinationNode) {
        int shading = NumericValue.SMOOTH;
        int segments = 512;

        SimpleGeometry geo = Primitives.buildSphereGeometry(MOON_RADIUS, segments, segments);
        Material mat = Material.buildLambertMaterial((Texture.buildBundleTexture("data", "images/river.jpg")), false, false);
        //mat.setWireframe(true);
        Mesh mesh = new Mesh(geo, mat);
        SceneNode node = new SceneNode();
        node.setMesh(mesh);
        //node.setName(name);
        destinationNode.attach(node);
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
        return new SimpleEllipsoidCalculations(MOON_RADIUS);
    }
}
