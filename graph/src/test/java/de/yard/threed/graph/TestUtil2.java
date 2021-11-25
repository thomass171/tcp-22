package de.yard.threed.graph;

import de.yard.threed.core.testutil.TestUtil;

public class TestUtil2 {
    public static void assertGraphPosition(String label, GraphPosition expected, GraphPosition actual) {
        TestUtil.assertEquals("graphposition.currentedge", expected.currentedge.getId(), actual.currentedge.getId());
        TestUtil.assertEquals("graphposition.edgeposition", expected.edgeposition, actual.edgeposition);
        TestUtil.assertTrue("graphposition.isReverseOrientation", expected.isReverseOrientation() == actual.isReverseOrientation());

    }

    public static void assertGraphPathSegment(String label, String[] expectednodenames, GraphPathSegment actual) {
        TestUtil.assertEquals(label + ":GraphPathSegment.enternode", expectednodenames[0], actual.getEnterNode().getName());
        TestUtil.assertEquals(label + ":GraphPathSegment.leavenode", expectednodenames[1], actual.getLeaveNode().getName());

    }
}
