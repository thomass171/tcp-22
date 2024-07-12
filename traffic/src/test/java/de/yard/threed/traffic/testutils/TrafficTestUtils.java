package de.yard.threed.traffic.testutils;

import de.yard.threed.core.LatLon;
import de.yard.threed.core.LocalTransform;
import de.yard.threed.core.MathUtil2;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Util;
import de.yard.threed.core.Vector3;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.testutil.SceneRunnerForTesting;
import de.yard.threed.graph.DefaultEdgeBasedRotationProvider;
import de.yard.threed.graph.Graph;
import de.yard.threed.graph.GraphEdge;
import de.yard.threed.graph.GraphMovingComponent;
import de.yard.threed.graph.GraphPosition;
import de.yard.threed.traffic.SimpleEllipsoidCalculations;
import de.yard.threed.traffic.geodesy.GeoCoordinate;

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
}
