package de.yard.threed.engine;


import de.yard.threed.core.*;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.apps.WoodenToyFactory;
import de.yard.threed.engine.geometry.ShapeGeometry;
import de.yard.threed.engine.geometry.Primitives;


import de.yard.threed.engine.loader.*;


import de.yard.threed.engine.platform.common.Face;
import de.yard.threed.engine.platform.common.Face3;
import de.yard.threed.engine.platform.common.Face3List;
import de.yard.threed.engine.platform.common.FaceList;
import de.yard.threed.engine.geometry.GeometryHelper;
import de.yard.threed.engine.platform.common.SimpleGeometry;
import de.yard.threed.engine.platform.common.SmartArrayList;
import de.yard.threed.engine.geometry.SmoothingHelper;
import de.yard.threed.engine.geometry.SmoothingMap;

import de.yard.threed.engine.platform.common.StringReader;
import de.yard.threed.engine.testutil.PlatformFactoryHeadless;
import de.yard.threed.engine.testutil.TestFactory;
import de.yard.threed.engine.test.testutil.TestUtil;
import org.junit.Test;

import java.util.List;


/**
 * Auch fuer (Composed)Primitive.
 * 
 * Created by thomass on 08.02.16.
 */
public class GeometryHelperTest {
    static Platform platform = TestFactory.initPlatformForTest( new String[] {"engine"}, new PlatformFactoryHeadless());

    /**
     * Skizze 19 (so ähnlich)
     */
    @Test
    public void testSphere() {
        int wsegs = 8;
        int hsegs = 4;
        SimpleGeometry sphere = Primitives.buildSphereGeometry(7, wsegs, hsegs);
        Vector3Array vertices = sphere.getVertices();
        //List<Face> faces = sphere.getFaces().faces;
        TestUtil.assertEquals("Anzahl Vertices", (hsegs + 1) * (wsegs + 1), vertices.size());
        TestUtil.assertVector3("Vertex 0", new Vector3(0, 7, 0), vertices.getElement(0));
        TestUtil.assertVector3("Vertex 2", new Vector3(7, 0, 0), vertices.getElement(2));
        TestUtil.assertVector3("Vertex 5", new Vector3(0, 7, 0), vertices.getElement(5));

        TestUtil.assertVector3("Vertex 0 back", new Vector3(0, 7, 0), vertices.getElement((hsegs + 1) * (wsegs / 4) + 0));
        TestUtil.assertVector3("Vertex 2 back", new Vector3(0, 0, -7), vertices.getElement((hsegs + 1) * (wsegs / 4) + 2));
        TestUtil.assertVector3("Vertex 5 back", new Vector3(0, 7, 0), vertices.getElement((hsegs + 1) * (wsegs / 4) + 5));

        TestUtil.assertVector3("Vertex 0 opp", new Vector3(0, 7, 0), vertices.getElement((hsegs + 1) * (wsegs / 2) + 0));
        TestUtil.assertVector3("Vertex 2 opp", new Vector3(-7, 0, 0), vertices.getElement((hsegs + 1) * (wsegs / 2) + 2));
        TestUtil.assertVector3("Vertex 5 opp", new Vector3(0, 7, 0), vertices.getElement((hsegs + 1) * (wsegs / 2) + 5));

        TestUtil.assertEquals("Anzahl Indizes", (((hsegs-2)*2 + 2) * (wsegs ))*3, sphere.getIndices().length);

        Vector2Array uvs = sphere.getUvs();
        TestUtil.assertFace3("face 0", new int[]{0, 1, 6}, sphere.getIndices(), 0);
        TestUtil.assertST("0:uv0 ", new Vector2(0, 1), uvs.getElement(0));
        TestUtil.assertFace3("face 1", new int[]{1, 2, 6}, sphere.getIndices(), 1);
        TestUtil.assertST("0:uv0 ", new Vector2(0, 0.75f), uvs.getElement(1));
        //Suedpol Triangle
        TestUtil.assertFace3("face 0", new int[]{3, 4, 8}, sphere.getIndices(), 5);
        //naechster Bogen
        TestUtil.assertFace3("face 6", new int[]{hsegs + 1 + 0, hsegs + 1 + 1, hsegs + 1 + 6}, sphere.getIndices(), 6);
        TestUtil.assertST("1:uv0 ", new Vector2(0.125f, 1), uvs.getElement(5));

    }

    /**
     * Eine 8el Kugel "oben vorne links"
     *
     * Skizze 19a
     */
    @Test
    public void testSphere8() {
        int wsegs = 4;
        int hsegs = 3;
        SimpleGeometry sphere = Primitives.buildSphereGeometry(7, wsegs,MathUtil2.PI, MathUtil2.PI_2, hsegs,0, MathUtil2.PI_2);
        Vector3Array vertices = sphere.getVertices();
        TestUtil.assertEquals("Anzahl Vertices", (hsegs + 1) * (wsegs + 1), vertices.size());
        TestUtil.assertVector3("Vertex 0", new Vector3(0, 7, 0), vertices.getElement(0));
        TestUtil.assertVector3("Vertex 3", new Vector3(-7, 0, 0), vertices.getElement(3));
        TestUtil.assertVector3("Vertex 19", new Vector3(0, 0, 7), vertices.getElement(19));

        Vector2Array uvs = sphere.getUvs();
        TestUtil.assertFace3("face 0", new int[]{0, 1, 5}, sphere.getIndices(), 0);
        TestUtil.assertST("0:uv0 ", new Vector2(0.5f, 1), uvs.getElement(0));
        TestUtil.assertST("0:uv3 ", new Vector2(0.5f, 0.5f), uvs.getElement(3));
        TestUtil.assertST(":uv8 ", new Vector2(0.625f, 1), uvs.getElement(8));
        TestUtil.assertFace3("face 1", new int[]{1, 2, 5}, sphere.getIndices(), 1);
        TestUtil.assertST("4:uv0 ", new Vector2(0.75f, 1), uvs.getElement(16));
        TestUtil.assertST("4:uv0 ", new Vector2(0.75f, 0.8333f), uvs.getElement(17));
        TestUtil.assertST("4:uv19 ", new Vector2(0.75f, 0.5f), uvs.getElement(19));

    }

    @Test
    public void testPlane() {
        int wsegs = 6;
        int hsegs = 8;
        SimpleGeometry plane = Primitives.buildPlaneGeometry(2, 4, wsegs, hsegs);
        TestUtil.assertEquals("Anzahl Vertices", (hsegs + 1) * (wsegs + 1), plane.getVertices().size());

        TestUtil.assertVector3("Vertex 0", new Vector3(-1, 0, 2), plane.getVertices().getElement(0));
        TestUtil.assertVector3("Vertex 3", new Vector3(0, 0, 2), plane.getVertices().getElement(3));
        TestUtil.assertST("uvl ", new Vector2(1, 0), plane.getUvs().getElement((hsegs + 1) * (wsegs + 1)-1));
    }

    @Test
    public void testCylinder() {
        int segs = 8;
        float height = 4;
        float h2 = height/2;
        SimpleGeometry cylinder = Primitives.buildCylinderGeometry(2, 3, height, segs,0,MathUtil2.PI2);
        Vector3Array vertices = cylinder.getVertices();
        //List<Face> faces = cylinder.getFaces().faces;
        TestUtil.assertEquals("Anzahl Vertices", (segs + 1) * 2, vertices.size());
        TestUtil.assertVector3("Vertex 0", new Vector3(2f, height / 2, 0), vertices.getElement(0));
        TestUtil.assertVector3("Vertex 1", new Vector3(3f, -height / 2, 0), vertices.getElement(1));
        TestUtil.assertVector3("Vertex 4", new Vector3(0, h2, -2), vertices.getElement(4));
        TestUtil.assertVector3("Vertex 5", new Vector3(0, -h2, -3), vertices.getElement(5));

        Vector2Array uvs = cylinder.getUvs();
        TestUtil.assertFace3("face 0", new int[]{0, 1, 2}, cylinder.getIndices(), 0);
        TestUtil.assertFace3("face 1", new int[]{1, 3,2}, cylinder.getIndices(), 1);
        TestUtil.assertST("0:uv0 ", new Vector2(0, 1), uvs.getElement(0));
        TestUtil.assertST("0:uv1 ", new Vector2(0, 0), uvs.getElement(1));
        TestUtil.assertST("0:uv8 ", new Vector2(1f/segs, 1), uvs.getElement(2));
        TestUtil.assertFace3("face 2", new int[]{2, 3, 4}, cylinder.getIndices(), 2);
    }

    /**
     * Viertelcylinder "vorne rechts"
     */
    @Test
    public void testCylinder4() {
        int segs = 8;
        float height = 4;
        float h2 = height/2;
        SimpleGeometry cylinder = Primitives.buildCylinderGeometry(2f,3f,height, segs,MathUtil2.PI+MathUtil2.PI_2,MathUtil2.PI_2);

        Vector3Array vertices = cylinder.getVertices();
        //List<Face> faces = cylinder.getFaces().faces;
        TestUtil.assertEquals("Anzahl Vertices", (segs + 1) * 2, vertices.size());
        TestUtil.assertVector3("Vertex 0", new Vector3(0, height / 2, 2), vertices.getElement(0));
        TestUtil.assertVector3("Vertex 1", new Vector3(0, -height / 2, 3), vertices.getElement(1));
        TestUtil.assertVector3("Vertex 4", new Vector3(2, h2, 0), vertices.getElement(16));
        TestUtil.assertVector3("Vertex 5", new Vector3(3, -h2, 0), vertices.getElement(17));

        Vector2Array uvs = cylinder.getUvs();
        TestUtil.assertFace3("face 0", new int[]{0, 1, 2}, cylinder.getIndices(), 0);
        TestUtil.assertFace3("face 1", new int[]{1, 3,2}, cylinder.getIndices(), 1);
        TestUtil.assertST("0:uv0 ", new Vector2(0.75f, 1), uvs.getElement(0));
        TestUtil.assertST("0:uv1 ", new Vector2(0.75f, 0), uvs.getElement(1));
        TestUtil.assertST("0:uv8 ", new Vector2(0.75f+1f/segs/4, 1), uvs.getElement(2));
        TestUtil.assertFace3("face 2", new int[]{2, 3, 4}, cylinder.getIndices(), 2);
    }

    /**
     * Skizze 9
     */
    @Test
    public void testTriangulateAndNormalBuilding() {
        LoaderAC ac;
        try {
            ac = new LoaderAC(new StringReader(LoaderAC.lndspots),false);
        } catch (Exception e) {
            throw new RuntimeException("Error opening or reading ac file", e);
        }

        // LHND ist twosided unshaded. Das erste FaceN "zeigt nach unten", das zweite (0,1,2,3) "nach oben".
        LoadedObject lhnd = ac.loadedfile.objects.get(0).kids.get(0);
        TestUtil.assertEquals("vertices", 4, lhnd.vertices.size());

        Face3List f = GeometryHelper.triangulate(lhnd.vertices,lhnd.faces.get(0));
        TestUtil.assertEquals("vertices", 4, lhnd.vertices.size());

        // Jetzt gibt es Face3, die ersten zwei nach unten, die anderen nach oben
        TestUtil.assertFace3("face3 0", new int[]{3, 2, 1}, (Face3) f.faces.get(0));
        TestUtil.assertFace3("face3 1", new int[]{3, 1, 0}, (Face3) f.faces.get(1));
        TestUtil.assertFace3("face3 2", new int[]{0, 1, 2}, (Face3) f.faces.get(2));
        TestUtil.assertFace3("face3 3", new int[]{0, 2, 3}, (Face3) f.faces.get(3));
        
        GeometryHelper.calculateMissingFaceNormals(lhnd.vertices, new SmartArrayList<Face3List>(f));
        //List<Vector3> normallist = GeometryHelper.calculateSmoothVertexNormals(lhnd.vertices, new SmartArrayList<Face3List>(f), new Degree(90));
        SmoothingMap smoothingMap = SmoothingHelper.buildStandardSmoothingMap(lhnd.vertices, new SmartArrayList<Face3List>(f));
        smoothingMap.applyCrease(lhnd.vertices, new SmartArrayList<Face3List>(f), new Degree(90));
        List<Vector3> normallist = GeometryHelper.calculateSmoothVertexNormals(lhnd.vertices, new SmartArrayList<Face3List>(f), smoothingMap);
        
        // durch die verschiedenen Ausrichtungen wurden die Vertices dupliziert
        TestUtil.assertEquals("vertices", 8, lhnd.vertices.size());
        TestUtil.assertEquals("normals", 8, normallist.size());
        //TODO faces zu neuen Vertices testen
    }





    @Test
    public void testPrepareGeometryWithEdges() {
        ShapeGeometry cg = ShapeGeometry.buildBox(1, 1, 1, null);
        // Es wird nicht gesplittet, weil es nur ein Material gibt. Aber Vertexduplizierung muss gemacht werden.
        List<SimpleGeometry> geos = GeometryHelper.prepareGeometry(cg.getVertices(), cg.getFaceLists(), null, false, cg.hasedges);
        TestUtil.assertEquals("nur eine Geo, kein split", 1, geos.size());
        SimpleGeometry geo = geos.get(0);

        TestUtil.assertEquals("vertices", 24, geo.getVertices().size());
        TestUtil.assertEquals("indices", 12 * 3, geo.getIndices().length);
        // Fuer den shapeAnteil kann man die UVs generisch testen. 5.12.16: Seit Änderung der Duplizierung aber nur noch das erste Face. Könnte aber doch irgendwie gehen TODO
        for (int i = 0; i < 1; i++) {
            TestUtil.assertST("uv0 "+i, new Vector2(0, 1), geo.getUvs().getElement(i * 4));
            TestUtil.assertST("uv1 "+i, new Vector2(0, 0), geo.getUvs().getElement(i * 4 + 1));
            TestUtil.assertST("uv2 "+i, new Vector2(1, 0), geo.getUvs().getElement(i * 4 + 5));
            TestUtil.assertST("uv3 "+i, new Vector2(1, 1), geo.getUvs().getElement(i * 4 + 4));
        }
        // Front
        TestUtil.assertST("front.uv0", new Vector2(1, 0), geo.getUvs().getElement(5 * 4));
        TestUtil.assertST("front.uv1", new Vector2(1, 1), geo.getUvs().getElement(5 * 4 + 1));
        TestUtil.assertST("front.uv2", new Vector2(0, 1), geo.getUvs().getElement(5 * 4 + 2));
        TestUtil.assertST("front.uv3", new Vector2(0, 0), geo.getUvs().getElement(5 * 4 + 3));
        // Back ist noch nicht eindeutig definiert. Oder?
    }

    @Test
    public void testWheel() {
        testWheel(4);         
        testWheel(64);
    }
    
        /**
         * Skizze 11
         */
    private void testWheel(int segments) {
        float radius = 2.5f;
        Shape shape = new WoodenToyFactory().buildWheelShape(0,radius,0.25f);
        ShapeGeometry sg = ShapeGeometry.buildByCircleRotation(shape, /*0.01f,*/ segments);
        TestUtil.assertEquals("Anzahl Vertices", (segments + 1) * shape.getPoints().size(), sg.getVertices().size());
        TestUtil.assertEquals("Anzahl Surfaces", 1, sg.getSurfacesCount());
        TestUtil.assertEquals("Anzahl Surfaces normal", 3, sg.getSurfaces(0).size());
        List<Face> topfaces = sg.getSurfaces(0).get(0).getFaces();
        TestUtil.assertEquals("Anzahl top faces", segments, topfaces.size());
        List<Face> bottomfaces = sg.getSurfaces(0).get(2).getFaces();
        TestUtil.assertEquals("Anzahl bottom faces", segments, bottomfaces.size());

        Face3List topfaces3 = GeometryHelper.triangulate(sg.vertices,new FaceList(topfaces));
        TestUtil.assertEquals("Anzahl top face3", 2*segments, topfaces3.faces.size());
        Face3List bottomfaces3 = GeometryHelper.triangulate(sg.vertices,new FaceList(bottomfaces));
        TestUtil.assertEquals("Anzahl bottom face3", 2*segments, bottomfaces3.faces.size());
        /*List<Vector3> bottomnormals = GeometryHelper.calculateSmoothVertexNormals(sg.vertices, new SmartArrayList<Face3List>(faces3), new Degree(45));
        for (int i=0;i<bottomnormals.size();i++){
           Vector3 normal =  new Vector3(bottomnormals.get(i));
            System.out.println("" + i + ": " + normal);
        }*/
        
        TestUtil.assertTrue("hasedges", sg.hasedges);
        List<SimpleGeometry> geos = GeometryHelper.prepareGeometry(sg.getVertices(), sg.getFaceLists(), null, false, sg.hasedges);
        TestUtil.assertEquals("nur eine Geo, kein split", 1, geos.size());
        SimpleGeometry geo = geos.get(0);
        TestUtil.assertEquals("Anzahl Vertices", (segments + 1) * 6, geo.getVertices().size());
        for (int i = 0; i < geo.getNormals().size(); i++) {
            System.out.println("normal " + i + ": " + geo.getNormals().getElement(i));
        }
        // wegen Vertexduplizierung nicht mehr einfach zu testen. Eigntlich schon, durch das etasw ineffiziente Duplizieren? Nee.
        // Aber die oberen 8 Triangles haben keinen Dupliziereffekt.
        //TestUtil.assertVector3("top normale",new Vector3(0,1,0),geo.getNormals(),0,10);
        for (int i=0;i<2*segments*3;i++) {
            TestUtil.assertVector3("top normale "+geo.getIndices()[i],new Vector3(0,1,0),geo.getNormals().getElement(geo.getIndices()[i]));
        }
        //nicht alle gleich TestUtil.assertVector3("side normale",new Vector3(0.7071067f,0,-0.7071067f),geo.getNormals(),10,10);
        //TestUtil.assertVector3("bottom normale",new Vector3(0,-1,0),geo.getNormals(),20,10);
    }

    @Test
    public void testCylinderbyTube(){
        float radius = 0.0035f;
        float depth = 0.004f;
        ShapeGeometry sg = ShapeGeometry.buildCylinder(radius, depth);
        List<SimpleGeometry> geos = GeometryHelper.prepareGeometry(sg.getVertices(), sg.getFaceLists(), null, false, sg.hasedges);

    }

    @Test
    public void testRotatedRectangle(){
        int segments=4;
        ShapeGeometry sg = ShapeGeometry.buildRotatedRectangle(segments);
        TestUtil.assertEquals("Anzahl Vertices", (segments + 1) * 4, sg.getVertices().size());
        TestUtil.assertEquals("Anzahl Surfaces", 1, sg.getSurfacesCount());
        List<Surface> surfaces = sg.getSurfaces(0);
        // ist ja ein Rechteck, daher vier Surfaces.
        TestUtil.assertEquals("Anzahl Surfaces normal", 4, surfaces.size());
        SmoothingMap smoothingMap = sg.smoothingMap;
        List<SimpleGeometry> geos = GeometryHelper.prepareGeometry(sg.getVertices(), sg.getFaceLists(), null, false, null, sg.hasedges, smoothingMap);
        //19.12.16: Test nicht weiterverfolgt, weil die SmoothingMap die Railing darstellungsprobleme nicht löst.
    }
    

}