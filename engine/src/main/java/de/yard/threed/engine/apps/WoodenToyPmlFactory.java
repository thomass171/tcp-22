package de.yard.threed.engine.apps;

import de.yard.threed.core.Vector2;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.Shape;
import de.yard.threed.engine.geometry.ShapeGeometry;
import de.yard.threed.engine.loader.PortableMaterial;
import de.yard.threed.engine.loader.PortableModelDefinition;
import de.yard.threed.core.platform.Log;
import de.yard.threed.engine.util.IntProvider;
import de.yard.threed.engine.util.RandomIntProvider;

/**
 * Holzspielzeug.
 *
 * 06.03.21: Vor allem, weil man damit nicht nur GLTF speichern kann, sondern über PortableModelList
 *  auch ein 3D Model. Darum mal umbenannt: WoodenToyGltfFactory->WoodenToyPmlFactory
 * <p>
 * Created by thomass on 18.05.16.
 */
public class WoodenToyPmlFactory {
    Log logger = Platform.getInstance().getLog(WoodenToyPmlFactory.class);
    static IntProvider rand = new RandomIntProvider();
    PortableMaterial mat;

    /**
     *
     */
    public WoodenToyPmlFactory() {
        mat = null;//Material.buildLambertMaterial((Texture.buildBundleTexture("data","textures/gimp/wood/BucheHell.png")));

    }

    /**
     * Ein Block in Standardgröße.
     *
     * @return
     */
    public PortableModelDefinition buildBlock(String matname) {
        return buildBlock(1, 1, 4, matname);
    }

    /**
     * width geht ueber x, height ueber y und depth ueber z.
     *
     * @return
     */
    public PortableModelDefinition buildBlock(double width, double height, double depth, String matname) {
        ShapeGeometry box = ShapeGeometry.buildBox(width, height, depth, null);
        // Die Textur kommt erstmal ohne wickeln komplett auf jede Seite. Sie ist ja seamless, so dass dass ausser bei Front/Back
        // gut passen muesste.
        return PmlFactory.buildElement(box, matname);
    }

    /**
     * radius geht ueber x?,  depth ueber z?
     *
     * @return
     */
    public PortableModelDefinition buildCylinder(double radius, double depth, String matname) {
        ShapeGeometry box = ShapeGeometry.buildCylinder(radius, depth);
        return PmlFactory.buildElement(box, matname);
    }


    /**
     * Ein quasi U rotieren, das kann man dann noch weiter konturieren.
     *
     * @param radius
     * @return
     */
    public PortableModelDefinition buildWheel(double radius, double width, String matname) {
        // Rotieren ganz dicht am Center, sonst verzerrt es innen bzw. hat falsche Normale. Aber dann eiert das Wheel?
        // Zusätzlich nicht ganz im Zentrum anfangen. Durch die echte CircleExtrusion ist das nicht mehr erforderlich.
        double inneroffset = 0;//0.01f;
        ShapeGeometry geometry = ShapeGeometry.buildByCircleRotation(buildWheelShape(inneroffset, radius, width), /*radius / 2,*/64);
        //Mesh mesh = new Mesh(geometry, Material.buildPhongMaterial(Color.RED));
        //SceneNode m = new SceneNode(mesh);
        //mesh.rotateZ(new Degree(90));
        //m.add(mesh);
        return PmlFactory.buildElement(geometry, matname);
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

    public PortableModelDefinition buildChimney(double height, double innerradius, double outerradius, String matname) {
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
        PortableModelDefinition e = PmlFactory.buildElement(chimney, matname);
        e.setName("Chimney");
        return e;
    }

    /**
     * Fahrradgabel. Sind zwei parallel (ueber x von 0 bis -len) laufende Streben. Erstmal als Block statt Rohr.
     * Liefert Connector an (0,0,0).
     */
    public PortableModelDefinition buildGabel(double len, double radius, double distance, String matname) {
        PortableModelDefinition p0 = buildBlock(len, radius * 2, radius * 2, matname);
        p0.setPosition(new Vector3(-len / 2, 0, distance / 2));
        PortableModelDefinition p1 = buildBlock(len, radius * 2, radius * 2, matname);
        p1.setPosition(new Vector3(-len / 2, 0, -distance / 2));
        PortableModelDefinition connector = buildBlock(0, 0, 0, matname);
        connector.attach(p0);
        connector.attach(p1);
        //connector.setPosition();
        PortableModelDefinition pml = new PortableModelDefinition();
        pml.attach(connector);
        return pml;
    }

    /**
     * Einen Holm (ueber x von 0 bis len) , erstmal einfach als Block.
     */
    public PortableModelDefinition buildBeam(double len, double radius, String matname) {
        PortableModelDefinition p0 = buildBlock(len, radius * 2, radius * 2, matname);
        p0.setPosition(new Vector3(len / 2, 0, 0));

        PortableModelDefinition pml = new PortableModelDefinition();
        pml.attach(p0);
        return pml;
    }
}
