package de.yard.threed.engine.apps;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.GenericGeometry;
import de.yard.threed.engine.Material;
import de.yard.threed.engine.Mesh;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.Shape;
import de.yard.threed.engine.geometry.Primitives;
import de.yard.threed.engine.geometry.ShapeGeometry;
import de.yard.threed.engine.Texture;
import de.yard.threed.core.Vector2;
import de.yard.threed.core.platform.Log;
import de.yard.threed.engine.imaging.HeightMap;
import de.yard.threed.engine.imaging.NormalMap;
import de.yard.threed.core.Color;
import de.yard.threed.engine.platform.common.SimpleGeometry;
import de.yard.threed.engine.util.IntProvider;
import de.yard.threed.engine.util.RandomIntProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Holzspielzeug.
 * <p>
 * Created by thomass on 18.05.16.
 */
public class WoodenToyFactory {
    Log logger = Platform.getInstance().getLog(WoodenToyFactory.class);
    static IntProvider rand = new RandomIntProvider();
    Material mat;

    /**
     * * 28.12.18: Deprecated zugiunsten der GLTF Variante. Die Methode hier soll dann evtl. mal ganz weg? Aber ausser der NormalMap! Die muss irgendwo bleiben.
     */
    @Deprecated
    public WoodenToyFactory() {
        mat = Material.buildLambertMaterial((Texture.buildBundleTexture("data","textures/gimp/wood/BucheHell.png")));
    }

    /**
     * Ein Block in Standardgröße.
     *
     * @return
     */
    public SceneNode buildBlock() {
        return buildBlock(1, 1, 4);
    }

    /**
     * width geht ueber x, height ueber y und depth ueber z.
     *
     * @return
     */
    public SceneNode buildBlock(double width, double height, double depth) {
        ShapeGeometry box = ShapeGeometry.buildBox(width, height, depth, null);
        // Die Textur kommt erstmal ohne wickeln komplett auf jede Seite. Sie ist ja seamless, so dass dass ausser bei Front/Back
        // gut passen muesste.
        return buildElement(box);
    }

    /**
     * width geht ueber x, height ueber y und depth ueber z.
     *
     * @return
     */
    public SceneNode buildCylinder(double radius, double depth) {
        ShapeGeometry box = ShapeGeometry.buildCylinder(radius, depth);
        return buildElement(box);
    }

    public SceneNode buildWall() {
        ShapeGeometry plane = ShapeGeometry.buildPlane(1, 1, 1, 1, null);
        Material mat = Material.buildPhongMaterialWithNormalMap(Texture.buildBundleTexture("data","textures/gimp/wood/BucheHell.png"),
                //18.5.16: TODO Das mit der Normalmap ist noch nicht das wahre.
        Texture.buildBundleTexture("data","textures/gimp/SampleWallNormalMapByPetry.png"));
        Mesh mesh = new Mesh(plane, mat);
        SceneNode model = new SceneNode();
        model.setMesh(mesh);
        return model;
    }

    /**
     * Wall mit dynamischer NormalMap.
     *
     * @return
     */
    public SceneNode buildWall(int rows) {
        SimpleGeometry plane = Primitives.buildPlaneGeometry(1, 1, 1, 1);
        Material mat = Material.buildPhongMaterialWithNormalMap(Texture.buildBundleTexture("data","textures/gimp/wood/BucheHell.png"),
                Texture.buildNormalMap(buildWallNormalMap(rows).image));
        Mesh mesh = new Mesh(new GenericGeometry(plane), mat);
        SceneNode model = new SceneNode();
        model.setMesh(mesh);
        return model;
    }

    /**
     * dynamische NormalMap.
     * Die Anzahl der vertikalen Fugen wird aus den Rows hergeleitet. Noch nicht.
     * Ganz aussen rum wird auch eine Senkung angedeutet.
     * So ganz zufaellig sind die Spalten noch nicht.
     * Einfacher Algorithmus statt irgend sowas smartes.
     * 27.4.17: In GWT zur Runtime evtl. ein Problem (zumindest Firefox).
     * @return
     */
    public NormalMap buildWallNormalMap(int rows) {
        logger.warn("possible performance issue");
        long starttime = Platform.getInstance().currentTimeMillis();
        int size = 2;
        int width = 512;
        int height = 512;
        //C# manuelle anpassung
        int[][] widths = new int[][]{
                new int[]{120, 80, 100, 110},
                new int[]{80, 100, 140, 110},
                new int[]{60, 140, 100, 60},
                new int[]{140, 100, 100, 110, 90},
                new int[]{100, 80, 100, 110}
        };
        
        List<Integer> ypos = new ArrayList<Integer>();
        ypos.add(size - 2);
        for (int i = 0; i < rows; i++) {
            ypos.add((i + 1) * (height / rows));
        }
        ypos.add(height - size + 1);
        List<List<Integer>> xpos = new ArrayList<List<Integer>>();
        int currindex = 0;
        currindex = rand.nextInt();
        currindex = currindex % widths.length;
        for (int i = 0; i < rows + 1; i++) {
            List<Integer> cxpos = new ArrayList<Integer>();
            xpos.add(cxpos);
            int x = 0;
            int[] wa = widths[currindex];
            for (int j = 0; j < wa.length; j++) {
                x += wa[j];
                cxpos.add(x);
            }
            currindex++;
            if (currindex >= widths.length){
                currindex=0;
            }
        }

        HeightMap hm = HeightMap.buildDefaultHeightmap(width, height);
        hm.addWallGrid(ypos, xpos, size);
        logger.debug("took"+ (Platform.getInstance().currentTimeMillis()-starttime)+" ms");
        return hm.buildNormalMap(0.5f);

    }

    /**
     * Ein quasi U rotieren, das kann man dann noch weiter konturieren.
     *
     * @param radius
     * @return
     */
    public SceneNode buildWheel(double radius, double width) {
        // Rotieren ganz dicht am Center, sonst verzerrt es innen bzw. hat falsche Normale. Aber dann eiert das Wheel?
        // Zusätzlich nicht ganz im Zentrum anfangen. Durch die echte CircleExtrusion ist das nicht mehr erforderlich.
        double inneroffset = 0;//0.01f;
        ShapeGeometry geometry = ShapeGeometry.buildByCircleRotation(buildWheelShape(inneroffset, radius, width), /*radius / 2,*/64);
        Mesh mesh = new Mesh(geometry, Material.buildPhongMaterial(Color.RED));
        SceneNode m = new SceneNode(mesh);
        //mesh.rotateZ(new Degree(90));
        //m.add(mesh);
        return m;
    }

    /**
     * Zunächst ein nach links offenes liegendes U.
     * 02.12.16: Mit Kanten. Ob das das Wahre ist, muss sich noch zeigen.
     * Nicht ganz innen beginnen wegen potentieller Normalenfalschberechnung.
     *
     * @param radius
     * @return
     */
    public Shape buildWheelShape(double inneroffset, double radius, double height) {
        double width = radius * 2;//Sizes.WHEELRADIUS;
        Shape shape = new Shape(false);
        shape.addPoint(inneroffset, height / 2);
        shape.addPoint(inneroffset + width / 2, height / 2, true);
        shape.addPoint(inneroffset + width / 2, -height / 2, true);
        shape.addPoint(inneroffset, -height / 2);
        return shape;
    }

    public SceneNode buildChimney(double height, double innerradius, double outerradius) {
        Shape shape = new Shape(false);
        //oben anfangen
        double height2 = height / 2;
        shape.addPoint(new Vector2(0, height2));
        shape.addPoint(new Vector2(innerradius, height2), true);
        shape.addPoint(new Vector2(innerradius, height2 - height * 0.1f), true);
        shape.addPoint(new Vector2(outerradius, height2 - height * 0.2f), true);
        shape.addPoint(new Vector2(outerradius, height2 - height * 0.4f), true);
        shape.addPoint(new Vector2(innerradius, height2 - height * 0.5f), true);
        shape.addPoint(new Vector2(innerradius, -height2), true);
        shape.addPoint(new Vector2(0, -height2), false);
        ShapeGeometry chimney = ShapeGeometry.buildByCircleRotation(shape, 64);
        return buildElement(chimney);
    }


    private SceneNode buildElement(ShapeGeometry geo) {
        Mesh mesh = new Mesh(geo, mat);
        SceneNode model = new SceneNode();
        model.setMesh(mesh);
        return model;
    }
}
