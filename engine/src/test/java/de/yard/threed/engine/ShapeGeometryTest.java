package de.yard.threed.engine;

import de.yard.threed.core.Matrix4;
import de.yard.threed.core.Vector2;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.geometry.ShapeGeometry;
import de.yard.threed.engine.geometry.ShapeSurface;
import de.yard.threed.engine.testutil.PlatformFactoryHeadless;
import de.yard.threed.engine.platform.common.Face;
import de.yard.threed.engine.platform.common.Face3;
import de.yard.threed.engine.platform.common.Face3List;
import de.yard.threed.engine.platform.common.FaceN;
import de.yard.threed.engine.testutil.TestFactory;


import de.yard.threed.engine.geometry.GeometryHelper;
import de.yard.threed.engine.test.testutil.ShapedGeometryTestHelper;
import de.yard.threed.engine.test.testutil.TestUtil;
import de.yard.threed.engine.test.testutil.TinyPlane;
import de.yard.threed.engine.testutil.TestHelper;
import org.junit.Test;

import java.util.List;


/**
 * Date: 09.06.14
 */
public class ShapeGeometryTest {
    static Platform platform = TestFactory.initPlatformForTest( new String[] {"engine"}, new PlatformFactoryHeadless());

    /**
     * Punktfolge im Rectangle, aus dem Cuboid extruhiert wurde.
     * 1-2
     * | |
     * 0-3
     */
    @Test
    public void testCuboid() {
        ShapeGeometry cuboid = ShapeGeometry.buildBox(7, 5, 9, null);
        List<Vector3> vertices = cuboid.getVertices();
        // Front und Back sind ja je 2*Face3, 21.8.15:Jetzte Face4, ausser Front und Back.15.6.16:Jetzt ist front EIN FaceN
        assertFacesCount(cuboid, new int[][]{new int[]{1, 1, 1, 1}}, new int[]{1/*2*/}, new int[]{2});
        TestUtil.assertEquals("Anzahl Vertices", 8, vertices.size());
        TestUtil.assertVector3("Vertex 0", new Vector3(-3.5f, -2.5f, 4.5f), vertices.get(0));
        TestUtil.assertVector3("Vertex 1", new Vector3(-3.5f, 2.5f, 4.5f), vertices.get(1));
        TestUtil.assertVector3("Vertex 2", new Vector3(3.5f, 2.5f, 4.5f), vertices.get(2));
        TestUtil.assertVector3("Vertex 3", new Vector3(3.5f, -2.5f, 4.5f), vertices.get(3));
        TestUtil.assertVector3("Vertex 4", new Vector3(-3.5f, -2.5f, -4.5f), vertices.get(4));
        TestUtil.assertVector3("Vertex 5", new Vector3(-3.5f, 2.5f, -4.5f), vertices.get(5));
        TestUtil.assertVector3("Vertex 6", new Vector3(3.5f, 2.5f, -4.5f), vertices.get(6));
        TestUtil.assertVector3("Vertex 7", new Vector3(3.5f, -2.5f, -4.5f), vertices.get(7));
        TestUtil.assertEquals("Anzahl Tapes", 1, cuboid.getTapes(0));
        //TestUtil.assertEquals("Anzahl Segments", 4, cuboid.segmentspertape);
        TestUtil.assertEquals("Anzahl Surfaces normal", 4, cuboid.getSurfaces(0).size());
        TestUtil.assertEquals("Anzahl Surfaces front", 1, cuboid.getSurfaces(-1).size());
        TestUtil.assertEquals("Anzahl Surfaces back", 1, cuboid.getSurfaces(-2).size());
        assertGridSegmentCount(cuboid, new int[]{1, 1, 1, 1, -1, -1});
        //die erste Surface ist die senkrechte Fläche links auf x=-width/2 in der yz-Ebene.
        //
        Surface surfacelinks = cuboid.getSurfaces(0).get(0);
        List<Face> faces = surfacelinks.getFaces();
        TestUtil.assertEquals("Anzahl Face4", 1, faces.size());

        // Erstmal die 4 normalen Surfaces testen. Das ist noch am einfachsten
        // Da ist die Textur drumgewickelt. Nee, es wird nicht mehr ueber Kanten gewickelt.
        int index = 0;
        for (Surface surf : cuboid.getSurfaces(0)) {
            assertStandardUvMapping((FaceN) surf.getFaces().get(0)/*, surface.faces.get(index+1)*/);
            index += 1;
        }
        // Front liegt mit Sicht auf den Shape mit (0,0) links oben
        index = 0;
        ShapeSurface surface = (ShapeSurface) cuboid.getSurfaces(-1).get(0);
        // Die Front hat eigenen Standard fuer UV Mapping. Das fuer Back ist spiegelbildlich identisch. 15.6.16:Jetzt ist front EIN FaceN.
        // Uber triangulate testen.
        Face3List f3 = GeometryHelper.triangulate(cuboid.getVertices(), surface.getFacelist());
        //TODO uvs sind durch trinagluate anders assertStandardFrontBackUvMapping((Face3)f3.faces.get(0), (Face3)f3.faces.get(1));
        surface = (ShapeSurface) cuboid.getSurfaces(-2).get(0);
        assertStandardFrontBackUvMapping((Face3) surface.getFaces().get(0), (Face3) surface.getFaces().get(1));
    }

    /**
     * Skizze 7
     */
    @Test
    public void testTinyPlane() {
        ShapeGeometry sg = TinyPlane.buildTinyPlane();
        TestUtil.assertVector3(sg.getVertices().get(0), new Vector3(-0.5f, 0, 0.5f));
        TestUtil.assertVector3(sg.getVertices().get(1), new Vector3(0.5f, 0, 0.5f));
        TestUtil.assertVector3(sg.getVertices().get(2), new Vector3(-0.5f, 0, -0.5f));
        TestUtil.assertVector3(sg.getVertices().get(3), new Vector3(0.5f, 0, -0.5f));

        //pruefen, ob das konform zu ThreeJS isType?. Aber muss es ja nicht. Das ist so, wie ich die Geometrie erstelle.
        //MAn koennte es aber mal mit einer Plane von THReejs vergleichen. Nur interessehalber. Ach, ist doch Quatsch
        TestUtil.assertEquals("Anzahl Surfaces normal", 1, sg.getSurfaces(0).size());
        TestUtil.assertEquals("Anzahl Faces normal", 1, sg.getSurfaces(0).get(0).getFaces().size());

        FaceN face = (FaceN) sg.getSurfaces(0).get(0).getFaces().get(0);
        TestUtil.assertFace4("", new int[]{0, 1, 3, 2}, face);

        Surface surface = sg.getSurfaces(0).get(0);
        assertStandardUvMapping((FaceN) surface.getFaces().get(0));
    }

    /**
     *
     */
    @Test
    public void testSimpleEdged() {
        ShapeGeometry sg = ShapedGeometryTestHelper.buildRoof();
        // Zweimal Face4
        assertFacesCount(sg, new int[][]{new int[]{1, 1}}, null, null);
        TestUtil.assertEquals("Anzahl Vertices", 6, sg.getVertices().size());
        TestUtil.assertEquals("Anzahl Tapes", 1, sg.getTapes(0));
        TestUtil.assertEquals("Anzahl Surfaces", 2, sg.getSurfaces(0).size());
        assertGridSegmentCount(sg, new int[]{1, 1});
        // Durch die Kante haben beide Faces ein Standardmapping
        assertStandardUvMapping((FaceN) sg.getSurfaces(0).get(0).getFaces().get(0));
        assertStandardUvMapping((FaceN) sg.getSurfaces(0).get(1).getFaces().get(0));
    }

    /**
     * Wie fuer Radio
     */
    @Test
    public void testRoundedCuboid() {
        float width = 0.5f;
        float height = 0.2f;
        float depth = 0.2f;
        float radius = 0.06f;
        ShapeGeometry sg = ShapeGeometry.buildRoundedBox(width, height, depth, radius);
        int arcsteps = 10;
        // Jeweils 10 in den Rundungen, dann vier Linien und front und back
        int vertices = 2 * (4 * arcsteps + 4);
        TestUtil.assertEquals("Anzahl Vertices", vertices, sg.getVertices().size());
        // Der Shape beginnt links unten und dann CW rum
        TestUtil.assertVector3(new Vector3(-width / 2 + radius, -height / 2, depth / 2), sg.getVertices().get(0));
        // Die Kooridnaten zu Punkt eins sind per Plausi ermittelt
        TestUtil.assertVector3(new Vector3(-width / 2 + radius - 0.0093f, -height / 2, depth / 2), sg.getVertices().get(1));
        TestUtil.assertVector3(new Vector3(-width / 2, -height / 2 + radius, depth / 2), sg.getVertices().get(10));
        TestUtil.assertVector3(new Vector3(-width / 2, height / 2 - radius, depth / 2), sg.getVertices().get(11));

        // sind jetzt alles Face4. 15.6.16:Jetzt ist front EIN FaceN
        assertFacesCount(sg, new int[][]{new int[]{(4 * arcsteps + 4)}}, new int[]{1/*44 - 2*/}, new int[]{44 - 2});//TODO 29.5. duerfte noch nicht stimmen
        TestUtil.assertEquals("Anzahl Tapes", 1, sg.getTapes(0));
        //TestUtil.assertEquals("Anzahl Segments", 44, sg.segmentspertape);
        TestUtil.assertEquals("Anzahl Surfaces normal", 1, sg.getSurfaces(0).size());
        TestUtil.assertEquals("Anzahl Surfaces front", 1, sg.getSurfaces(-1).size());
        TestUtil.assertEquals("Anzahl Surfaces back", 1, sg.getSurfaces(-2).size());
        //Bei Front/Back gibt es keine Segments
        assertGridSegmentCount(sg, new int[]{44, -1, -1});

     
    }

    /**
     * Ein Rechteck entlang eines Pfads extrudieren
     * Punktfolge im Rectangle, aus dem Cuboid extruhiert wurde.
     * 1-2
     * | |
     * 0-3
     */
    @Test
    public void testRectangleExtrusion() {
        Shape shape = ShapeFactory.buildRectangle(4, 1);
        Path path = TestHelper.buildTestPath();
        // Der Halbkreis hat nur vier Steps
        ShapeGeometry sg = new ShapeGeometry(shape, path);
        // das sind dann 6 Tapes a 4 Segmente
        //ist unklar assertFacesCount(sg, new int[]{1, 1});
        TestUtil.assertEquals("Anzahl Vertices", 28, sg.getVertices().size());
        TestUtil.assertEquals("Anzahl Tapes", 1, sg.getTapes(0));
        TestUtil.assertEquals("Anzahl Tapes", 4, sg.getTapes(1));
        TestUtil.assertEquals("Anzahl Tapes", 1, sg.getTapes(2));
        // Der Pfad besteht aus drei Segmenten. Der Bogen besteht unabhaengig von der Aufteilung
        // aus 4 Surfaces. (5.12.16: Nicht Tapes? Ja, das auch, aber 4 Surfaces wegen des Rectangle)
        // und das sind 4 Surfaces, weil es entlang der Extrusion keine Kanten gibt.
        TestUtil.assertEquals("Anzahl Surfaces", /*3 **/ 4, sg.getSurfaces(0).size());

        List<Vector3> vertices = sg.getVertices();
        TestUtil.assertVector3("Vertex 0", new Vector3(-2f, -0.5f, 0f), vertices.get(0));
        /*5.12.16: die Wete stimmen nicht TestUtil.assertVector3("Vertex 1", new Vector3(-3.5f, 2.5f, 4.5f), vertices.get(1));
        TestUtil.assertVector3("Vertex 2", new Vector3(3.5f, 2.5f, 4.5f), vertices.get(2));
        TestUtil.assertVector3("Vertex 3", new Vector3(3.5f, -2.5f, 4.5f), vertices.get(3));
        TestUtil.assertVector3("Vertex 4", new Vector3(-3.5f, -2.5f, -4.5f), vertices.get(4));
        TestUtil.assertVector3("Vertex 5", new Vector3(-3.5f, 2.5f, -4.5f), vertices.get(5));
        TestUtil.assertVector3("Vertex 6", new Vector3(3.5f, 2.5f, -4.5f), vertices.get(6));
        TestUtil.assertVector3("Vertex 7", new Vector3(3.5f, -2.5f, -4.5f), vertices.get(7));*/

        //auch unklar assertGridSegmentCount(sg, new int[]{1, 1});
        //sg.
        //die erste Surface ist die senkrechte Fläche links auf x=-width/2 in der yz-Ebene. Die fünfte ist dann die dahinter
        //JEDE Surface hat zwei Faces. 5.12.16: Es sind doch 4 Surfaces, die sich an der Extrusion entlangziehen.
        //TODO das mit der surfaceliste muss doch nochmal geklärt werden.
        /*for (int i = 0; i < /*3 ** / 4; i++) {
            Surface surface = sg.getSurfaces(0).get(i);
            TestUtil.assertEquals("Anzahl Faces in Surface " + i, 1+4+1, surface.getFaces().size());
        }
        Surface surface4 = sg.getSurfaces(0).get(4);
        TestUtil.assertEquals("Anzahl Faces in Surface 4", 2, surface4.getFaces().size());*/
    }

    /**
     * rough Kugel vertikal 4 und horizontal 16 Segmenten
     * Der Durchmesser ist 2.
     */
    @Test
    public void testRoughShere() {
        ShapeGeometry sg = ShapedGeometryTestHelper.buildRoughShere();
        // das sind dann 16 Tapes a 4 Segmente
        int tapes = 16;
        int segments = 4;
        // 02.12.16: closed geo gibts nicht mehr. Darum ein Satz Vertices mehr
        TestUtil.assertEquals("Anzahl Vertices", (16 + 1) * 5, sg.getVertices().size());
        TestUtil.assertEquals("Anzahl Surfaces", 1, sg.getSurfaces(0).size());
        GridSurface surface = (GridSurface) sg.getSurfaces(0).get(0);
        TestUtil.assertEquals("Anzahl Tapes", 16, surface.tapes);
        TestUtil.assertEquals("Anzahl Segments", 4, surface.segmentlength.size());
        TestUtil.assertEquals("Anzahl Faces", 16 * 4, surface.getFaces().size());

        List<Vector3> vertices = sg.getVertices();
        TestUtil.assertVector3("Vertex 0", new Vector3(0, 1f, 0), vertices.get(0));
        TestUtil.assertVector3("Vertex 2", new Vector3(1, 0, 0), vertices.get(2));
        TestUtil.assertVector3("Vertex 4", new Vector3(0, -1f, 0), vertices.get(4));
        TestUtil.assertVector3("Vertex 5+0", new Vector3(0, 1f, 0), vertices.get(5 + 0));
        TestUtil.assertVector3("Vertex 5+4", new Vector3(0, -1f, 0), vertices.get(5 + 4));
        // Element nach links
        TestUtil.assertVector3("Vertex 40+0", new Vector3(0, 1f, 0), vertices.get(40 + 0));
        TestUtil.assertVector3("Vertex 40+2", new Vector3(-1, 0, 0), vertices.get(40 + 2));
        TestUtil.assertVector3("Vertex 45+0", new Vector3(0, 1f, 0), vertices.get(45 + 0));

        // Das letzte Tape ist nicht so generisch zu testen, weil es ja schliesst
        // 02.11.16: closed geo gibts nicht mehr. Darum doch generisch
        for (int t = 0; t < tapes /*- 1*/; t++) {
            int toff = t * (segments + 1);
            for (int s = 0; s < segments; s++) {
                FaceN face = (FaceN) sg.getSurfaces(0).get(0).getFaces().get(t * segments + s);
                TestUtil.assertFace4("tape " + t + ",segment " + s, new int[]{toff + s + 0, toff + s + 1, toff + s + segments + 2, toff + s + segments + 1}, face);
            }
        }
        // letztes Tape
        /*int toffl = (tapes - 1) * (segments + 1);
        for (int s = 0; s < segments; s++) {
            FaceN face = (FaceN) sg.getSurfaces(0).get(0).getFaces().get((tapes - 1) * segments + s);
            TestUtil.assertFace4("closing tape, segment " + s, new int[]{toffl + s, toffl + s + 1, s + 1, s}, face);
        }*/

        assertSegmentedUvMapping((FaceN) surface.getFaces().get(0), 0, 16, 0, 4);
        // Die Faces laufen die Tapes entlang
        assertSegmentedUvMapping((FaceN) surface.getFaces().get(1), 0, 16, 1, 4);
        assertSegmentedUvMapping((FaceN) surface.getFaces().get(3), 0, 16, 3, 4);
        assertSegmentedUvMapping((FaceN) surface.getFaces().get(4), 1, 16, 0, 4);
        // Dann duerften die restlichen auch stimmen. Jetzt noch das schliessende Tape
        assertSegmentedUvMapping((FaceN) surface.getFaces().get(16 * 4 - 4), 15, 16, 0, 4);
        assertSegmentedUvMapping((FaceN) surface.getFaces().get(16 * 4 - 3), 15, 16, 1, 4);
        assertSegmentedUvMapping((FaceN) surface.getFaces().get(16 * 4 - 1), 15, 16, 3, 4);
    }

    /**
     * Die Hoehe ist 2.
     */
    @Test
    public void testVerticalTube() {
        ShapeGeometry sg = ShapeGeometry.buildVerticalTube(8);
        // das sind dann 16 Tapes a 4 Segmente
        // 02.12.16: closed geo gibts nicht mehr. Darum ein Satz Vertices mehr
        TestUtil.assertEquals("Anzahl Vertices", (8 + 1) * 2, sg.getVertices().size());
        TestUtil.assertEquals("Anzahl Surfaces", 1, sg.getSurfaces(0).size());
        GridSurface surface = (GridSurface) sg.getSurfaces(0).get(0);
        TestUtil.assertEquals("Anzahl Tapes", 8, surface.tapes);
        TestUtil.assertEquals("Anzahl Segments", 1, surface.segmentlength.size());
        TestUtil.assertEquals("Anzahl Faces", 8 * 1, surface.getFaces().size());

        List<Vector3> vertices = sg.getVertices();
        TestUtil.assertVector3("Vertex 0", new Vector3(1, -1, 0), vertices.get(0));
        TestUtil.assertVector3("Vertex 1", new Vector3(1, 1, 0), vertices.get(1));
        TestUtil.assertVector3("Vertex 2", new Vector3(0.70710677f, -1, -0.70710677f), vertices.get(2));
        TestUtil.assertVector3("Vertex 3", new Vector3(0.70710677f, 1, -0.70710677f), vertices.get(3));
        TestUtil.assertVector3("Vertex 4", new Vector3(0, -1, -1), vertices.get(4));
        TestUtil.assertVector3("Vertex 5", new Vector3(0, 1, -1), vertices.get(5));
        TestUtil.assertVector3("Vertex 6", new Vector3(-0.70710677f, -1, -0.70710677f), vertices.get(6));
        TestUtil.assertVector3("Vertex 7", new Vector3(-0.70710677f, 1, -0.70710677f), vertices.get(7));
        TestUtil.assertVector3("Vertex 8", new Vector3(-1, -1, 0), vertices.get(8));
        TestUtil.assertVector3("Vertex 9", new Vector3(-1, 1, 0), vertices.get(9));
        TestUtil.assertVector3("Vertex 10", new Vector3(-0.70710677f, -1, +0.70710677f), vertices.get(10));
        TestUtil.assertVector3("Vertex 11", new Vector3(-0.70710677f, 1, +0.70710677f), vertices.get(11));
        TestUtil.assertVector3("Vertex 12", new Vector3(0, -1, 1), vertices.get(12));
        TestUtil.assertVector3("Vertex 13", new Vector3(0, 1, 1), vertices.get(13));
        TestUtil.assertVector3("Vertex 14", new Vector3(0.70710677f, -1, 0.70710677f), vertices.get(14));
        TestUtil.assertVector3("Vertex 15", new Vector3(0.70710677f, 1, 0.70710677f), vertices.get(15));
    }

    @Test
    public void testHorizontalDisc() {
        testHorizontalDisc(4);
        testHorizontalDisc(8);
        testHorizontalDisc(32);
    }

    private void testHorizontalDisc(int hsegments) {
        ShapeGeometry sg = ShapeGeometry.buildHorizontalDisc(hsegments);
        // das sind dann hsegments Tapes a 1 Segmente
        // 02.12.16: closed geo gibts nicht mehr. Darum ein Satz Vertices mehr
        TestUtil.assertEquals("Anzahl Vertices", (hsegments + 1) * 2, sg.getVertices().size());
        TestUtil.assertEquals("Anzahl Surfaces", 1, sg.getSurfaces(0).size());
        GridSurface surface = (GridSurface) sg.getSurfaces(0).get(0);
        TestUtil.assertEquals("Anzahl Tapes", hsegments, surface.tapes);
        TestUtil.assertEquals("Anzahl Segments", 1, surface.segmentlength.size());
        TestUtil.assertEquals("Anzahl Faces", hsegments * 1, surface.getFaces().size());

        float innerradius = 0.5f;
        float outerradius = 2.5f;
        List<Vector3> vertices = sg.getVertices();
        TestUtil.assertVector3("Vertex 0", new Vector3(innerradius, 0, 0), vertices.get(0));
        TestUtil.assertVector3("Vertex 1", new Vector3(outerradius, 0, 0), vertices.get(1));
        //TestUtil.assertVector3("Vertex 2", new Vector3(0.70710677f, -1, -0.70710677f), vertices.get(2));
        //TestUtil.assertVector3("Vertex 3", new Vector3(0.70710677f, 1, -0.70710677f), vertices.get(3));
        TestUtil.assertVector3("Vertex nach hinten", new Vector3(0, 0, -innerradius), vertices.get(hsegments / 2));
        TestUtil.assertVector3("Vertex nach hinten+1", new Vector3(0, 0, -outerradius), vertices.get(hsegments / 2 + 1));
        //TestUtil.assertVector3("Vertex 6", new Vector3(-0.70710677f, -1, -0.70710677f), vertices.get(6));
        //TestUtil.assertVector3("Vertex 7", new Vector3(-0.70710677f, 1, -0.70710677f), vertices.get(7));
        TestUtil.assertVector3("Vertex 8", new Vector3(-innerradius, 0, 0), vertices.get(hsegments));
        TestUtil.assertVector3("Vertex 9", new Vector3(-outerradius, 0, 0), vertices.get(hsegments + 1));
        //TestUtil.assertVector3("Vertex 10", new Vector3(-0.70710677f, -1, +0.70710677f), vertices.get(10));
        //TestUtil.assertVector3("Vertex 11", new Vector3(-0.70710677f, 1, +0.70710677f), vertices.get(11));
        TestUtil.assertVector3("Vertex nach vorne", new Vector3(0, 0, innerradius), vertices.get(hsegments + hsegments / 2));
        TestUtil.assertVector3("Vertex nach vorne", new Vector3(0, 0, outerradius), vertices.get(hsegments + hsegments / 2 + 1));
        //TestUtil.assertVector3("Vertex 14", new Vector3(0.70710677f, -1, 0.70710677f), vertices.get(14));
        //TestUtil.assertVector3("Vertex 15", new Vector3(0.70710677f, 1, 0.70710677f), vertices.get(15));
    }

    /**
     * Über Polygon (mit Ecken) extrudiert.
     */
    @Test
    public void testHorizontalPolygon() {
        testHorizontalPolygon(4);
    }

    /**
     * Über Polygon (mit Ecken) extrudiert.
     */
    private void testHorizontalPolygon(int steps) {
        Shape shape = new Shape(false);
        shape.addPoint(new Vector2(0.5f, 0));
        shape.addPoint(new Vector2(2.5f, 0));

        Vector3 origin = new Vector3(0, 0, 0);
        SegmentedPath path = new SegmentedPath(origin);
        // einfach mal nach hinten
        for (int i = 0; i < steps; i++) {
            Vector3 destination = new Vector3(0, 0, i + 1);
            path.addLine(destination);
        }
        ShapeGeometry sg = new ShapeGeometry(shape, path);
        // das sind dann hsegments Tapes a 1 Segmente
        TestUtil.assertEquals("Anzahl Vertices", (steps + 1) * 2, sg.getVertices().size());
        //assertEquals("Anzahl Surfaces", 1, sg.getSurfaces(0).size());
        //GridSurface surface = (GridSurface) sg.getSurfaces(0).get(0);
        //assertEquals("Anzahl Tapes", steps, surface.tapes);
        //assertEquals("Anzahl Segments", 1, surface.segmentlength.size());
        //assertEquals("Anzahl Faces", steps * 1, surface.getFaceLists().size());

        float innerradius = 0.5f;
        float outerradius = 2.5f;
        List<Vector3> vertices = sg.getVertices();
        //TestUtil.assertVector3("Vertex 0", new Vector3(innerradius, 0, 0), vertices.get(0));
        //TestUtil.assertVector3("Vertex 1", new Vector3(outerradius, 0, 0), vertices.get(1));

    }

    /**
     * Eine ellipsenförmige Extrusion, um den Punkt (1,0,0) um 90 Grad auf (0,0,-2)
     * zu verschieben und entsprechend zu rotieren.
     * Sowas ist Teil der Shape Extrusion.
     */
    @Test
    public void testSimpleExtrusion() {
        Vector3 extrudedirection = new Vector3(0, 0, -1);
        Vector3 source = new Vector3(1, 0, 0);
        Vector3 destination = new Vector3(0, 0, -2);
        Vector3 pathtangent = new Vector3(-1, 0, 0);

        Matrix4 transformmatrix = SegmentedPath.buildTransformationMatrix(destination, extrudedirection, pathtangent, source);

        System.out.println("transformmatrix=" + transformmatrix.dump("\n"));
        Vector3 transformedsource = transformmatrix.transform(source);
        TestUtil.assertVector3("transformedsource", destination, transformedsource);
    }

    /**
     * Skizze 7
     * <p>
     * Textur um 90 Grad nach links verdreht.
     */
    @Test
    public void testSimpleRotatedTextured() {
        ShapeGeometry sg = TinyPlane.buildTinyPlane(UvMap1.leftRotatedTexture);
        TestUtil.assertVector3(sg.getVertices().get(0), new Vector3(-0.5f, 0, 0.5f));
        TestUtil.assertVector3(sg.getVertices().get(1), new Vector3(0.5f, 0, 0.5f));
        TestUtil.assertVector3(sg.getVertices().get(2), new Vector3(-0.5f, 0, -0.5f));
        TestUtil.assertVector3(sg.getVertices().get(3), new Vector3(0.5f, 0, -0.5f));

        TestUtil.assertEquals("Anzahl Surfaces normal", 1, sg.getSurfaces(0).size());
        TestUtil.assertEquals("Anzahl Faces normal", 1, sg.getSurfaces(0).get(0).getFaces().size());

        FaceN face = (FaceN) sg.getSurfaces(0).get(0).getFaces().get(0);
        TestUtil.assertFace4("", new int[]{0, 1, 3, 2}, face);

        Surface surface = sg.getSurfaces(0).get(0);
        TestUtil.assertST("uv0 ", face.uv[0], new Vector2(1, 1));
        TestUtil.assertST("uv1 ", face.uv[1], new Vector2(0, 1));
        TestUtil.assertST("uv2 ", face.uv[2], new Vector2(0.0f, 0.0f));
        TestUtil.assertST("uv3 ", face.uv[3], new Vector2(1.0f, 0));
    }

    @Test
    public void testPlane5x8() {
        ShapeGeometry sg = ShapeGeometry.buildPlane(9, 12, 5, 8);
        TestUtil.assertEquals("Anzahl Vertices", (5 + 1) * (8 + 1), sg.getVertices().size());
        TestUtil.assertEquals("Anzahl Surfaces normal", 1, sg.getSurfaces(0).size());
        TestUtil.assertEquals("Anzahl Faces normal", 5 * 8, sg.getSurfaces(0).get(0).getFaces().size());

        TestUtil.assertVector3(sg.getVertices().get(0), new Vector3(-4.5f, 0, 6f));
        TestUtil.assertVector3(sg.getVertices().get(1), new Vector3(-4.5f + 9f / 5f, 0, 6f));
        TestUtil.assertVector3(sg.getVertices().get(2), new Vector3(-4.5f + 2 * 9f / 5f, 0, 6f));
        TestUtil.assertVector3(sg.getVertices().get((5 + 1) * (8 + 1) - 1), new Vector3(4.5f, 0, -6f));

        TestUtil.assertVector3(sg.getVertices().get(5 + 1), new Vector3(-4.5f, 0, 6f - 12f / 8f));
    }

    public static void assertFacesCount(ShapeGeometry sg, int[][] facesexpected, int[] facesexpectedfront, int[] facesexpectedback) {
        for (int i = 0; i < facesexpected.length; i++) {
            TestUtil.assertEquals("assertFacesCount: Anzahl Surfaces", facesexpected[i].length, sg.getSurfaces(i).size());
            for (int j = 0; j < facesexpected[i].length; j++) {
                TestUtil.assertEquals("assertFacesCount: Anzahl Faces in Surface " + i + "/" + j, facesexpected[i][j], sg.getSurfaces(i).get(j).getFaces().size());
            }
        }
        if (facesexpectedfront != null) {
            for (int j = 0; j < facesexpectedfront.length; j++) {
                TestUtil.assertEquals("assertFacesCount: Anzahl Faces in Surface -1/" + j, facesexpectedfront[j], sg.getSurfaces(-1).get(j).getFaces().size());
            }
        }
        if (facesexpectedback != null) {
            for (int j = 0; j < facesexpectedback.length; j++) {
                TestUtil.assertEquals("assertFacesCount: Anzahl Faces in Surface -1/" + j, facesexpectedback[j], sg.getSurfaces(-2).get(j).getFaces().size());
            }
        }
    }

    public static void assertGridSegmentCount(ShapeGeometry sg, int[] segmentsexpected) {
        for (int i = 0; i < sg.getSurfaces(0).size(); i++) {
            Surface surface = sg.getSurfaces(0).get(i);
            if (surface instanceof GridSurface)
                TestUtil.assertEquals("segments in surface " + i, segmentsexpected[i], ((GridSurface) surface)./*segmentspertape*/segmentlength.size());
        }
    }

    /**
     * Die ShapeGeometry arbeitet teilweise mit Face4 und Face3...
     */
    private void assertStandardUvMapping(FaceN face/*pf1, Face pf2*/) {
        TestUtil.assertST("uv0 ", face.uv[0], new Vector2(0, 1));
        TestUtil.assertST("uv1 ", face.uv[1], new Vector2(0, 0));
        TestUtil.assertST("uv2 ", face.uv[2], new Vector2(1.0f, 0.0f));
        TestUtil.assertST("uv3 ", face.uv[3], new Vector2(1.0f, 1));
        // zum testen der Tests auch noch mit der universellen Methode
        assertSegmentedUvMapping(face, 0, 1, 0, 1);
    }

    private void assertStandardUvMapping(Face3 f1, Face3 f2) {
        TestUtil.assertST("uv0 ", f1.uv[0], new Vector2(0, 1));
        TestUtil.assertST("uv1 ", f1.uv[1], new Vector2(0, 0));
        TestUtil.assertST("uv3 ", f1.uv[2], new Vector2(1.0f, 1));
        TestUtil.assertST("uv0 ", f2.uv[0], new Vector2(1, 0));
        TestUtil.assertST("uv1 ", f2.uv[1], new Vector2(1.0f, 1));
        TestUtil.assertST("uv3 ", f2.uv[2], new Vector2(0, 0));
    }

    /**
     * Geht erstmal nur fuer einfache Fronten wie z.B. Wuerfel
     * Front und Back sind identisch
     * 24.11.15: Die y-Werte korrigiert, weil y=0 ja unten ist.
     *
     * @param f1
     * @param f2
     */
    private void assertStandardFrontBackUvMapping(Face3 f1, Face3 f2) {
        TestUtil.assertST("0:uv0 ", f1.uv[0], new Vector2(0, 0));
        TestUtil.assertST("0:uv1 ", f1.uv[1], new Vector2(1, 0f));
        TestUtil.assertST("0:uv2 ", f1.uv[2], new Vector2(1.0f, 1));
        TestUtil.assertST("1:uv0 ", f2.uv[0], new Vector2(0, 0));
        TestUtil.assertST("1:uv1 ", f2.uv[1], new Vector2(1.0f, 1));
        TestUtil.assertST("1:uv2 ", f2.uv[2], new Vector2(0, 1));
    }

    private void assertSegmentedUvMapping(FaceN face, int xpos, int xsegments, int ypos, int ysegments) {
        float xsegwidth = 1f / xsegments;
        float ysegwidth = 1f / ysegments;

        TestUtil.assertST("uv0 ", face.uv[0], new Vector2((xpos + 0) * xsegwidth, 1 - (ypos + 0) * ysegwidth));
        TestUtil.assertST("uv1 ", face.uv[1], new Vector2((xpos + 0) * xsegwidth, 1 - (ypos + 1) * ysegwidth));
        TestUtil.assertST("uv2 ", face.uv[2], new Vector2((xpos + 1) * xsegwidth, 1 - (ypos + 1) * ysegwidth));
        TestUtil.assertST("uv3 ", face.uv[3], new Vector2((xpos + 1) * xsegwidth, 1 - (ypos + 0) * ysegwidth));
    }

    /*21.08.15 TODO private Vector2 getStFromVbo(GlSimulator sim, int row) {
        float s = sim.getVertexAttrib(2, 0)[row];
        float t = sim.getVertexAttrib(2, 1)[row];
        Vector2 st = new Vector2(s, t);
        return st;
    }*/
}
