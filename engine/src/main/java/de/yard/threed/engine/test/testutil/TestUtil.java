package de.yard.threed.engine.test.testutil;

import de.yard.threed.core.*;
import de.yard.threed.core.testutil.Assert;
import de.yard.threed.core.Vector3Array;
import de.yard.threed.core.Color;
import de.yard.threed.engine.platform.common.Face3;
import de.yard.threed.engine.platform.common.FaceN;


/**
 * Date: 04.06.14
 */
public class TestUtil extends de.yard.threed.core.testutil.TestUtil {

    public static void assertFace3(String msg, int[] expected, Face3 actual) {
        assertEquals(msg + ".a", expected[0], actual.index0);
        assertEquals(msg + ".b", expected[1], actual.index1);
        assertEquals(msg + ".c", expected[2], actual.index2);
    }

    public static void assertFace3(String msg, int[] expected, int[] actual, int pos) {
        assertEquals(msg + ".a", expected[0], actual[pos * 3]);
        assertEquals(msg + ".b", expected[1], actual[pos * 3 + 1]);
        assertEquals(msg + ".c", expected[2], actual[pos * 3 + 2]);
    }

    public static void assertFace4(String msg, int[] expected, FaceN actual) {
        assertEquals(msg + ".a", expected[0], actual.index[0]);
        assertEquals(msg + ".b", expected[1], actual.index[1]);
        assertEquals(msg + ".c", expected[2], actual.index[2]);
        assertEquals(msg + ".d", expected[3], actual.index[3]);
    }


    public static void assertColor(String label, Color expected, Color actual) {
        Assert.assertEquals(label + ": v.r", expected.getRasInt(), actual.getRasInt());
        Assert.assertEquals(label + ": v.g", expected.getGasInt(), actual.getGasInt());
        Assert.assertEquals(label + ": v.b", expected.getBasInt(), actual.getBasInt());
        Assert.assertEquals(label + ": v.a", expected.getAlphaasInt(), actual.getAlphaasInt());
    }

    public static void assertVector3(String label, Vector3 v, Vector3Array ar, int startindex, int count) {
        for (int i = startindex; i < startindex + count; i++) {
            assertVector3(label + ":" + i, v, ar.getElement(i));
        }
    }

    public static void assertFaceIndexNormals(int[] indices, Vector3Array normals, int[] faceindices, Vector3 refnormal) {
        for (int i = 0; i < faceindices.length; i++) {
            assertVector3("faceindex0 " + faceindices[i], refnormal, normals.getElement(indices[faceindices[i] * 3]));
            assertVector3("faceindex1 " + faceindices[i] + 1, refnormal, normals.getElement(indices[faceindices[i] * 3 + 1]));
            assertVector3("faceindex2 " + faceindices[i] + 2, refnormal, normals.getElement(indices[faceindices[i] * 3 + 2]));
        }
    }
}
