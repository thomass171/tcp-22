package de.yard.threed.platform.homebrew;

import de.yard.threed.core.Matrix4;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.geometry.GeometryHelper;
import de.yard.threed.core.platform.*;

import de.yard.threed.core.MathUtil2;
import de.yard.threed.engine.Scene;

import java.util.ArrayList;
import java.util.List;

/**
 * Ein Strahl bestimmter Laenge von einem Ausgangspunkt in eine Richtung.
 *
 * 23.9.23: Generic parts extracted to GeometryHelper.
 * <p/>
 * Created by thomass on 25.11.14.
 */
public class OpenGlRay implements NativeRay {
    Log logger = Platform.getInstance().getLog(OpenGlRay.class);

    Vector3 origin, direction;
    double length;

    public OpenGlRay(Vector3 origin, Vector3 direction) {
        this(origin, direction, java.lang.Double.MAX_VALUE);
    }

    public OpenGlRay(Vector3 origin, Vector3 direction, double length) {
        this.origin = origin;
        // Das mit normalize ist aus http://gamedev.stackexchange.com/questions/72440/the-correct-way-to-transform-a-ray-with-a-matrix
        // Sicherstellen, dass die direction immer normalisiert ist.
        this.direction = direction;//.normalize();
        this.length = length;
    }

    @Override
    public String toString() {
        return "maze from " + origin + " with direction " + direction;
    }

    public Vector3 getDirection() {
        return direction;
    }

    public Vector3 getOrigin() {
        return origin;
    }

    /**
     * 22.3.18:Muss ja rekursiv suchen
     */
    public List<NativeCollision> intersects(NativeSceneNode model) {
        HomeBrewSceneNode n = (HomeBrewSceneNode) model;
        List<NativeCollision> na = new ArrayList<NativeCollision>();
        TransformNodeVisitor nodeVisitor = new TransformNodeVisitor() {

            @Override
            public void handleNode(NativeTransform node) {
                HomeBrewSceneNode n = (HomeBrewSceneNode) node.getSceneNode();
                HomeBrewMesh mesh = (HomeBrewMesh) n.getMesh();
                if (mesh != null) {
                    List<Vector3> results = getIntersection(mesh.geo, n.getTransform().getWorldModelMatrix());
                    for (Vector3 p : results) {
                        na.add(new OpenGlCollision(n, p));
                    }
                }
            }
        };
        //intersects(n, na);
        PlatformHelper.traverseTransform(model.getTransform(), nodeVisitor);
        return na;
    }

    @Override
    public List<NativeCollision> getIntersections() {
       //24.9.19 return intersects(OpenGlScene.root);
        return intersects(Scene.getCurrent().getWorld().nativescenenode);
    }


    /**
     *
     * Also exists in SimpleHeadlessPlatform.
     *
     * Liefert die Punkte, an denen sich die Geometrie mit dem Strahl schneiden.
     * Die Reihenfolge ist nicht bestimmt bzw. zufaellig.
     * <p/>
     * 26.8.2016: Gar nicht so einfach, denn die Vertexdaten sind ja in der GPU. Der VBO liegt noch vor. Den erstmal verwenden.
     * Koennte optimiert werden (nur bei bestimmten meshes) und verallgemeinert fuer alle Platformen.
     *
     * Die Vertexdaten sind alle im local space. Der Ray muss dahin transformiert werden.
     * 22.3.18: Also, ob das so das wahre ist?
     * @return
     */
    private List<Vector3> getIntersection(HomeBrewGeometry geo, Matrix4 worldModelMatrix) {
        Matrix4 worldModelMatrixInverse = MathUtil2.getInverse(worldModelMatrix);
        Vector3 lorigin = worldModelMatrixInverse.transform(origin);
        //die direction wird nicht transformiert. Häh?
        //Vector3 ldirection = worldModelMatrixInverse.transform(direction);
        List<Vector3> intersections = GeometryHelper.getRayIntersections(geo.vertices, geo.indices, lorigin, direction);
        // transform intersections back to world space of ray.
        List<Vector3> transformeedIntersections = new ArrayList<Vector3>();
        for (Vector3 intersection : intersections) {
            transformeedIntersections.add(worldModelMatrix.transform(intersection));
        }
        return transformeedIntersections;
    }

    public OpenGlRay transform(Matrix4 m) {
        // Das mit dem transpose bei der Direction hat den gleichen (unklaren) Grund wie bei den Normalen
        return new OpenGlRay(m.transform(origin),  MathUtil2.transpose(m).transform(direction));
    }
}
