package de.yard.threed.maze;

import de.yard.threed.core.Color;
import de.yard.threed.core.Degree;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector2;
import de.yard.threed.core.Vector3;
import de.yard.threed.engine.GenericGeometry;
import de.yard.threed.engine.Material;
import de.yard.threed.engine.Mesh;
import de.yard.threed.engine.ProportionalUvMap;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.Shape;
import de.yard.threed.engine.ShapeFactory;
import de.yard.threed.engine.UvMap1;
import de.yard.threed.engine.geometry.Primitives;
import de.yard.threed.engine.geometry.ShapeGeometry;
import de.yard.threed.engine.platform.common.SimpleGeometry;

public class MazeTraditionalModelFactory extends MazeModelFactory {

    public MazeTraditionalModelFactory(MazeTheme settings) {
        super(settings);
    }

    /**
     * Für Wände in zwei Richtungen, aber nur gerade, nicht mehr Ecken.
     * <p/>
     * Die Wall besteht auf zwei Planes, damit sie von beiden Seiten sichtbar ist (sowohl Ecken wie auch gerade).
     * <p/>
     * Das ginge zwar auch mit double sided Meshes, aber das waere vielleicht nicht so zukunftssicher.
     * Ausserdem kann man mit zwei Planes z-Fighting und Culling Probleme leicher umschiffen.
     * siehe auch Wiki zu two sided meshes.
     * <p>
     * Die Wall steht in Richtung y /bzw. z) mit dem Boden auf y=0 an x=0,z=0.
     * <p>
     * 21.5.21: Refactoring: nicht auf y0 ausrichten, um besser Dinge (ans center) attachen zu können? Ist aber eigentlich nicht ganz so wichtig.
     * 1.6.21: corner extrahiert. Liefert ein aufrecht stehendes Straight-Element. Das wird dann nachher passend um Y rotiert.
     *
     * <p>
     *
     * @param width
     * @return
     */
    public SceneNode buildWall(double width, int wallmode) {
        SceneNode model = new SceneNode();
        Vector3 pos;
        SceneNode element;
        double e2 = elementdistance / 2;
        double offset = 0;
        double uvx = 1;

        double wallWidth = width;
        switch (wallmode) {
            case MazeTraditionalTerrain.STRAIGHTWALLMODE_LOW_PART:
                wallWidth = width / 2;
                uvx = 0.5;
                offset = -wallWidth / 2;
                break;
            case MazeTraditionalTerrain.STRAIGHTWALLMODE_HIGH_PART:
                wallWidth = width / 2;
                uvx = 0.5;
                offset = wallWidth / 2;
                break;
        }
        element = buildPlaneForWall(wallWidth, 0, uvx);//buildSingleWallPlane(width, true, corner, new Degree(180));
        element.getTransform().setPosition(new Vector3(offset, 0, e2));
        model.attach(element);

        element = buildPlaneForWall(wallWidth, 0, uvx);//buildSingleWallPlane(width, false, corner, new Degree(180));
        element.getTransform().setPosition(new Vector3(offset, 0, -e2));
        element.getTransform().setRotation(Quaternion.buildRotationY(new Degree(180)));
        model.attach(element);

        // aufrecht stellen (nicht mehr) und halb nach oben
        return SceneNode.buildSceneNode(model, new Vector3(0, PILLARHEIGHT / 2, 0), new Quaternion()/*Quaternion.buildRotationX(new Degree(-90))*/);
    }

    /**
     * Liefert ein aufrecht stehendes Corner-Element (nach links und unten) bestehend aus 4 einzelnen Planes.
     * <p>
     * <p>
     * Das wird dann nachher passend um Y rotiert.
     */
    public SceneNode buildCorner(float width, int wallmode) {
        SceneNode model = new SceneNode();
        SceneNode element;

        // an jedem Ende um die Haelfte der distance anpassen
        // aussen
        double awidth = width + elementdistance;
        // innen
        double iwidth = width - elementdistance;

        double e2 = elementdistance / 2;
        // ausseneckteile
        element = buildPlaneForWall(awidth / 2, 0.5, 1);//buildSingleWallPlane(width, true, corner, new Degree(180));
        element.getTransform().setPosition(new Vector3(-awidth / 4 + e2, 0, -e2));
        element.getTransform().setRotation(Quaternion.buildRotationY(new Degree(180)));
        model.attach(element);
        element = buildPlaneForWall(awidth / 2, 0, 0.5);//buildSingleWallPlane(width, true, corner, new Degree(180));
        element.getTransform().setPosition(new Vector3(e2, 0, awidth / 4 - e2));
        element.getTransform().setRotation(Quaternion.buildRotationY(new Degree(90)));
        model.attach(element);

        // inneneckteile
        element = buildPlaneForWall(iwidth / 2, 0, 0.5);//buildSingleWallPlane(width, false, corner, new Degree(180));
        element.getTransform().setPosition(new Vector3(-iwidth / 4 - e2, 0, e2));
        model.attach(element);
        element = buildPlaneForWall(iwidth / 2, 0.5, 1);//buildSingleWallPlane(width, false, corner, new Degree(180));
        element.getTransform().setPosition(new Vector3(-e2, 0, iwidth / 4 + e2));
        element.getTransform().setRotation(Quaternion.buildRotationY(new Degree(-90)));
        model.attach(element);

        // aufrecht stellen (nicht mehr) und halb nach oben
        return SceneNode.buildSceneNode(model, new Vector3(0, PILLARHEIGHT / 2, 0), new Quaternion()/*Quaternion.buildRotationX(new Degree(-90))*/);
    }

    /**
     * One of the two planes of a wall.
     *
     * @return
     */
    /*1.6.21rivate SceneNode buildSingleWallPlane(float width, boolean top, boolean corner, Degree bottomrotation) {
        SceneNode model = new SceneNode();
        SceneNode element = buildPlaneForWall(width, top, corner, bottomrotation);
        model.attach(element);
        return model;
    }*/

    /**
     * Für Wände in drei Richtungen, also "Ts".
     * Liefert ein aufrecht stehendes T-Element. Das wird dann nachher passend um Y rotiert.
     *
     * @return
     */
    public SceneNode buildT(float width) {
        SceneNode model = new SceneNode();
        double e2 = elementdistance / 2;

        // Die lange Seite ist "hinten" (negative z)
        SceneNode longelement = buildPlaneForWall(width);//buildSingleWallPlane(width, false, false, new Degree(180));
        longelement.getTransform().setPosition(new Vector3(0, 0, -e2));
        longelement.getTransform().setRotation(Quaternion.buildRotationY(new Degree(180)));
        model.attach(longelement);

        double iwidth = width - elementdistance;

        addInnerCorner(model, iwidth, true, true);
        addInnerCorner(model, iwidth, false, true);

        return SceneNode.buildSceneNode(model, new Vector3(0, PILLARHEIGHT / 2, 0), new Quaternion()/*Quaternion.buildRotationX(new Degree(-90))*/);
    }

    /**
     * 2 inneneckteile
     */
    public void addInnerCorner(SceneNode model, double iwidth, boolean left, boolean bottom) {
        double e2 = elementdistance / 2;

        SceneNode element;
        if (left) {

            if (bottom) {
                element = buildPlaneForWall(iwidth / 2, 0, 0.5);//buildSingleWallPlane(width, false, corner, new Degree(180));
                element.getTransform().setPosition(new Vector3(-iwidth / 4 - e2, 0, e2));
                model.attach(element);
                element = buildPlaneForWall(iwidth / 2, 0.5, 1);//buildSingleWallPlane(width, false, corner, new Degree(180));
                element.getTransform().setPosition(new Vector3(-e2, 0, iwidth / 4 + e2));
                element.getTransform().setRotation(Quaternion.buildRotationY(new Degree(-90)));
                model.attach(element);
            } else {
                element = buildPlaneForWall(iwidth / 2, 0, 0.5);//buildSingleWallPlane(width, false, corner, new Degree(180));
                element.getTransform().setPosition(new Vector3(-iwidth / 4 - e2, 0, -e2));
                element.getTransform().setRotation(Quaternion.buildRotationY(new Degree(180)));
                model.attach(element);
                element = buildPlaneForWall(iwidth / 2, 0.5, 1);//buildSingleWallPlane(width, false, corner, new Degree(180));
                element.getTransform().setPosition(new Vector3(-e2, 0, -iwidth / 4 - e2));
                element.getTransform().setRotation(Quaternion.buildRotationY(new Degree(-90)));
                model.attach(element);
            }
        } else {
            if (bottom) {
                element = buildPlaneForWall(iwidth / 2, 0, 0.5);//buildSingleWallPlane(width, false, corner, new Degree(180));
                element.getTransform().setPosition(new Vector3(iwidth / 4 + e2, 0, e2));
                model.attach(element);
                element = buildPlaneForWall(iwidth / 2, 0.5, 1);//buildSingleWallPlane(width, false, corner, new Degree(180));
                element.getTransform().setPosition(new Vector3(e2, 0, iwidth / 4 + e2));
                element.getTransform().setRotation(Quaternion.buildRotationY(new Degree(90)));
                model.attach(element);
            } else {
                element = buildPlaneForWall(iwidth / 2, 0, 0.5);//buildSingleWallPlane(width, false, corner, new Degree(180));
                element.getTransform().setPosition(new Vector3(iwidth / 4 + e2, 0, -e2));
                element.getTransform().setRotation(Quaternion.buildRotationY(new Degree(180)));
                model.attach(element);
                element = buildPlaneForWall(iwidth / 2, 0.5, 1);//buildSingleWallPlane(width, false, corner, new Degree(180));
                element.getTransform().setPosition(new Vector3(e2, 0, -iwidth / 4 - e2));
                element.getTransform().setRotation(Quaternion.buildRotationY(new Degree(90)));
                model.attach(element);
            }
        }
    }

    /**
     * Für Wände in vier Richtungen, also "Ts".
     * Insgesamt 8 Innneneckteile.
     *
     * @return
     */
    public SceneNode buildCross(float width) {
        SceneNode model = new SceneNode();
        double iwidth = width - elementdistance;

        addInnerCorner(model, iwidth, true, true);
        addInnerCorner(model, iwidth, false, true);
        addInnerCorner(model, iwidth, true, false);
        addInnerCorner(model, iwidth, false, false);

        return SceneNode.buildSceneNode(model, new Vector3(0, PILLARHEIGHT / 2, 0), new Quaternion());
    }

    /**
     * Build single plane element for a wall.
     * Bei Ecken ist top immer das Aussenelement (längere) und Bottom das Innere (kürzere)
     * 1.6.21: Parameter vereinfacht. Hier wird nicht mehr rotiert (bleibt in z0) und das Element nur in der erforderlichen Laenge erstellt.
     */
    private SceneNode buildPlaneForWall(double width, double u0, double u1/*, boolean top, boolean corner, Degree bottomrotation*/) {

        Material mat;
        if (wallnormalmap != null) {
            int index = MazeScene.rand.nextInt() % wallnormalmap.length;
            mat = Material.buildPhongMaterialWithNormalMap(walltexture, wallnormalmap[index]);
        } else {
            mat = Material.buildLambertMaterial(walltexture);
        }
        //MeshLambertMaterial mat = new MeshLambertMaterial(new Color(0, 0xCC, 0xCC));
        //mat = Material.buildLambertMaterial(MazeSettings.getSettings().walltexture);

        SceneNode wall;

        if (simpleGeo) {
            SimpleGeometry geo = Primitives.buildSimpleXYPlaneGeometry(width, PILLARHEIGHT, new ProportionalUvMap(new Vector2(u0, 0), new Vector2(u1, 1)));
            wall = new SceneNode(new Mesh(geo, mat));
            /*if (!top) {
                //drehen, damit es von der Rückseite sichtbar ist
                wall.getTransform().rotateY(bottomrotation);
            }*/
        } else {
            // Die Plane besteht immer aus zwei Segmenten, an denen sie dann für Ecken "geknickt" wird.
            // 1.6.21: Das ist aber doch recht kompliziert wegen z.B. der Normalen.
            Shape shape = null;//buildWallShape(width, top, corner, elementdistance);
            GenericGeometry wallgeo = GenericGeometry.buildGenericGeometry(new ShapeGeometry(shape, PILLARHEIGHT, 1, UvMap1.leftRotatedTexture));
            wall = new SceneNode(new Mesh(wallgeo, mat));
            /*if (!top) {
                //drehen, damit es von der Rückseite sichtbar ist
                wall.getTransform().rotateZ(bottomrotation);
            }*/
        }

        return wall;
    }

    private SceneNode buildPlaneForWall(double width) {
        return buildPlaneForWall(width, 0, 1);
    }

    /**
     * Ein alleinstehendes Wall Element. Das ist eine quadratische Säule.
     */
    public SceneNode buildStandaloneWallElement(float width) {
        Shape shape = new Shape(true);
        //Die Punkte des Shape muessen CW sein, sonst passt das Culling nicht.
        shape.addPoint(new Vector2(-width / 2, -width / 2));
        shape.addPoint(new Vector2(-width / 2, +width / 2));
        shape.addPoint(new Vector2(+width / 2, +width / 2));
        shape.addPoint(new Vector2(+width / 2, -width / 2));
        ShapeGeometry wallgeo = new ShapeGeometry(shape, PILLARHEIGHT, 1, UvMap1.leftRotatedTexture);

        Material mat;
        mat = Material.buildLambertMaterial(walltexture);
        //MeshLambertMaterial mat = new MeshLambertMaterial(new Color(0, 0xCC, 0xCC));
        Mesh element = new Mesh(wallgeo, mat);

        SceneNode model = new SceneNode();
        model.setMesh(element);
        // aufrecht stellen
        //model.object3d.rotateX(new Degree(-90));
        // und halb nach oben
        //Vector3 pos = xypos.add(new Vector3(0, Pillar.HEIGHT / 2, 0));
        //model.object3d.setPosition(pos);
        //return model;
        return SceneNode.buildSceneNode(model, new Vector3(0, PILLARHEIGHT / 2, 0), Quaternion.buildRotationX(new Degree(-90)));

    }

    /**
     * Ecken mussen, je nach dem in welche Richtung sie später verschoben werden, innen etwas kuerzer
     * und aussen etwas länger sein.
     */
    public static Shape buildWallShape(float width, boolean top, boolean corner, float elementdistance) {
        if (corner) {
            // an jedem Ende um die Haelfte der distance anpassen
            if (top) {
                // aussen
                width += elementdistance;
            } else {
                // innen
                width -= elementdistance;
            }
        }
        Shape shape = ShapeFactory.buildLine(width, 2);
        if (corner) {
            if (top) {
                shape.getPoints().set(2, new Vector2(0, -width / 2));
            } else {
                shape.getPoints().set(0, new Vector2(0, width / 2));
            }
        }
        return shape;
    }

    public SceneNode buildPillar() {
        return buildPillar(pillarmaterial);
    }

    public SceneNode buildPillar(Color color) {
        return buildPillar(Material.buildBasicMaterial(color));
    }

    public Material getGroundmaterial() {
        return groundmaterial;
    }

    /**
     * Das Center des Boden des Pillar kommt an pos.
     * <p>
     * Created by thomass on 07.05.15.
     *
     * @param material
     */
    public static SceneNode buildPillar(Material material) {
        ShapeGeometry cubegeometry = ShapeGeometry.buildBox(PILLARWIDTH, PILLARHEIGHT, PILLARWIDTH, null);
        SceneNode mesh = new SceneNode(new Mesh(cubegeometry, material));
        // Die Säule muss jetzt noch halb nach oben und die halbe Stärke verschoben
        //Nee, das ist doch für x und z nicht erfoderlich, weil der Pillar genau auf der Grenze steht
        Vector3 pos = /*pos.add*/(new Vector3(0, PILLARHEIGHT / 2, 0));

        mesh.getTransform().setPosition(pos);
        return new SceneNode(mesh);
    }

}
