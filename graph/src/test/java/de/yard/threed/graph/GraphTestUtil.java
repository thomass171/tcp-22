package de.yard.threed.graph;


import de.yard.threed.core.testutil.TestUtil;

import static org.junit.Assert.*;

public class GraphTestUtil {
    public static void assertGraphPosition(String label, GraphPosition expected, GraphPosition actual) {
        assertEquals("graphposition.currentedge", expected.currentedge.getId(), actual.currentedge.getId());
        assertEquals("graphposition.edgeposition", expected.edgeposition, actual.edgeposition,0.001);
        assertTrue("graphposition.isReverseOrientation", expected.isReverseOrientation() == actual.isReverseOrientation());

    }

    public static void assertGraphPathSegment(String label, String[] expectednodenames, GraphPathSegment actual) {
        assertEquals(label + ":GraphPathSegment.enternode", expectednodenames[0], actual.getEnterNode().getName());
        assertEquals(label + ":GraphPathSegment.leavenode", expectednodenames[1], actual.getLeaveNode().getName());

    }

    public static void assertNode(String label, GraphNode expectednode, GraphNode actual) {
        assertEquals(label + ":node.name", expectednode.getName(), actual.getName());
        assertEquals(label + ":node.edgecount", expectednode.getEdgeCount(), actual.getEdgeCount());
    }

    public static void assertEdge(String label, GraphEdge expected, GraphEdge actual) {
        assertEquals(label + ":edge.name", expected.getName(), actual.getName());
        if (expected.getArc() != null){
            assertNotNull(actual.getArc());
            TestUtil.assertVector3(label + ":edge.arc.center", expected.getArc().getCenter(), actual.getArc().getCenter());
            assertEquals(label + ":edge.arc.beta", expected.getArc().getBeta(), actual.getArc().getBeta(),0.001);
            assertEquals(label + ":edge.arc.radius", expected.getArc().getRadius(), actual.getArc().getRadius(),0.001);
            TestUtil.assertVector3(label + ":edge.arc.ex", expected.getArc().getEx(), actual.getArc().getEx());
        }else{
            assertNull(actual.getArc());
        }
    }
}
