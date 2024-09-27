package de.yard.threed.traffic;

import de.yard.threed.core.MathUtil2;
import de.yard.threed.core.NumericValue;
import de.yard.threed.core.geometry.Primitives;
import de.yard.threed.core.geometry.SimpleGeometry;
import de.yard.threed.engine.Material;
import de.yard.threed.engine.Mesh;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.Texture;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.VelocityComponent;
import de.yard.threed.graph.GraphMovingComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * Doppelbedeutung: Einerseits der aktuelle "Kontext", aber auch eine Entity zur Darstellung
 * eines Himmelskörpers. Ob diese Doppelbedeutung so aufgeht? Naja.
 * Nur der Name "Sphere" führt zu Doppeldeutigkeiten z.B. mit ShapeGeometry und CSG.
 * <p>
 * Created on 19.10.19.
 */
public class TravelSphere {
    int SPHERE_SPACE = 2;
    int SPHERE_TERRESTRIAL = 3;
    public String name;
    public static List<TravelSphere> spheres = new ArrayList<TravelSphere>();

    public TravelSphere(String name) {
        this.name=name;
    }


    public static TravelSphere getByName(String name){
        for (TravelSphere sphere:spheres){
            if (sphere.name.equals(name)){
                return sphere;
            }
        }
        return null;
    }

    public static void add(TravelSphere sphere) {
        spheres.add(sphere);
    }

    public static double calculateSpeedFromRadius(double radius, int rotationTimeInSeconds) {
        return MathUtil2.PI2*radius / rotationTimeInSeconds;
    }

    /**
     * Anders als ModelSamples.buildEarth(): Primitives statt ShapeGeometry
     * Wird direkt eine Entity, um sich per GMC im Orbit zu bewegen.
     *
     * @return
     */
    public EcsEntity buildSphere(double radius, String name, String texture) {
        int shading = NumericValue.SMOOTH;
        int segments = 32;

        SimpleGeometry geo = Primitives.buildSphereGeometry(radius,segments,segments);
        Material mat = Material.buildLambertMaterial((Texture.buildBundleTexture("data-old", texture)),null,false);
        //mat.setWireframe(true);
        Mesh mesh = new Mesh(geo, mat);
        SceneNode node = new SceneNode();
        node.setMesh(mesh);
        node.setName(name);

        GraphMovingComponent gmc = new GraphMovingComponent(node.getTransform());

        EcsEntity e = new EcsEntity(node, gmc);
        VelocityComponent vc = new VelocityComponent();
        e.addComponent(vc);
        e.setName(name);

        return e;
    }
}
