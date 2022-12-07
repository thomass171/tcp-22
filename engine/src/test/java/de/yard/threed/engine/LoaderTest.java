package de.yard.threed.engine;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.apps.ModelSamples;
import de.yard.threed.engine.loader.*;
import de.yard.threed.engine.testutil.PlatformFactoryHeadless;
import de.yard.threed.engine.testutil.TestFactory;
import de.yard.threed.engine.platform.common.FaceList;
import de.yard.threed.engine.platform.common.FaceN;
import de.yard.threed.engine.platform.common.SimpleGeometry;
import de.yard.threed.engine.platform.common.StringReader;
import de.yard.threed.engine.test.testutil.TestUtil;
import de.yard.threed.engine.testutil.TestHelper;
import org.junit.jupiter.api.Test;


/**
 * 9.3.21: "genie*", 3DS, shuttle, OBJ, splines nach "sandbox" verschoben.
 * <p>
 * Created by thomass on 08.02.16.
 */
public class LoaderTest {
    //6.7.21 static EngineHelper platform = TestFactory.initPlatformForTest(false,false,null,true);
    static Platform platform = TestFactory.initPlatformForTest(new String[]{"engine"}, new PlatformFactoryHeadless());


    @Test
    public void testAC() {
        try {
            LoaderAC ac = new LoaderAC(new StringReader(LoaderAC.sampleac), false);
            TestUtil.assertEquals("", 1, ac.loadedfile.objects.size());
            TestUtil.assertEquals("", 1, ac.loadedfile.objects.get(0).kids.size());
            TestUtil.assertEquals("facelists", 1, ac.loadedfile.objects.get(0).kids.get(0).getFaceLists().size());
            // 2 faces wegene twosided
            TestUtil.assertEquals("face", 2, ac.loadedfile.objects.get(0).kids.get(0).getFaceLists().get(0).faces.size());
            TestUtil.assertFace4("face 0", new int[]{3, 2, 1, 0}, (FaceN) ac.loadedfile.objects.get(0).kids.get(0).getFaceLists().get(0).faces.get(0));
            TestUtil.assertEquals("materials", 2, ac.loadedfile.materials.size());
            TestUtil.assertTrue("shaded", ac.loadedfile.materials.get(0).shaded);
            TestUtil.assertFalse("shaded", ac.loadedfile.materials.get(1).shaded);
        } catch (Exception e) {
            throw new RuntimeException("Error opening or reading ac file", e);
        }
    }


    @Test
    public void testLndSpots() {
        try {
            LoaderAC ac = new LoaderAC(new StringReader(LoaderAC.lndspots), false);
            TestUtil.assertEquals("", 1, ac.loadedfile.objects.size());
            TestUtil.assertEquals("", 2, ac.loadedfile.objects.get(0).kids.size());
            TestUtil.assertEquals("facelists", 1, ac.loadedfile.objects.get(0).kids.get(0).getFaceLists().size());
            // 2 faces wegen twosided
            TestUtil.assertEquals("face", 2, ac.loadedfile.objects.get(0).kids.get(0).getFaceLists().get(0).faces.size());
            TestUtil.assertEquals("materials", 2, ac.loadedfile.materials.size());
            TestUtil.assertTrue("shaded", ac.loadedfile.materials.get(0).shaded);
            TestUtil.assertFalse("shaded", ac.loadedfile.materials.get(1).shaded);
            // LHND ist twosided. Die beiden Faces sind aber nicht definiert, eine ist halt front, die andere back.
            FaceList lhndfaces = ac.loadedfile.objects.get(0).kids.get(0).getFaceLists().get(0);
            //FaceList lhndfaces1 = ac.loadedfile.objects.get(0).kids.get(0).getFaceLists().get(1);
            TestUtil.assertFace4("lhndfaces0", new int[]{3, 2, 1, 0}, (FaceN) lhndfaces.faces.get(0));
            TestUtil.assertFace4("lhndfaces1", new int[]{0, 1, 2, 3}, (FaceN) lhndfaces.faces.get(1));

        } catch (Exception e) {
            throw new RuntimeException("Error opening or reading ac file", e);
        }
    }

    /**
     * Der tower erfordert vertexduplizierung wegen UV.
     */
    @Test
    public void testEgkk_tower() {
        try {
            LoaderAC ac = new LoaderAC(new StringReader(LoaderAC.egkk_tower), false);
            PortableModelList ppfile = ac.preProcess();
            TestUtil.assertEquals("", 1, ppfile.getObjectCount());
            TestUtil.assertEquals("", 1, ppfile.getObject(0).kids.size());
            PortableModelDefinition tower = ppfile.getObject(0).kids.get(0);
            SimpleGeometry geo = tower.geolist.get(0);
            //29 durch ausprobieren
            TestUtil.assertEquals("vertices", 29, geo.getVertices().size());
            TestUtil.assertEquals("indices", 4 * 2 * 3, geo.getIndices().length);
        } catch (Exception e) {
            throw new RuntimeException("Error opening or reading ac file", e);
        }
    }

    /**
     * 24.1.19: Liegt hier vielleicht nicht ganz ideal, aber einen besseren Platz gibt es nicht?
     */
    @Test
    public void testPortableModelList() {
        PortableModelList needle = ModelSamples.buildCompassNeedle(20, 30);
        SceneNode n = needle.createPortableModelBuilder().buildModel(null);
        TestUtil.assertEquals("needle.name", "CompassNeedle", n.getName());
    }

    @Test
    public void testSceneLoader() throws Exception {

        SceneLoader sceneLoader = new SceneLoader(TestHelper.loadFileFromClasspath("SimpleScene.json"), "");
        PortableModelList ppfile = sceneLoader.preProcess();
        TestUtil.assertEquals("", 1, ppfile.getObjectCount());
        TestUtil.assertEquals("", 0, ppfile.getObject(0).kids.size());
        PortableModelDefinition box = ppfile.getObject(0);
        SimpleGeometry geo = box.geolist.get(0);
        TestUtil.assertEquals("vertices", 3 * 8, geo.getVertices().size());

    }

}
