package de.yard.threed.graph;


import de.yard.threed.core.testutil.TestUtil;

import static org.junit.jupiter.api.Assertions.*;

public class GraphTestUtil {
    public static void assertGraphPosition(String label, GraphPosition expected, GraphPosition actual) {
        assertEquals(expected.currentedge.getId(), actual.currentedge.getId(), "graphposition.currentedge");
        assertEquals(expected.edgeposition, actual.edgeposition, 0.001, "graphposition.edgeposition");
        assertTrue(expected.isReverseOrientation() == actual.isReverseOrientation(), "graphposition.isReverseOrientation");

    }

    public static void assertGraphPathSegment(String label, String[] expectednodenames, GraphPathSegment actual) {
        assertEquals(expectednodenames[0], actual.getEnterNode().getName(), label + ":GraphPathSegment.enternode");
        assertEquals(expectednodenames[1], actual.getLeaveNode().getName(), label + ":GraphPathSegment.leavenode");

    }

    public static void assertNode(String label, GraphNode expectednode, GraphNode actual) {
        assertEquals(expectednode.getName(), actual.getName(), label + ":node.name");
        assertEquals(expectednode.getEdgeCount(), actual.getEdgeCount(), label + ":node.edgecount");
    }

    public static void assertEdge(String label, GraphEdge expected, GraphEdge actual) {
        assertEquals( expected.getName(), actual.getName(),label + ":edge.name");
        if (expected.getArc() != null) {
            assertNotNull(actual.getArc());
            TestUtil.assertVector3(label + ":edge.arc.center", expected.getArc().getCenter(), actual.getArc().getCenter());
            assertEquals(expected.getArc().getBeta(), actual.getArc().getBeta(), 0.001, label + ":edge.arc.beta");
            assertEquals(expected.getArc().getRadius(), actual.getArc().getRadius(), 0.001, label + ":edge.arc.radius");
            TestUtil.assertVector3(label + ":edge.arc.ex", expected.getArc().getEx(), actual.getArc().getEx());
        } else {
            assertNull(actual.getArc());
        }
    }
}
