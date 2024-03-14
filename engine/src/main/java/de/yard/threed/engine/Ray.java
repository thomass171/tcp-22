package de.yard.threed.engine;

import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.*;

import java.util.ArrayList;
import java.util.List;

/**
 * A ray with a length starting at origin in a direction. Origin is in world coordinates.
 * <p>
 * <p/>
 * Created by thomass on 25.11.14.
 */
public class Ray {
    Log logger = Platform.getInstance().getLog(Ray.class);
    NativeRay ray;

    public Ray(Vector3 origin, Vector3 direction) {
        this(origin, direction, java.lang.Double.MAX_VALUE);
    }

    public Ray(Vector3 origin, Vector3 direction, double length) {
        // Normalize is from http://gamedev.stackexchange.com/questions/72440/the-correct-way-to-transform-a-ray-with-a-matrix
        // Its important that direction is normalized. At least ThreeJs needs this.
        ray = Platform.getInstance().buildRay(origin, direction.normalize());
    }

    public Ray(NativeRay ray) {
        this.ray = ray;
    }

    @Override
    public String toString() {
        return "ray from " + getOrigin() + " with direction " + getDirection();
    }

    public Vector3 getDirection() {
        return (ray.getDirection());
    }

    public Vector3 getOrigin() {
        return ((ray.getOrigin()));
    }


    /**
     * Be careful with parents/children. These are only tested with 'recursive'.
     * <p>
     * 30.3.22: Different approach to avoid deprecated native methods (MA22).
     * 24.9.23: Why do we search from world here? Probably because there is no platfrom independent way
     * to check a single geometry for intersection. But this method makes the impression it is possible.
     * For that reason deprecated. At least it should be renamed to something like getIntersectionsFromWorld/Top.
     */
    @Deprecated
    public List<NativeCollision> getIntersections(SceneNode model, boolean recursive) {
        List<NativeCollision> collisions = getIntersections();
        return extractModelsHit(model, collisions, recursive);
    }

    /**
     * 21.5.21
     *
     * @param model
     * @return
     */
    public boolean intersects(SceneNode model, boolean recursive) {
        return getIntersections(model, recursive).size() > 0;
    }

    /**
     * Be careful with parents/children. These are only tested with 'recursive'.
     * 30.3.22: Different approach to avoid deprecated native methods (MA22).
     *
     * @param modellist
     * @return collisions sorted by distance (closest at beginning of list)
     */
    public List<NativeCollision> getIntersections(List<SceneNode> modellist, boolean recursive) {
        List<NativeCollision> imodellist = new ArrayList<NativeCollision>();
        for (SceneNode m : modellist) {
            imodellist.addAll(getIntersections(m, recursive));
        }
        imodellist.sort((o1, o2) -> Vector3.getDistance(getOrigin(), o1.getPoint()) < Vector3.getDistance(getOrigin(), o2.getPoint()) ? -1 : 1);
        return imodellist;
    }

    /**
     * Detect all intersections without considering sub graphs.
     * More Unity Like.
     * See comments in {@link NativeRay}.
     *
     * @return
     */
    public List<NativeCollision> getIntersections() {
        long startTime = Platform.getInstance().currentTimeMillis();
        List<NativeCollision> result = ray.getIntersections();
        long took = Platform.getInstance().currentTimeMillis() - startTime;
        // there is no indicator currently that ray intersection is a performance bottleneck.
        // But now there seems to be an indicator. Eg. just clicking in the sky in TravelScene. 14.3.24: 50->20
        if (took > 20) {
            logger.warn("intersection detection took " + took + " ms");
        }
        return result;
    }

    private List<NativeCollision> extractModelsHit(SceneNode model, List<NativeCollision> collisions, boolean recursive) {
        List<NativeCollision> imodellist = new ArrayList<NativeCollision>();

        for (NativeCollision collision : collisions) {
            NativeSceneNode collidingNode = collision.getSceneNode();
            if (model.nativescenenode.getUniqueId() == collidingNode.getUniqueId()) {
                imodellist.add(collision);
            }
        }
        if (recursive) {
            for (Transform c : model.getTransform().getChildren()) {
                imodellist.addAll(extractModelsHit(c.getSceneNode(), collisions, recursive));
            }
        }
        return imodellist;
    }
}
