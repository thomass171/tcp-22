package de.yard.threed.engine;

import de.yard.threed.core.loader.InvalidDataException;
import de.yard.threed.core.loader.LoaderAC;
import de.yard.threed.core.loader.PortableMaterial;
import de.yard.threed.core.loader.PortableModelDefinition;
import de.yard.threed.core.loader.PortableModel;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.apps.ModelSamples;
import de.yard.threed.engine.loader.*;
import de.yard.threed.engine.testutil.EngineTestUtils;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.core.geometry.FaceList;
import de.yard.threed.core.geometry.FaceN;
import de.yard.threed.core.geometry.SimpleGeometry;
import de.yard.threed.core.loader.StringReader;
import de.yard.threed.engine.test.testutil.TestUtil;
import de.yard.threed.javacommon.SimpleHeadlessPlatformFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


/**
 * 9.3.21: "genie*", 3DS, shuttle, OBJ, splines nach "sandbox" verschoben.
 * <p>
 * Created by thomass on 08.02.16.
 */
public class LoaderTest {
    static Platform platform = EngineTestFactory.initPlatformForTest(new String[]{"engine"}, new SimpleHeadlessPlatformFactory());


    @Test
    public void testAC() throws InvalidDataException {
        LoaderAC ac = new LoaderAC(new StringReader(LoaderAC.sampleac), false);
        assertNotNull(ac.loadedfile.object);
        TestUtil.assertEquals("", 1, ac.loadedfile.object.kids.size());
        TestUtil.assertEquals("facelists", 1, ac.loadedfile.object.kids.get(0).getFaceLists().size());
        // 2 facelists due to twosided
        TestUtil.assertEquals("face", 1, ac.loadedfile.object.kids.get(0).getFaceLists().size());
        TestUtil.assertEquals("face", 1, ac.loadedfile.object.kids.get(0).backfaces.size());
        TestUtil.assertEquals("face", 1, ac.loadedfile.object.kids.get(0).getFaceLists().get(0).faces.size());
        TestUtil.assertEquals("face", 1, ac.loadedfile.object.kids.get(0).backfaces.get(0).faces.size());
        TestUtil.assertFace4("face 0", new int[]{3, 2, 1, 0}, (FaceN) ac.loadedfile.object.kids.get(0).getFaceLists().get(0).faces.get(0));
        TestUtil.assertEquals("materials", 2, ac.loadedfile.materials.size());
        TestUtil.assertTrue("shaded", ac.loadedfile.materials.get(0).shaded);
        TestUtil.assertFalse("shaded", ac.loadedfile.materials.get(1).shaded);
    }


    @Test
    public void testLndSpots() throws InvalidDataException {
        LoaderAC ac = new LoaderAC(new StringReader(LoaderAC.lndspots), false);
        assertNotNull(ac.loadedfile.object);
        TestUtil.assertEquals("", 2, ac.loadedfile.object.kids.size());
        TestUtil.assertEquals("facelists", 1, ac.loadedfile.object.kids.get(0).getFaceLists().size());
        // 2 faces wegen twosided
        TestUtil.assertEquals("face", 1, ac.loadedfile.object.kids.get(0).getFaceLists().get(0).faces.size());
        TestUtil.assertEquals("face", 1, ac.loadedfile.object.kids.get(0).backfaces.get(0).faces.size());
        TestUtil.assertEquals("materials", 2, ac.loadedfile.materials.size());
        TestUtil.assertTrue("shaded", ac.loadedfile.materials.get(0).shaded);
        TestUtil.assertFalse("shaded", ac.loadedfile.materials.get(1).shaded);
        // LHND ist twosided. Die beiden Faces sind aber nicht definiert, eine ist halt front, die andere back.
        FaceList lhndfaces = ac.loadedfile.object.kids.get(0).getFaceLists().get(0);
        //FaceList lhndfaces1 = ac.loadedfile.objects.get(0).kids.get(0).getFaceLists().get(1);
        TestUtil.assertFace4("lhndfaces0", new int[]{3, 2, 1, 0}, (FaceN) lhndfaces.faces.get(0));
        TestUtil.assertFace4("lhndfaces1", new int[]{0, 1, 2, 3}, (FaceN) ac.loadedfile.object.kids.get(0).backfaces.get(0).faces.get(0));
    }

    /**
     * Der tower erfordert vertexduplizierung wegen UV.
     */
    @Test
    public void testEgkk_tower() throws InvalidDataException {
        LoaderAC ac = new LoaderAC(new StringReader(LoaderAC.egkk_tower), false);
        PortableModel ppfile = ac.buildPortableModel();
        //TestUtil.assertEquals("", 1, ppfile.getObjectCount());
        TestUtil.assertEquals("", 1, ppfile.getRoot().kids.size());
        PortableModelDefinition tower = ppfile.getRoot().kids.get(0);
        SimpleGeometry geo = tower.geo/*list.get(0)*/;

        // 18.9.24:Up to 32 after shading refactorings
        TestUtil.assertEquals("vertices", /*29 less after refactoring of index duplicating*//*24*/32, geo.getVertices().size());
        TestUtil.assertEquals("indices", 4 * 2 * 3, geo.getIndices().length);

    }

    /**
     * 24.1.19: Maybe not the best location here, but which is better?
     */
    @Test
    public void testPortableModel() {
        PortableModel needle = ModelSamples.buildCompassNeedle(20, 30);
        SceneNode n = PortableModelBuilder.buildModel(needle, null);
        TestUtil.assertEquals("needle.name", "CompassNeedle", n.getName());
        Assertions.assertEquals(0, PortableModelBuilder.dummyMaterialReasons.size(), "dummymaterialused");
    }

    @Test
    public void testSceneLoader() throws Exception {

        SceneLoader sceneLoader = new SceneLoader(EngineTestUtils.loadFileFromClasspath("SimpleScene.json"), "");
        PortableModel ppfile = sceneLoader.buildPortableModel();
        //TestUtil.assertEquals("", 1, ppfile.getObjectCount());
        // 27.7.24: scene objects are now one level below root.
        TestUtil.assertEquals("", 1, ppfile.getRoot().kids.size());
        PortableModelDefinition box = ppfile.getRoot().kids.get(0);
        SimpleGeometry geo = box.geo/*list.get(0)*/;
        TestUtil.assertEquals("vertices", 3 * 8, geo.getVertices().size());

    }

    @Test
    public void testAC_RGB2PNG() throws InvalidDataException {

        String sampleWithRgb = "AC3Db\n" +
                "MATERIAL \"\" rgb 1 1 1  amb 0.2 0.2 0.2  emis 0 0 0  spec 0.5 0.5 0.5  shi 10  trans 0\n" +
                "OBJECT world\n" +
                "kids 1\n" +
                "OBJECT poly\n" +
                "name \"rect\"\n" +
                "texture \"sample.rgb\"\n" +
                "numvert 4\n" +
                "-1 0.5 0\n" +
                "1 0.5 0\n" +
                "1 -0.5 0\n" +
                "-1 -0.5 0\n" +
                "numsurf 1\n" +
                // unshaded
                "SURF 0x20\n" +
                "mat 0\n" +
                "refs 4\n" +
                "3 0 0\n" +
                "2 1 0\n" +
                "1 1 1\n" +
                "0 0 1\n" +
                "kids 0\n";

        LoaderAC ac = new LoaderAC(new StringReader(sampleWithRgb), false);
        assertNotNull(ac.loadedfile.object);
        TestUtil.assertEquals("", 1, ac.loadedfile.object.kids.size());
        TestUtil.assertEquals("materials", 2, ac.loadedfile.materials.size());
        TestUtil.assertTrue("shaded", ac.loadedfile.materials.get(0).shaded);
        TestUtil.assertFalse("shaded", ac.loadedfile.materials.get(1).shaded);

        PortableModel portableModelList = ac.buildPortableModel();
        // 14.8.24 materials now only contains really used material
        assertEquals(1, portableModelList.getMaterialCount());
        PortableMaterial unshaded = portableModelList.getMaterialByIndex(0);
        assertFalse(unshaded.isShaded());
        // rgb should have been mapped to png
        assertEquals("sample.png", unshaded.getTexture());
    }
}
