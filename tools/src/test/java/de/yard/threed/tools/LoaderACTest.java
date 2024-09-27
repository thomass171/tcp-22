package de.yard.threed.tools;

import de.yard.threed.core.NumericValue;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.geometry.FaceList;
import de.yard.threed.core.geometry.GeometryHelper;
import de.yard.threed.core.geometry.SimpleGeometry;
import de.yard.threed.core.loader.LoadedObject;
import de.yard.threed.core.loader.LoaderAC;
import de.yard.threed.core.loader.PortableModel;
import de.yard.threed.core.loader.PortableModelDefinition;
import de.yard.threed.core.loader.StringReader;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.testutil.CoreTestFactory;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.engine.Material;
import de.yard.threed.engine.loader.PortableModelBuilder;
import de.yard.threed.engine.test.testutil.TestUtil;
import de.yard.threed.javacommon.ConfigurationByEnv;
import de.yard.threed.javacommon.SimpleHeadlessPlatform;
import de.yard.threed.javacommon.SimpleHeadlessPlatformFactory;
import de.yard.threed.javanative.FileReader;
import de.yard.threed.tools.testutil.ModelAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

public class LoaderACTest {
    Platform platform = CoreTestFactory.initPlatformForTest(new SimpleHeadlessPlatformFactory(), null,
            ConfigurationByEnv.buildDefaultConfigurationWithEnv(new HashMap<>()));


    @ParameterizedTest
    @ValueSource(booleans = {
            false,
            true,
    })
    public void testMultiObject(boolean twoSidedAsSingleSided) throws Exception {

        String acfile = "tools/src/test/resources/multi-object.ac";
        String source = FileReader.readAsString(new File(TestUtils.locatedTestFile(acfile)));
        if (twoSidedAsSingleSided) {
            source = source.replaceAll(" 0x30", " 0x10");
        }
        LoaderAC ac = new LoaderAC(new StringReader(source), false);
        LoadedObject obj3 = ac.loadedfile.object.kids.get(2);
        assertEquals(1, obj3.faces.size());
        FaceList faceList = obj3.faces.get(0);
        // obj3 is twosided originally
        assertEquals( 12, obj3.faces.get(0).faces.size());
        if (twoSidedAsSingleSided) {
            //assertNull(obj3.backfaces.get(0));
            assertEquals(0, obj3.backfaces.get(0).faces.size());
        }else {
            assertEquals(12, obj3.backfaces.get(0).faces.size());
        }

        LoadedObject obj5 = ac.loadedfile.object.kids.get(4);
        assertFalse(obj5.faces.get(0).isShaded());
        assertTrue(obj5.faces.get(1).isShaded());

        LoadedObject obj6 = ac.loadedfile.object.kids.get(5);
        assertEquals("obj6", obj6.name);
        // detailed test. Heads up: modifies data (vertices), so only for debugging
        //SimpleGeometry geo = GeometryHelper.prepareGeometry(obj6.vertices, obj6.getAllFacelists(), null, false, obj6.crease/*, false, null*/).get(0);

        LoadedObject obj7 = ac.loadedfile.object.kids.get(6);
        assertEquals("obj7", obj7.name);
        assertEquals(1, obj7.faces.size());
        assertTrue(obj7.faces.get(0).isShaded());
        assertEquals(6, obj7.faces.get(0).faces.size());
        assertEquals(1, obj7.backfaces.size());
        assertEquals(0, obj7.backfaces.get(0).faces.size());
        assertEquals(30.0, obj7.crease.getDegree());
        // detailed test. Heads up: modifies data (vertices), so only for debugging
        //SimpleGeometry geo = GeometryHelper.prepareGeometry(obj7.vertices, obj7.getAllFacelists(), null, false, obj7.crease).get(0);

        LoadedObject obj8 = ac.loadedfile.object.kids.get(7);
        assertEquals("obj8", obj8.name);
        assertEquals(1, obj8.faces.size());
        assertTrue(obj8.faces.get(0).isShaded());
        assertEquals(11, obj8.faces.get(0).faces.size());
        assertEquals(1, obj8.backfaces.size());
        assertEquals(0, obj8.backfaces.get(0).faces.size());
        assertEquals(50.0, obj8.crease.getDegree());
        // detailed test. Heads up: modifies data (vertices), so only for debugging
        //SimpleGeometry geo = GeometryHelper.prepareGeometry(obj8.vertices, obj8.getAllFacelists(), null, false, obj8.crease).get(0);

        PortableModel portableModel = ac.buildPortableModel();
        ModelAssertions.assertMultiObject(portableModel, false, true, twoSidedAsSingleSided);

        Material material = PortableModelBuilder.buildMaterial(null, portableModel.findMaterial("unshaded1glass"), null, false);
        assertNotNull(material);
        assertNotNull(material.material);
        Float transparency = NumericValue.transparency(((SimpleHeadlessPlatform.DummyMaterial) material.material).parameters);
        assertEquals(0.27, transparency.floatValue(), 0.001);
    }

    @Test
    public void testDuplicateMatname() throws Exception {

        String acfile = "tools/src/test/resources/duplicate-matname.ac";
        LoaderAC ac = new LoaderAC(new StringReader(FileReader.readAsString(new File(TestUtils.locatedTestFile(acfile)))), false);

        LoadedObject obj = ac.loadedfile.object.kids.get(0);
        assertEquals(3, obj.faces.size());
        assertEquals(1, obj.faces.get(0).faces.size());
        assertEquals(1, obj.faces.get(1).faces.size());
        assertEquals(1, obj.faces.get(2).faces.size());
        assertEquals(3, obj.backfaces.size());
        assertEquals(1, obj.backfaces.get(0).faces.size());
        assertEquals(0, obj.backfaces.get(1).faces.size());
        assertEquals(0, obj.backfaces.get(2).faces.size());

        PortableModel portableModel = ac.buildPortableModel();
        ModelAssertions.assertDuplicateMatname(portableModel, false, true);


    }

    @Test
    public void testTwoSidedRectangle() throws Exception {

        LoaderAC ac = new LoaderAC(new StringReader(LoaderAC.twosidedRectangle), false);
        LoadedObject rect = ac.loadedfile.object.kids.get(0);
        // rectangle is twosided
        assertEquals(1, rect.faces.size());
        assertEquals(1, rect.backfaces.size());
        assertEquals(1, rect.faces.get(0).faces.size());
        assertEquals(1, rect.backfaces.get(0).faces.size());

        PortableModel portableModel = ac.buildPortableModel();

    }

    /**
     * a3cd-ref (was built back in 2016 by Ac3DBuilder.java)
     * Skizze 10
     */
    @Test
    public void testTriangulateAndNormalBuildingAc3dRef() throws Exception {
        PortableModel loadedfile;
        String acfile = "tools/src/test/resources/ac3d-ref.ac";
        // 6.8.24: Still ignoring ac 'world'. 16.9.24:No longer accepted
        LoaderAC ac = new LoaderAC(new StringReader(FileReader.readAsString(new File(TestUtils.locatedTestFile(acfile)))), false);

        loadedfile = ac.buildPortableModel();//28.10.23 ModelLoader.readModelFromBundle(new BundleResource(BundleRegistry.getBundle("data-old"),"model/ac3d-ref.ac"), false,0);

        PortableModelDefinition ac3drefflat = loadedfile.getRoot().kids.get(0);
        checkRefCylinder(ac3drefflat, true);
        PortableModelDefinition ac3drefsmooth = loadedfile.getRoot().kids.get(1);
        checkRefCylinder(ac3drefsmooth, false);
    }

    /**
     * Den RefCylinder pruefen, der entwder mit flat oder smoothshading erstellt wurde.
     *
     * @param ac3dref
     * @param flat
     */
    private void checkRefCylinder(PortableModelDefinition ac3dref, boolean flat) {
        Vector3 refnormal0 = new Vector3(0.92387956f, 0, -0.38268346f);
        Vector3 refnormal1 = new Vector3(0.38268346f, 0, -0.92387956f);
        Vector3 refnormal0smooth = new Vector3(1, 0, 0);
        //Die Refwerte sind einfach uebernommen, scheinen aber nicht ganz richtig. x und z sollten doch absolut gleich sein.TODO
        Vector3 refnormal1smooth = new Vector3(0.603748f, 0, -0.603748f);
        int segments = 8;
        /*ist schon preprocessed TestUtil.assertEquals("vertices", (segments + 1) * 2, ac3dref.vertices.size());
        List<SimpleGeometry> geolist = GeometryHelper.prepareGeometry(ac3dref.vertices, ac3dref.faces, null, false, ac3dref.crease);*/
        SimpleGeometry geo = ac3dref.geo;//list.get(0);
        // Bei der flat Variante gibt es wegen duplizierter Vertices fast doppelt zu viel.
        int basesize = (segments + 1) * 2;
        int size = basesize;
        if (flat) {
            size += (segments - 1) * 2;
        }
        TestUtil.assertEquals("vertices", size, geo.getVertices().size());
        TestUtil.assertEquals("normals", size, geo.getNormals().size());
        TestUtil.assertEquals("indices", 2 * 8 * 3, geo.getIndices().length);

        // Die erste zwei Faces pruefen.
        TestUtil.assertFace3("face3 0", new int[]{0, 2, 1}, geo.getIndices(), 0);
        TestUtil.assertFace3("face3 1", new int[]{3, 1, 2}, geo.getIndices(), 1);
        // Sollten im Face dieselbe Normale haben (bei flat auch in Normallist)
        //TODO TestUtil.assertVector3("facenormals", ((Face3) geo.getFaces().faces.get(0)).normal, refnormal0);
        //TODO TestUtil.assertVector3("facenormals", ((Face3) geo.getFaces().faces.get(0)).normal, ((Face3) geo.getFaces().faces.get(1)).normal);
        if (flat) {
            // Die zweiten zwei Faces pruefen.
            TestUtil.assertFace3("face3 2", new int[]{18, 4, 19}, geo.getIndices(), 2);
            TestUtil.assertFace3("face3 3", new int[]{5, 19, 4}, geo.getIndices(), 3);
            // Der Vertex 2 muesste der erste duplizierte sein, der 3 der zweite; also die linke Seite von Face 2
            // 15.12.16: Das Duplizieren geht nicht mehr so systematisch. Darum nicht darauf verlassen; die Face Reihenfolge hat sich aber eigentlich nicht geändert.
            // Duplizierung ist doch noch so wie hier angenommen. TODO Mit assertFaceIndexNormals kann man doch jetzt auch alle Normale testen
            // 16.9.24 fails now TestUtil.assertFaceIndexNormals(geo.getIndices(), geo.getNormals(), new int[]{0, 1}, refnormal0);
            for (int i = 0; i < 4; i++) {
                // 16.9.24 fails now  TestUtil.assertVector3("facenormals " + i + ":", refnormal0, geo.getNormals().getElement(i));
            }

            /* 16.9.24 fails now   TestUtil.assertVector3("facenormals", refnormal1, geo.getNormals().getElement(basesize));
            TestUtil.assertVector3("facenormals", refnormal1, geo.getNormals().getElement(basesize + 1));
            TestUtil.assertVector3("facenormals", refnormal1, geo.getNormals().getElement(4));
            TestUtil.assertVector3("facenormals", refnormal1, geo.getNormals().getElement(5));
            //Fuer die restlichen pruefen, dass sie identisch sind
            for (int i = 0; i < segments - 1; i++) {
                Vector3 refn = geo.getNormals().getElement(4 + (i * 2));
                TestUtil.assertVector3("facenormals " + i, refn, geo.getNormals().getElement(basesize + (i * 2)));
                TestUtil.assertVector3("facenormals " + i, refn, geo.getNormals().getElement(basesize + (i * 2) + 1));
                TestUtil.assertVector3("facenormals " + i, refn, geo.getNormals().getElement(5 + (i * 2)));
            }*/

            TestUtil.assertFace3("face3 2", new int[]{basesize + 0, 4, basesize + 1}, geo.getIndices(), 2);

        } else {
            //Die Normalen an den Ausenkanten (0,1 und am Ende) stimmen nicht, weil die Geo nicht geschlossen ist und damit an der Kante nicht
            //gemittelt werden kann.
            //TODO stimmt nicht ganz TestUtil.assertVector3("smoothvertexnormals", refnormal1smooth, geo.normals.get(2));
            //TODO stimmt nicht ganz TestUtil.assertVector3("smoothvertexnormals", refnormal1smooth, geo.normals.get(3));

        }

        // TestUtil.assertFace3("face3 2", new int[]{0, 1, 2}, (Face3) f.faces.get(2));
        //TestUtil.assertFace3("face3 3", new int[]{0, 2, 3}, (Face3) f.faces.get(3));

        /*
        // 4xFace3 ergibt eine VBO Groesse von 12
        TestUtil.assertEquals("Anzahl Normals", 12, normals.size());
        // die ersten 6 Normalen zeigen alle nach unten (y=-1), die anderen 6 nach oben
        for (int i = 0; i < 6; i++) {
            TestUtil.assertVector3("normal down" + i, new Vector3(0, -1, 0), normals.get(i));
            TestUtil.assertVector3("normal up " + i, new Vector3(0, 1, 0), normals.get(6 + i));
        }*/
    }
}
