package de.yard.threed.engine.testutil;

import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.NativeCollision;
import de.yard.threed.core.platform.NativeRay;

import java.util.List;

/**
 * There is no Ray in Test/SimpleHeadless platform for now.
 * But for testing it might even be better to mock it.
 * <p>
 * 30.3.22
 */
public class MockedRay implements NativeRay {

    List<NativeCollision> collisions;
    Vector3 origin;
    Vector3 direction;

    public MockedRay(Vector3 origin, Vector3 direction, List<NativeCollision> collisions) {
        this.origin = origin;
        this.direction = direction;
        this.collisions = collisions;
    }

    @Override
    public Vector3 getDirection() {
        return direction;
    }

    @Override
    public Vector3 getOrigin() {
        return origin;
    }

    @Override
    public List<NativeCollision> getIntersections() {
        return collisions;
    }
}
