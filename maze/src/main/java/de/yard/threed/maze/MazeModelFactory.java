package de.yard.threed.maze;

import de.yard.threed.core.Degree;
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
 *
 * 15.7.23: Many parts here belong to traditional TerrainBuilder and should be moved to there. But needs to be merged with generic theme rules.
 * Maybe this should be a super factory where MazeTerrain extends.
 *
 * Created by thomass on 06.03.17.
 */
public class MazeModelFactory implements ModelBuilderRegistry {

    public static String DIAMOND_BUILDER = "diamondbuilder";
    public static String BOX_BUILDER = "boxbuilder";
    public static String BULLET_BUILDER = "bulletbuilder";

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

    public Texture walltexture;
    public Texture[] wallnormalmap;
    public Material pillarmaterial, groundmaterial;
    private ShapeGeometry sokobanboxgeo;
    private Material sokobanboxmaterial;

    public MazeModelFactory(MazeTheme settings) {
        buildBasics(settings);
    }


    /**
     * Eine einzelne Bodenkachel erstellen. Bekommt aussen rum eine Normal Map zum abrunden.
     */
    public SceneNode buildGroundElement() {
        SimpleGeometry plane = Primitives.buildPlaneGeometry(MazeDimensions.GRIDSEGMENTSIZE, MazeDimensions.GRIDSEGMENTSIZE, 1, 1);
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
        mesh.getTransform().setPosition(new Vector3(0, MazeTheme.getSettings().sokobanboxsize / 2, 0));
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
        double size = MazeTheme.getSettings().sokobanboxsize / 2;
        Geometry cuboid = Geometry.buildCube(size, size, size);
        // Not too much transparency (0xCC)
        Mesh m = new Mesh(cuboid, Material.buildBasicMaterial(MazeTheme.diamondColor.transparency(0xCC), true));
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
        if (key.equals(BULLET_BUILDER)) {
            return (destinationNode, entity) -> destinationNode.attach(buildSimpleBall(0.3, MazeTheme.bulletColor));
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

    private void buildBasics(MazeTheme settings) {

        switch (settings.getTheme()) {
            case MazeTheme.THEME_TRADITIONAL:
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
        }
    }

    private Texture buildTexture(String s) {
        return Texture.buildBundleTexture("data", s);
    }

}

