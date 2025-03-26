package de.yard.threed.traffic.testutils;

import de.yard.threed.core.LatLon;
import de.yard.threed.core.LocalTransform;
import de.yard.threed.core.MathUtil2;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Util;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.testutil.SceneRunnerForTesting;
import de.yard.threed.graph.Graph;
import de.yard.threed.graph.GraphEdge;
import de.yard.threed.graph.GraphMovingComponent;
import de.yard.threed.graph.GraphPosition;
import de.yard.threed.traffic.VehicleLauncher;
import de.yard.threed.traffic.geodesy.GeoCoordinate;
import org.slf4j.Logger;

import static de.yard.threed.core.testutil.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNull;


public class TrafficTestUtils {


    public static void runAdditionalFrames(SceneRunnerForTesting sceneRunner, int frames) {
        Util.notyet();
        /*sceneRunner.frameLimit = frames;
        sceneRunner.startRenderloop();*/
    }

    public static void assertGeoCoordinate(GeoCoordinate expected, GeoCoordinate actual, String label) {
        assertEquals(expected.getLatDeg().getDegree(), actual.getLatDeg().getDegree(), 0.000001, "LatitudeDeg");
        assertEquals(expected.getLonDeg().getDegree(), actual.getLonDeg().getDegree(), 0.000001, "LongitudeDeg");
    }

    /**
     * Assert that the 3D rotation (posrot) of a real (not projected) geo graph derived from a GeoRoute is correct.
     * How do we validate that: We take a default forward pointer, rotate it by the returned rotation on some edge, move
     * some distance from a location on the edge and check that we arrive still on the edge.
     * The caller needs to make sure that the edge to use is long enough!
     * <p>
     * <p>
     * The rotation of the vehicle (OpenGL, FG, AC) has no effect to the graph rotation!
     */
    public static void assertGeoGraphRotation(Graph graph, GraphEdge edge, double distance) {
        GraphPosition graphPosition = new GraphPosition(edge, 0);
        LocalTransform posrot = graph.getPosRot(graphPosition, new Quaternion());

        GeoCoordinate first = GeoCoordinate.fromLatLon(LatLon.fromDegrees(50.768, 7.1672000), 60);
        GeoCoordinate second = GeoCoordinate.fromLatLon(LatLon.fromDegrees(50.7692, 7.1617000), 60);

        // revert due to typical forward orientation confusion
        Vector3 defaultForward = MathUtil2.DEFAULT_FORWARD.negate();
        Vector3 rotatedForward = defaultForward.rotate(posrot.rotation);
        // -x, -y, +z
        //log.debug("distance={},rotatedForward={}", distance, rotatedForward);
        Vector3 rotTarget = edge.from.getLocation().add(rotatedForward.multiply(distance));
        //log.debug("rotTarget={}", rotTarget);
        // 50.7690336,7.1622472 is correct (appx. 100m from first on runway) according to visual map check
        assertVector3(graph.getPosRot(new GraphPosition(edge, distance), new Quaternion()).position, rotTarget, 0.001, "rotTarget");

        // check UP at edge begin. Not sure that is valid always
        Vector3 rotatedUp = MathUtil2.DEFAULT_UP.rotate(posrot.rotation);
        assertVector3(posrot.position.normalize(), rotatedUp, 0.001, "rotatedUp");

    }

    public static void assertEntityOnGraph(EcsEntity entity, boolean expectPath, Quaternion expectedModelRotation) {
        GraphMovingComponent gmc = GraphMovingComponent.getGraphMovingComponent(entity);
        assertNotNull(gmc.getGraph());
        assertFalse(gmc.hasAutomove());
        assertEquals(expectPath, gmc.getPath() != null);
        assertQuaternion(expectedModelRotation, gmc.customModelRotation);
    }

    /**
     *
     */
    public static void assertVehicleEntity(EcsEntity entity, String vehicleName, double expectedZOffset, Vector3 expectedPosition, String expectedParent, Logger log) {
        GraphMovingComponent gmc = GraphMovingComponent.getGraphMovingComponent(entity);
        SceneNode entityNode = entity.getSceneNode();
        log.debug("entityNode: {}", entityNode.dump("  ", 0));
        assertEquals("vehiclecontainer-" + vehicleName, entityNode.getName());
        if (expectedPosition != null) {
            assertVector3(expectedPosition, entityNode.getTransform().getPosition());
        }
        SceneNode entityParentNode = entityNode.getTransform().getParent().getSceneNode();
        assertEquals(expectedParent, entityParentNode.getName());
        SceneNode vehicle_container = getSingleChild(entityNode, "vehicle-container");
        assertVehicleNodeHierarchy(vehicle_container, expectedZOffset);
    }

    /**
     * Tests in both node 'directions'
     * Scetch 37
     */
    public static void assertVehicleNodeHierarchy(SceneNode container, double expectedZOffset) {
        assertEquals("vehicle-container", container.getName());
        // from top
        SceneNode zoffsetnode1 = getSingleChild(container, "zoffsetnode");
        TestUtils.assertQuaternion(new Quaternion(), zoffsetnode1.getTransform().getRotation());

        SceneNode basenode = getSingleChild(zoffsetnode1, "basenode");
        TestUtils.assertQuaternion(new Quaternion(), basenode.getTransform().getRotation());

        // from bottom
        SceneNode vehicleModelnode = VehicleLauncher.getModelNodeFromVehicleNode(container);
        assertEquals("vehicle-container", container.getName());
        TestUtils.assertQuaternion(new Quaternion(), container.getTransform().getRotation());

        assertEquals("basenode", vehicleModelnode.getName());
        TestUtils.assertQuaternion(new Quaternion(), vehicleModelnode.getTransform().getRotation());

        SceneNode zoffsetnode = vehicleModelnode.getParent();
        assertEquals("zoffsetnode", zoffsetnode.getName());
        TestUtils.assertVector3(new Vector3(0, 0, expectedZOffset), zoffsetnode.getTransform().getPosition());
        TestUtils.assertQuaternion(new Quaternion(), zoffsetnode.getTransform().getRotation());

        // container1 is container again
        SceneNode container1 = zoffsetnode.getParent();
        assertEquals("vehicle-container", container1.getName());
    }

    public static SceneNode getSingleChild(SceneNode node, String expectedChildName) {
        if (node.getTransform().getChildCount() != 1) {
            fail("not one child");
        }
        SceneNode child = node.getTransform().getChild(0).getSceneNode();
        assertEquals(expectedChildName, child.getName());
        return child;
    }
}
