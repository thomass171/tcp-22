package de.yard.threed.maze;

import de.yard.threed.core.Degree;
import de.yard.threed.core.Util;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.*;
import de.yard.threed.core.Quaternion;
import de.yard.threed.engine.apps.WoodenToyFactory;
import de.yard.threed.engine.avatar.AvatarPmlFactory;
import de.yard.threed.engine.geometry.Primitives;
import de.yard.threed.engine.geometry.ShapeGeometry;
import de.yard.threed.core.Vector2;
import de.yard.threed.core.Vector3;

import de.yard.threed.engine.gui.Icon;
import de.yard.threed.engine.imaging.NormalMap;
import de.yard.threed.core.Color;
import de.yard.threed.engine.loader.PortableMaterial;
import de.yard.threed.engine.loader.PortableModelDefinition;
import de.yard.threed.engine.loader.PortableModelList;
import de.yard.threed.engine.platform.common.SimpleGeometry;

/**
 * Created by thomass on 06.03.17.
 */
public class MazeModelFactory implements ModelBuilderRegistry {

    public static String DIAMOND_BUILDER = "diamondbuilder";
    public static String BOX_BUILDER = "boxbuilder";

    private static Log logger = Platform.getInstance().getLog(MazeModelFactory.class);

    //Der Abstand zwischen den beiden Oberflächen einer Wand
    //float elementdistance = 0.01f;
    // mit groesserer Distance kann man Probleme besser erkennen.
    // Pillar hat 0.2
    float elementdistance = 0.07f;
    // Die PILLARHEIGHT bestimmt auch die Höhe der Wände und die PILLARWIDTH die Wandbreite
    public static final float PILLARWIDTH = 0.2f;
    public static final float PILLARHEIGHT = 2f;
    public static float gsz = MazeDimensions.GRIDSEGMENTSIZE;
    public static float gsz2 = gsz / 2;
    public static float width2 = PILLARWIDTH / 2;

    // 1.6.21: Die Wall Geometries nicht mehr per Shape und CustomGeometry bauen, sondern per Primitives. Das macht das ganze vielleicht etwas einfacher.
    public static boolean simpleGeo = true;
    private static MazeModelFactory instance = null;

    private Texture walltexture;
    private Texture[] wallnormalmap;
    private Material pillarmaterial, groundmaterial;
    private ShapeGeometry sokobanboxgeo;
    private Material sokobanboxmaterial;

    private MazeModelFactory() {
        buildBasics();
    }

    public static MazeModelFactory getInstance() {
        if (instance == null) {
            instance = new MazeModelFactory();
        }
        return instance;
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
            case Grid.STRAIGHTWALLMODE_LOW_PART:
                wallWidth = width / 2;
                uvx = 0.5;
                offset = -wallWidth / 2;
                break;
            case Grid.STRAIGHTWALLMODE_HIGH_PART:
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

    /**
     * Eine einzelne Bodenkachel erstellen. Bekommt aussen rum eine Normal Map zum abrunden.
     */
    public SceneNode buildGroundElement() {
        SimpleGeometry plane = Primitives.buildPlaneGeometry(MazeDimensions.GRIDSEGMENTSIZE, MazeDimensions.GRIDSEGMENTSIZE, 1, 1);
        //17.6.21: Der init gehoert doch hier nun wirklich nicht hin
        MazeSettings.init(MazeSettings./*17.6.21 MODE_MAZE*/THEME_WOOD);
        SceneNode mesh = new SceneNode(new Mesh(new GenericGeometry(plane), groundmaterial));
        return mesh;
    }

    public static NormalMap buildEdgeNormalmap() {
        return NormalMap.buildEdgeNormalmap(512, 512, 3);
    }

    public static SceneNode buildFireTargetMarker() {
        Icon icon = Icon.ICON_DESTINATION;
        double size = 1.3;
        SimpleGeometry geometry = Primitives.buildSimpleXYPlaneGeometry(size, size, icon.getUvMap());
        SceneNode marker = new SceneNode(new Mesh(geometry, Material.buildBasicMaterial(icon.getTexture())));
        marker.setName("fire target marker");
        // translation not set here, but by caller
        marker.getTransform().rotateY(new Degree(0));
        return marker;
    }

    public SceneNode buildSokobanBox(/*int x, int y*/) {
        //mover = new Mover(this.object3d);

        SceneNode container = new SceneNode();
        SceneNode mesh = new SceneNode(new Mesh(sokobanboxgeo, sokobanboxmaterial));
        // Das Mesh halb nach oben
        mesh.getTransform().setPosition(new Vector3(0, MazeSettings.getSettings().sokobanboxsize / 2, 0));
        // Und das ganze Model auf dir Gridposition
        //Vector3 pos = MazeDimensions.getWorldElementCoordinates(x, y);
        //24.4.21  getTransform().setPosition(pos);
        container.attach(mesh);
        return container;

    }

    public SceneNode buildSimpleBody(float height, float diameter, Color color) {
        Geometry cuboid = Geometry.buildCube(diameter, height, diameter);
        Mesh m = new Mesh(cuboid, Material.buildBasicMaterial(color));
        return new SceneNode(m);
    }

    public SceneNode buildDiamond() {
        double size = MazeSettings.getSettings().sokobanboxsize / 2;
        Geometry cuboid = Geometry.buildCube(size, size, size);
        // Not too much transparency (0xCC)
        Mesh m = new Mesh(cuboid, Material.buildBasicMaterial(MazeSettings.diamondColor.transparency(0xCC), true));
        SceneNode diamond = new SceneNode(m);
        diamond.getTransform().setRotation(Quaternion.buildFromAngles(new Degree(45), new Degree(45), new Degree(45)));
        return new SceneNode(diamond);
    }

    public SceneNode buildSimpleBall(double radius, Color color/*, Point position*/) {
        SimpleGeometry sphere = Primitives.buildSphereGeometry(radius, 32, 32);
        Mesh m = new Mesh(sphere, Material.buildBasicMaterial(color));
        SceneNode ball = new SceneNode(m);
        ball.setName("bullet");
        //ball.getTransform().setPosition(MazeUtils.point2Vector3(position).add(new Vector3(0, 1.25, 0)));
        Scene.getCurrent().addToWorld(ball);
        return ball;
    }

    @Override
    public ModelBuilder lookupModelBuilder(String key) {
        if (key.equals(DIAMOND_BUILDER)) {
            return (destinationNode, entity) -> destinationNode.attach(buildDiamond());
        }
        if (key.equals(BOX_BUILDER)) {
            return (destinationNode, entity) -> destinationNode.attach(buildSokobanBox());
        }
        // No need to do a logging here because there might be other builder factories registered.
        return null;
    }

    /**
     * Just a sphere. Has no elevation above anything, so needs to be raised.
     *
     * @return
     */
    public SceneNode buildMonster() {

        double headRadius = 0.20;

        PortableMaterial faceMaterial = new PortableMaterial("faceMaterial", "maze:textures/Face-Monster.png");

        PortableModelDefinition head = AvatarPmlFactory.buildHead(headRadius, "faceMaterial");

        PortableModelList pml = new PortableModelList(null);
        pml.addModel(head);
        pml.addMaterial(faceMaterial);

        SceneNode model = pml.createPortableModelBuilder().buildModel(null, null);
        model.setName("Monster");
        return new SceneNode(model);
    }

    private void buildBasics() {

        MazeSettings settings = MazeSettings.getSettings();
        switch (settings.getTheme()) {
            case MazeSettings.THEME_DRAFT:
                Util.nomore();
                /*14.6.21 unauusgegoren
                walltexture =buildTexture ("images/BrickLargeBare0001_1_M.jpg");
                pillarmaterial = Material.buildLambertMaterial(buildTexture("images/WoodPlanksDirty0085_M.jpg"));
                UvMap1 uvmap = new ProportionalUvMap(new Vector2(0, 1f / 3f), new Vector2(0.5f, 2f / 3f));
                List<UvMap1> uvmaps = new ArrayList<UvMap1>();
                for (int i = 0; i < 6; i++) {
                    uvmaps.add(uvmap);
                }
                sokobanboxgeo = ShapeGeometry.buildBox(sokobanboxsize, sokobanboxsize, sokobanboxsize, uvmaps);
                sokobanboxmaterial = Material.buildLambertMaterial(buildTexture("images/texturedcube-atlas.jpg"));
                groundmaterial = Material.buildPhongMaterialWithNormalMap(buildTexture("images/FloorsMedieval0034_6_M.jpg"),
                        Texture.buildNormalMap(MazeModelFactory.buildEdgeNormalmap().image));*/
                break;
            case MazeSettings.THEME_WOOD:
                walltexture = buildTexture("textures/gimp/wood/BucheHell.png");
                pillarmaterial = Material.buildLambertMaterial(buildTexture("textures/gimp/wood/BucheDunkel.png"));
                sokobanboxgeo = ShapeGeometry.buildBox(settings.sokobanboxsize, settings.sokobanboxsize, settings.sokobanboxsize, null);
                sokobanboxmaterial = Material.buildLambertMaterial(buildTexture("textures/gimp/wood/BucheMedium.png"));
                //22.3.17: Wandmaterial auch fuer Boden statt Ground.png
                groundmaterial = Material.buildPhongMaterialWithNormalMap(buildTexture("textures/gimp/wood/BucheHell.png"),
                        Texture.buildNormalMap(MazeModelFactory.buildEdgeNormalmap().image));
                //wallnormalmap = buildTexture("textures/gimp/SampleWallNormalMapByPetry.png"));
                //Ein paar anlegen, damit nicht alle gleich aussehen. Die werden durch randon alle unterschiedlich
                wallnormalmap = new Texture[5];
                wallnormalmap[0] = Texture.buildNormalMap(new WoodenToyFactory().buildWallNormalMap(6).image);
                wallnormalmap[1] = Texture.buildNormalMap(new WoodenToyFactory().buildWallNormalMap(6).image);
                wallnormalmap[2] = Texture.buildNormalMap(new WoodenToyFactory().buildWallNormalMap(6).image);
                wallnormalmap[3] = Texture.buildNormalMap(new WoodenToyFactory().buildWallNormalMap(6).image);
                wallnormalmap[4] = Texture.buildNormalMap(new WoodenToyFactory().buildWallNormalMap(6).image);
                break;
            case MazeSettings.THEME_REALWOOD:
                walltexture = buildTexture("textures/gimp/wood/BucheHell.png");
                pillarmaterial = Material.buildLambertMaterial(buildTexture("textures/gimp/wood/BucheDunkel.png"));
                sokobanboxgeo = ShapeGeometry.buildBox(settings.sokobanboxsize, settings.sokobanboxsize, settings.sokobanboxsize, null);
                sokobanboxmaterial = Material.buildLambertMaterial(buildTexture("textures/gimp/wood/BucheMedium.png"));
                //22.3.17: Wandmaterial auch fuer Boden statt Ground.png
                groundmaterial = Material.buildPhongMaterialWithNormalMap(buildTexture("textures/gimp/wood/BucheHell.png"),
                        Texture.buildNormalMap(MazeModelFactory.buildEdgeNormalmap().image));
                wallnormalmap = null;
                walltexture = buildTexture("textures/realwood/Wall.png");

                break;
        }
    }

    private Texture buildTexture(String s) {
        return Texture.buildBundleTexture("data", s);
    }

}

