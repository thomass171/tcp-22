package de.yard.threed.engine;

import de.yard.threed.core.Color;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.geometry.Primitives;
import de.yard.threed.core.geometry.SimpleGeometry;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeCollision;
import de.yard.threed.core.platform.NativeRay;
import de.yard.threed.core.platform.NativeSceneNode;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.apps.ModelSamples;
import de.yard.threed.engine.testutil.PlatformFactoryHeadless;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.javacommon.SimpleHeadlessPlatformFactory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Ray intersection is part of the platform, so there are runtime tests (Geometrytest and ReferenceTests).
 * This is only a test for SimpleHeadlessPlatform and GeometryHelper.getRayIntersections().
 * <p>
 * Created by thomass on 07.02.16.
 */
public class RayTest {
    Platform platform = EngineTestFactory.initPlatformForTest(new String[]{"engine"}, new SimpleHeadlessPlatformFactory());

    @Test
    void testSimpleRayIntersection() {
        Log logger = Platform.getInstance().getLog(RayTest.class);
        SimpleGeometry boxgeometry = Primitives.buildBox(1, 0.1, 1);
        // no material needed
        SceneNode box = new SceneNode(new Mesh(boxgeometry, null));
        box.setName("box");
        box.getTransform().setPosition(new Vector3(4, 0, -1));

        // intersections only work inside 'world'
        SceneNode world = new SceneNode(SceneNode.findByName("World").get(0));
        box.getTransform().setParent(world.getTransform());

        // vertical ray centered above box. Might return up to 4 intersections
        Vector3 origin = new Vector3(4, 15, -1);
        Vector3 direction = new Vector3(0, -1, 0);
        Ray ray = new Ray(origin, direction);

        List<NativeCollision> intersections = ray.getIntersections();

        for (NativeCollision intersection : intersections) {
            logger.debug("intersection=" + intersection.getPoint() + "," + intersection.getSceneNode().getName());
        }

        // vertical ray not exactly centered above box. Should return 2 intersections
        origin = new Vector3(4.1, 15, -1);
        ray = new Ray(origin, direction);

        intersections = ray.getIntersections();

        for (NativeCollision intersection : intersections) {
            logger.debug("intersection=" + intersection.getPoint() + "," + intersection.getSceneNode().getName());
        }
        assertEquals(2, intersections.size());
    }

}
