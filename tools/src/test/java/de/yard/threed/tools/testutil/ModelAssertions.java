package de.yard.threed.tools.testutil;

import de.yard.threed.core.Color;
import de.yard.threed.core.Degree;
import de.yard.threed.core.PortableModelTest;
import de.yard.threed.core.Vector2;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.geometry.FaceN;
import de.yard.threed.core.geometry.SimpleGeometry;
import de.yard.threed.core.loader.LoaderGLTF;
import de.yard.threed.core.loader.PortableMaterial;
import de.yard.threed.core.loader.PortableModel;
import de.yard.threed.core.loader.PortableModelDefinition;
import de.yard.threed.engine.test.testutil.TestUtil;

import static de.yard.threed.core.testutil.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

public class ModelAssertions {

    static Vector3 normalX = new Vector3(1, 0, 0);
    static Vector3 normalY = new Vector3(0, 1, 0);
    static Vector3 normalZ = new Vector3(0, 0, 1);

    public static void assertMultiObject(PortableModel portableModel, boolean loadedFromGltf,
                                         boolean acworldIncluded, boolean twoSidedAsSingleSided) {
        PortableModelDefinition root = portableModel.getRoot();

        if (loadedFromGltf) {
            root = PortableModelTest.validateSingleRoot(root, LoaderGLTF.GLTF_ROOT);
            assertEquals("multi-object.gltf", portableModel.getName());
        }

        PortableModelTest.assertLevel(root, "ac-world", new String[]{"obj1", "obj2", "obj3", "obj4", "obj5", "obj6", "obj7", "obj8"});

        PortableModelDefinition obj1 = portableModel.findObject("obj1");
        assertNotNull(obj1);

        assertEquals(0, obj1.kids.size());
        assertEquals("obj1", obj1.getName());
        assertEquals("unshaded0white", obj1.material);

        PortableMaterial white = portableModel.findMaterial("unshaded0white");
        assertNotNull(white);
        assertFalse(white.isShaded());
        assertNull(white.getTexture());
        assertNull(white.getTransparency());
        TestUtil.assertColor("unshaded0white.color", new Color(1.0f, 1.0f, 1.0f), white.getColor());
        TestUtil.assertColor("unshaded0white.color", new Color(0.15f, 0.15f, 0.15f), white.getEmis());

        PortableModelDefinition obj2 = portableModel.findObject("obj2");
        assertNotNull(obj2);

        assertEquals(0, obj2.kids.size());
        assertEquals("obj2", obj2.getName());
        assertEquals("unshaded1glass", obj2.material);

        PortableMaterial glass = portableModel.findMaterial("unshaded1glass");
        assertNotNull(glass);
        assertFalse(glass.isShaded());
        assertNull(glass.getTexture());
        assertNotNull(glass.getTransparency());
        assertEquals(0.27, glass.getTransparency().floatValue(), 0.001);

        PortableModelDefinition obj3 = portableModel.findObject("obj3");
        assertNotNull(obj3);

        // Sketch 41 (windsock part)
        assertEquals(0, obj3.kids.size());
        assertEquals("obj3", obj3.getName());
        assertEquals("shaded2ac3dmat4", obj3.material);
        // duplication of 2 vertices happened due to uvs. twosided? For twosided we now duplicate all
        assertEquals(twoSidedAsSingleSided ? (12 + 2) : 2 * (12 + 2), obj3.geo.getVertices().size());
        assertEquals(twoSidedAsSingleSided ? (12 + 2) : 2 * (12 + 2), obj3.geo.getNormals().size());
        assertEquals((twoSidedAsSingleSided ? 1 : 2) * 12 * 3, obj3.geo.getIndices().length);

        // 12.9.24: twosided indices just taken after visual test
        int[] expectedIndices = new int[]{0, 7, 6, 0, 1, 7, 1, 8, 7, 1, 2, 8, 2, 9, 8, 2, 3, 9, 3, 10, 9, 3, 4, 10, 4, 11, 10, 4, 5, 11, 5, 24, 11, 5, 25, 6, 12, 13, 14, 13, 15, 14, 13, 16, 15, 16, 17, 15, 16, 18, 17, 18, 19, 17, 18, 20, 19, 20, 21, 19, 20, 22, 21, 22, 23, 21, 22, 26, 23, 12, 27, 23};
        if (twoSidedAsSingleSided) {
            expectedIndices = new int[]{0, 7, 6, 0, 1, 7, 1, 8, 7, 1, 2, 8, 2, 9, 8, 2, 3, 9, 3, 10, 9, 3, 4, 10, 4, 11, 10, 4, 5, 11,
                    // the last two used duplicated vertices
                    5, 12, 11, 5, 13, 6};
        }
        assertIndices("", expectedIndices, obj3.geo.getIndices());

        // normals do not reach the 'correct'/perfect value because 3 face3 meet at a point, so the majority of two spoil the
        // perfect average. Even though the calculation appears correct, the result is not 'correct'.
        Vector3 normal0 = obj3.geo.getNormals().getElement(0);
        //assertVector3(new Vector3(1,0,0), normal0);
        assertVector3(new Vector3(0.9332556, 0.1796104, 0.3110852), normal0);
        Vector3 normal3 = obj3.geo.getNormals().getElement(3);
        //assertVector3(new Vector3(-1,0,0), normal3);
        assertVector3(new Vector3(-0.933255, -0.179610, 0.31108522), normal3);
        //assertVector3Array

        PortableModelDefinition obj5 = portableModel.findObject("obj5");
        assertNotNull(obj5);

        assertEquals(2, obj5.kids.size());
        //assertEquals(0, obj4.geo??);
        assertEquals("obj5", obj5.getName());

        // "crease" was ignored because "obj6" is unshaded. Should be 'cube like'
        PortableModelDefinition obj6 = portableModel.findObject("obj6");
        assertNotNull(obj6);
        assertEquals(0, obj6.kids.size());
        assertEquals("obj6", obj6.getName());
        SimpleGeometry geo = obj6.geo;
        // no face at bottom. In total we have 10 Face3, so 30 vertices
        assertEquals(10 * 3, geo.getVertices().size());
        assertEquals(10 * 3, geo.getIndices().length);
        // hard to test each index because no simple rule expectedIndices = new int[]{4, 7, 6, 4, 6, 5, 0, 8, 9, 0, 5, 1, 10, 11, 12, 1, 6, 2, 13, 14, 15, 2, 7, 3, 16, 17, 18, 4, 3, 19};
        //assertIndices("", expectedIndices, geo.getIndices());
        assertEquals(30, geo.getIndices().length);

        // further tests require vertexMaps
        if (!loadedFromGltf) {

            assertNotNull(geo.vertexMaps);

            // top faces (sketch 34)
            assertFace4Processing(new FaceN(4, 7, 6, 5, new Vector2[]{
                    new Vector2(0.484773218632, 0.967145442963),
                    new Vector2(0.0179822817445, 0.967145442963),
                    new Vector2(0.0179822519422, 0.573368549347),
                    new Vector2(0.484772980213, 0.573368310928)}), normalY, 0, geo);
            // right faces
            assertFace4Processing(new FaceN(0, 4, 5, 1, new Vector2[]{
                    new Vector2(0.210489034653, 0.00967678427696),
                    new Vector2(0.210489034653, 0.162261396646),
                    new Vector2(0.00359664112329, 0.162261396646),
                    new Vector2(0.00359684973955, 0.00967678427696)}), normalX, 6, geo);
            // front faces
            assertFace4Processing(new FaceN(1, 5, 6, 2, new Vector2[]{
                    new Vector2(0.4966750741, 0.398539841175),
                    new Vector2(0.496674835682, 0.551433444023),
                    new Vector2(0.00517372041941, 0.551433444023),
                    new Vector2(0.00517375022173, 0.398539841175)}), normalZ, 12, geo);
            // left faces
            assertFace4Processing(new FaceN(2, 6, 7, 3, new Vector2[]{
                    new Vector2(0.21037453413, 0.0103213787079),
                    new Vector2(0.210374474525, 0.16290590167),
                    new Vector2(0.00348237901926, 0.16290590167),
                    new Vector2(0.00348228961229, 0.0103213787079)}), normalX.negate(), 18, geo);
            // back faces
            assertFace4Processing(new FaceN(4, 0, 3, 7, new Vector2[]{
                    new Vector2(0.00338683277369, 0.375841796398),
                    new Vector2(0.00338683277369, 0.222948431969),
                    new Vector2(0.494887888432, 0.222948431969),
                    new Vector2(0.494888007641, 0.375841796398)}), normalZ.negate(), 24, geo);
        }

        // "obj7" uses "crease 30". With this box like object it should lead to flat shading for all surfaces at the end.
        PortableModelDefinition obj7 = portableModel.findObject("obj7");
        assertNotNull(obj7);
        assertEquals(0, obj7.kids.size());
        assertEquals("obj7", obj7.getName());
        geo = obj7.geo;
        // In total we have 12 Face3, so 36 vertices. Hmm, might be less because some can be shared.
        assertEquals(6 * 4, geo.getVertices().size());
        assertEquals(36, geo.getIndices().length);
        // hard to test each index because no simple rule expectedIndices = new int[]{4, 7, 6, 4, 6, 5, 0, 8, 9, 0, 5, 1, 10, 11, 12, 1, 6, 2, 13, 14, 15, 2, 7, 3, 16, 17, 18, 4, 3, 19};
        //assertIndices("", expectedIndices, geo.getIndices());

        // further tests require vertexMaps
        if (!loadedFromGltf) {

            assertNotNull(geo.vertexMaps);

            // bottom faces (sketch 35)
            assertFace4Processing(new FaceN(0, 3, 5, 6, new Vector2[]{
                    new Vector2(0.395064234734, 0.390598207712),
                    new Vector2(0.395064234734, 0.437023192644),
                    new Vector2(0.3778642416, 0.437023192644),
                    new Vector2(0.3778642416, 0.390598207712)}), normalY.negate(), 0, geo);
            // top faces
            assertFace4Processing(new FaceN(7, 4, 2, 1, new Vector2[]{
                    new Vector2(0.376414179802, 0.390598207712),
                    new Vector2(0.376414179802, 0.437023192644),
                    new Vector2(0.359314203262, 0.437023192644),
                    new Vector2(0.359314203262, 0.390598207712)}), normalY, 6, geo);
            // left faces
            assertFace4Processing(new FaceN(0, 1, 2, 3, new Vector2[]{
                    new Vector2(0.123614162207, 0.376173228025),
                    new Vector2(0.123614162207, 0.431348234415),
                    new Vector2(0.068814188242, 0.431348234415),
                    new Vector2(0.068814188242, 0.376173228025)}), normalX.negate(), 12, geo);
            // front faces
            assertFace4Processing(new FaceN(1, 0, 6, 7, new Vector2[]{
                    new Vector2(0.334014177322, 0.367848217487),
                    new Vector2(0.334014177322, 0.250148206949),
                    new Vector2(0.351964235306, 0.250148206949),
                    new Vector2(0.351964235306, 0.367848217487)}), normalZ, 18, geo);
            // rear faces
            assertFace4Processing(new FaceN(4, 5, 3, 2, new Vector2[]{
                    new Vector2(0.334014177322, 0.367848217487),
                    new Vector2(0.334014177322, 0.250148206949),
                    new Vector2(0.351964235306, 0.250148206949),
                    new Vector2(0.351964235306, 0.367848217487)}), normalZ.negate(), 24, geo);
            // right faces
            assertFace4Processing(new FaceN(7, 6, 5, 4, new Vector2[]{
                    new Vector2(0.068814188242, 0.435523182154),
                    new Vector2(0.068814188242, 0.375598222017),
                    new Vector2(0.123614162207, 0.375598222017),
                    new Vector2(0.123614162207, 0.435523182154)}), normalX, 30, geo);
        }

        // "obj8" uses "crease 50". With all angles 45 degrees it should lead to smooth shading for all surfaces at the end.
        PortableModelDefinition obj8 = portableModel.findObject("obj8");
        assertNotNull(obj8);
        assertEquals(0, obj8.kids.size());
        assertEquals("obj8", obj8.getName());
        geo = obj8.geo;
        // In total we have 16 Face3, all smoothed, but 32 vertices because each vertex needed duplication due to different uv.
        // plus 8 duplicates for top
        assertEquals(16 * 2 + 8, geo.getVertices().size());

        // sketch 35
        // doesn't fit, probably due to weighting issue
        //assertVector3(normalZ.rotateOnAxis(new Degree(22.5), new Vector3(0,1,0)), geo.getNormals().getElement(2));
        // TODO needs more tests
    }

    /**
     * For now assume all normals the same in that face.
     */
    public static void assertFace4Processing(FaceN face4, Vector3 expectedNormal, int startIndex, SimpleGeometry geo) {
        // assume flat shading
        int expectedIndices = 6;

        for (int i = 0; i < expectedIndices; i++) {
            assertVector3(expectedNormal, geo.getNormals().getElement(geo.getIndices()[startIndex + i]));
        }
        // uvs. Assume a face4 resulting in 6 final vertices/indexes
        for (int i = 0; i < 6; i++) {
            int currentIndex = geo.getIndices()[startIndex + i];
            // assume only first map was used
            int originalIndex = geo.vertexMaps.get(0).getOriginal(currentIndex);
            // I cannot know where original was mapped to. I only can look back from index
            int facePositionOfOriginalIndex = findVertexInFace(face4, originalIndex);
            assertVector2(face4.uv[facePositionOfOriginalIndex], geo.getUvs().getElement(currentIndex));
        }
    }

    private static int findVertexInFace(FaceN face4, int originalIndex) {
        for (int i = 0; i < face4.index.length; i++) {
            if (face4.index[i] == originalIndex) {
                return i;
            }
        }
        fail("not found");
        return -1;
    }

    public static void assertDuplicateMatname(PortableModel portableModel, boolean loadedFromGltf, boolean acworldIncluded) {
        PortableModelDefinition root = portableModel.getRoot();

        if (loadedFromGltf) {
            root = PortableModelTest.validateSingleRoot(root, LoaderGLTF.GLTF_ROOT);
            assertEquals("multi-object.gltf", portableModel.getName());
        }

        PortableModelTest.assertLevel(root, "ac-world", new String[]{"obj"});

        PortableModelDefinition obj = portableModel.findObject("obj");
        assertNotNull(obj);

        // ac object "obj" was split into four due to two different materials (unshaded with mat 0 and 1,
        // shaded with mat 1, and finally backfaces)
        assertEquals(4, obj.kids.size());

        assertEquals("subnode0", obj.getChild(0).getName());
        assertEquals("unshaded0NoName-texture-a", obj.getChild(0).material);
        PortableMaterial unshaded0NoName = portableModel.findMaterial(obj.getChild(0).material);
        assertNotNull(unshaded0NoName);
        assertFalse(unshaded0NoName.isShaded());
        assertNotNull(unshaded0NoName.getTexture());
        assertEquals("texture-a.png", unshaded0NoName.getTexture());
        assertNull(unshaded0NoName.getShininess());
        assertNull(unshaded0NoName.getTransparency());

        assertEquals("subnode1", obj.getChild(1).getName());
        assertEquals("shaded1NoName-texture-a", obj.getChild(1).material);
        PortableMaterial shaded1NoName = portableModel.findMaterial("shaded1NoName-texture-a");
        assertNotNull(shaded1NoName);
        assertTrue(shaded1NoName.isShaded());
        assertNotNull(shaded1NoName.getTexture());
        assertEquals("texture-a.png", shaded1NoName.getTexture());
        assertNotNull(shaded1NoName.getShininess());
        assertEquals(0.97, shaded1NoName.getShininess().value, 0.001);
        assertNull(shaded1NoName.getTransparency());

        assertEquals("subnode2", obj.getChild(2).getName());
        assertEquals("unshaded1NoName-texture-a", obj.getChild(2).material);
        PortableMaterial unshaded1NoName = portableModel.findMaterial(obj.getChild(2).material);
        assertNotNull(unshaded1NoName);
        assertFalse(unshaded1NoName.isShaded());
        assertNotNull(unshaded1NoName.getTexture());
        assertEquals("texture-a.png", unshaded1NoName.getTexture());
        assertNotNull(unshaded1NoName.getShininess());
        assertEquals(0.97, unshaded1NoName.getShininess().value, 0.001);
        assertNull(unshaded1NoName.getTransparency());

    }
}
