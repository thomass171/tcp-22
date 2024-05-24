package de.yard.threed.graph;

import de.yard.threed.core.Degree;
import de.yard.threed.core.LocalTransform;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.javacommon.SimpleHeadlessPlatformFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 */
public class GraphComponentTest {
    static Platform platform = EngineTestFactory.initPlatformForTest(new String[]{"engine", "data"}, new SimpleHeadlessPlatformFactory());

    /**
     * Test graph projections (which is used by groundnet)
     */
    @Test
    public void testProjectedGraph() {
        runProjectedGraph(GraphTestUtil.buildSimpleGraphProjection());
    }

    @Test
    public void testUnProjectedGraph() {
        runProjectedGraph(null);
    }

    public void runProjectedGraph(GraphProjection projection) {

        GeoGraphForTest geoGraphForTest = new GeoGraphForTest(projection);

        GraphMovingComponent gmc = new GraphMovingComponent();
        GraphPosition start = new GraphPosition(geoGraphForTest.e0);

        gmc.setGraph(geoGraphForTest.graph, start, null);

        LocalTransform projectedLocalTransform;
        double expectedX;
        if (projection == null) {
            expectedX = 50.76;
        } else {
            expectedX = 53.76;
            projectedLocalTransform = projection.project(gmc.getCurrentposition());
            assertEquals(expectedX, projectedLocalTransform.position.getX());
        }

        projectedLocalTransform = GraphMovingSystem.getTransform(gmc);
        assertEquals(expectedX, projectedLocalTransform.position.getX());

        projectedLocalTransform = GraphMovingSystem.getPosRot(gmc/*, projection*/);
        assertEquals(expectedX, projectedLocalTransform.position.getX());
    }

}
