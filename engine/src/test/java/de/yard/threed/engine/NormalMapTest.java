package de.yard.threed.engine;


import de.yard.threed.core.Point;
import de.yard.threed.core.Vector3;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.engine.imaging.HeightMap;
import de.yard.threed.engine.imaging.NormalMap;
import de.yard.threed.engine.testutil.PlatformFactoryHeadless;
import de.yard.threed.engine.testutil.EngineTestFactory;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;


/**
 * Auch fuer HeightMap.
 * <p>
 * Created by thomass on 17.05.16.
 */
public class NormalMapTest {
    static Platform platform = EngineTestFactory.initPlatformForTest(new String[]{"engine"}, new PlatformFactoryHeadless());

    static int defaultnormalargb = NormalMap.vector2color(NormalMap.defaultnormal);
    static int rechtsunten45normal = NormalMap.vector2color(new Vector3(1, 1, 1));
    // Einen exakt nach links gibt es nicht
    static Vector3 links90normal = new Vector3(-1, 0, 0.0393f).normalize();
    static Vector3 oben90normal = new Vector3(0, 1, 0.0393f).normalize();
    static int links90normalargb = NormalMap.vector2color(links90normal);

    //@Test
    public void testSemisphere() {
        int radius = 50;
        int width = 512;
        int height = 512;
        NormalMap map = NormalMap.buildSampleNormalmap(width, height, 100, 200, radius);

        int argb = map.image.getColor(260, 60).getARGB();
        Assertions.assertEquals(defaultnormalargb, argb, "defaultnormal");
        Point center = new Point(100, 200);
        // der Schnittpunkt selber hat eine Standard
        argb = map.image.getColor(center.getX(), center.getY()).getARGB();
        Assertions.assertEquals(defaultnormalargb, argb, "centernormal");
        //links vom center auf der Haelfte muss sie 45 Grad nach links zeigen, ganze auss 90 Grad nach links
        argb = map.image.getColor(center.getX() - 50, center.getY()).getARGB();
        Assertions.assertEquals(links90normalargb, argb, "links90normal");

        // die jeweils 3 Pixel oberhalb vom Schnittpunkt liegenden mussen 45 Grad in Richtung Schnittpunkt geneigt sein.
        //argb = map.getRGB(60 - 3, 60 - 3);
        //TestUtil.assertEquals("rechtsunten45normal", rechtsunten45normal, argb);

    }

    /**
     * Eine 200x100 Textur rundherum abrunden (wie ein Holzklotz halt).
     * Die aeussersten Normalen stehen im 45 Grad Winkel, weil sie da ja an die anderen stossen, die auch 45 Grad haben.
     */
    //@Test
    public void testToyBlock() {
        int radius = 2;
        int width = 200;
        int height = 100;
        List<Integer> ypos = new ArrayList<Integer>();
        ypos.add(radius);
        ypos.add(width - radius - 1);
        List<List<Integer>> xpos = new ArrayList<List<Integer>>();
        for (Integer y : ypos) {
            List<Integer> cxpos = new ArrayList<Integer>();
            cxpos.add(10);
            cxpos.add(200);
            cxpos.add(300);
            cxpos.add(500);
            xpos.add(cxpos);
        }
        NormalMap map = NormalMap.buildToyBlockNormalmap(512, 512, ypos, xpos, 1);


    }

    @Test
    public void testWall() {
        HeightMap hm = HeightMap.buildSampleWallGrid();
        int h = hm.getHeightFromMap(60, 60);
        Assertions.assertEquals(HeightMap.DEFAULTHEIGHT, h, "defaultheight");
        // der Schnittpunkt selber hat eine ...
        Point plinksoben = new Point(30, 90);
        h = hm.getHeightFromMap(plinksoben.getX(), plinksoben.getY());
        Assertions.assertEquals(HeightMap.FUGENTIEFE, h, "centerheight");
        h = hm.getHeightFromMap(plinksoben.getX() - 1, plinksoben.getY() - 1);
        Assertions.assertEquals(HeightMap.FUGENTIEFE, h, "nearcenterheight");
        h = hm.getHeightFromMap(plinksoben.getX() - 2, plinksoben.getY() - 2);
        Assertions.assertEquals(HeightMap.DEFAULTHEIGHT, h, "nearcenterheight");

    }

    /**
     * Nur eine waagerechte Fuge ziemlich oben.
     * Mit Radius 2 ist die Fuge drei Pixel breit.
     */
    @Test
    public void testSimpleWall() {
        int radius = 2;
        int width = 512;
        int height = 512;
        List<Integer> ypos = new ArrayList<Integer>();
        ypos.add(10);
        ypos.add(height - radius + 1);
        List<List<Integer>> xpos = new ArrayList<List<Integer>>();

        HeightMap hm = HeightMap.buildDefaultHeightmap(width, height);
        hm.addWallGrid(ypos, xpos, radius);

        int h = hm.getHeightFromMap(60, 60);
        Assertions.assertEquals(HeightMap.DEFAULTHEIGHT, h, "defaultheight");
        Point pgenaudrauf = new Point(33, 10);
        h = hm.getHeightFromMap(pgenaudrauf);
        Assertions.assertEquals(HeightMap.FUGENTIEFE, h, "drauf");
        h = hm.getHeightFromMap(pgenaudrauf.getX(), pgenaudrauf.getY() - 1);
        Assertions.assertEquals(HeightMap.FUGENTIEFE, h, "drueber");
        h = hm.getHeightFromMap(pgenaudrauf.getX(), pgenaudrauf.getY() - 2);
        Assertions.assertEquals(HeightMap.DEFAULTHEIGHT, h, "drueber");
        h = hm.getHeightFromMap(pgenaudrauf.getX() - 2, pgenaudrauf.getY() + 1);
        Assertions.assertEquals(HeightMap.FUGENTIEFE, h, "drunter");
        h = hm.getHeightFromMap(pgenaudrauf.getX() - 2, pgenaudrauf.getY() + 2);
        Assertions.assertEquals(HeightMap.DEFAULTHEIGHT, h, "drunter");

        NormalMap normalmap = hm.buildNormalMap(1);
        //Skizze 26
        Vector3 normal = normalmap.getNormalFromMap(pgenaudrauf);
        TestUtils.assertVector3(new Vector3(0, 0, 1), normal, "innentief");
        normal = normalmap.getNormalFromMap(pgenaudrauf.add(new Point(0, -1)));
        //23.7.2017: Nicht mehr normalisiert (siehe impl)
        TestUtils.assertVector3(new Vector3(0, -1, 1)/*.normalize()*/, normal, "drueber");
        Point pgenaudrauflinks = new Point(0, 10);
        normal = normalmap.getNormalFromMap(pgenaudrauflinks);
        TestUtils.assertVector3(new Vector3(0, 0, 1), normal, "innentief");
        normal = normalmap.getNormalFromMap(pgenaudrauflinks.add(new Point(0, -1)));
        TestUtils.assertVector3(new Vector3(0, -1, 1)/*.normalize()*/, normal, "drueberlinks");

    }

    //@Test
    public void testBuildVector() {
        // Im Zentrum ist die Normale immer der Default.
        Vector3 v = NormalMap.buildVector(0, 0, 3, false);
        TestUtils.assertVector3(new Vector3(), v);
    }

    @Test
    public void testDefaultNormalArgb() {
        int argb = (0xFF << 24) + (127 << 16) + (127 << 8) + 254;
        Assertions.assertEquals(argb, defaultnormalargb, "defaultnormal");
    }

    @Test
    public void testHeightmap() {
        int radius = 50;
        int width = 512;
        int height = 512;
        HeightMap hm = HeightMap.buildDefaultHeightmap(width, height);
        hm.addSphere(100, 200, radius, false);

        int centerheight = hm.getHeightFromMap(100, 200);
        //Das Center ist ganz oben
        Assertions.assertEquals(254, centerheight, "centerheight");

        NormalMap normalmap = hm.buildNormalMap(1);
        Vector3 n = normalmap.getNormalFromMap(4, 7);
        TestUtils.assertVector3(NormalMap.defaultnormal, n, "somenormal");

        n = normalmap.getNormalFromMap(100, 200);
        TestUtils.assertVector3(NormalMap.defaultnormal, n, "centernormal");
        n = normalmap.getNormalFromMap(100 - radius, 200);
        //14.11.16 TODO: unklar, warum der jetzt auf einmal scheitert. TestUtil.assertVector3("links90normal", links90normal, n);
        n = normalmap.getNormalFromMap(100, 200 - radius);
        //unklar was richtig ist TestUtil.assertVector3("oben90normal", oben90normal, n);
    }

    @Test
    public void testDelta() {
        int radius = 1;
        int len = 450;
        Assertions.assertEquals(-1, NormalMap.getDelta(len, 0, radius), "delta");
        Assertions.assertEquals(0, NormalMap.getDelta(len, 1, radius), "delta");
        Assertions.assertEquals(0, NormalMap.getDelta(len, len - 2, radius), "delta");
        Assertions.assertEquals(1, NormalMap.getDelta(len, len - 1, radius), "delta");
        radius = 4;
        Assertions.assertEquals(-1, NormalMap.getDelta(len, 0, radius), "delta");
        Assertions.assertEquals(-0.75f, NormalMap.getDelta(len, 1, radius), "delta");
        Assertions.assertEquals(-0.5f, NormalMap.getDelta(len, 2, radius), "delta");
        Assertions.assertEquals(-0.25f, NormalMap.getDelta(len, 3, radius), "delta");
        Assertions.assertEquals(-0, NormalMap.getDelta(len, 4, radius), "delta");
        Assertions.assertEquals(0, NormalMap.getDelta(len, len - 5, radius), "delta");
        Assertions.assertEquals(0.25f, NormalMap.getDelta(len, len - 4, radius), "delta");
        Assertions.assertEquals(0.5f, NormalMap.getDelta(len, len - 3, radius), "delta");
        Assertions.assertEquals(0.75f, NormalMap.getDelta(len, len - 2, radius), "delta");
        Assertions.assertEquals(1, NormalMap.getDelta(len, len - 1, radius), "delta");
    }
}