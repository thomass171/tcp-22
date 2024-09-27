package de.yard.threed.maze;

import de.yard.threed.core.Point;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.Material;
import de.yard.threed.engine.Mesh;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.Texture;
import de.yard.threed.engine.avatar.TeamColor;
import de.yard.threed.engine.geometry.ShapeGeometry;

import java.util.HashMap;
import java.util.Map;

/**
 * 3D visualization/representation of the static elements of a maze grid (MazeLayout). Not just a builder but also a helper/provider for
 * intersection detection. And needed for game specific actions like adding a target marker on a wall.
 * <p></p>
 * Additionally there is a logical representation (GridState oder MazeLayout).
 * <p>
 * Contains everything static, ground, walls and pillars. Everything in a submodel, so delete is just a remove of the terrain node.
 * <p>
 * No Player, No Boxes.
 * <p>
 * <p>
 * Created by thomass on 22.2.22.
 */
public abstract class AbstractMazeTerrain {
    protected Log logger;
    protected SceneNode node;
    protected MazeLayout layout;
    protected MazeTraditionalModelFactory mazeModelFactory;

    int width;
    int height;
    float effectivewidth, effectiveheight;
    //public SceneNode terrain;
    boolean simpleground = false;
    private Map<Point, SceneNode> tiles = new HashMap<Point, SceneNode>();
    // walls are only pure H and V walls. But the nodes are the center of two single planes, not the real wall!
    protected Map<Point, SceneNode> walls = new HashMap<Point, SceneNode>();

    static int STRAIGHTWALLMODE_NONE = 0;
    static int STRAIGHTWALLMODE_FULL = 1;
    static final int STRAIGHTWALLMODE_LOW_PART = 2;
    static final int STRAIGHTWALLMODE_HIGH_PART = 3;

    public AbstractMazeTerrain(MazeLayout layout) {
        this.layout = layout;

        int width = layout.getMaxWidth();
        int height = layout.getHeight();
        logger = Platform.getInstance().getLog(MazeTraditionalTerrain.class);

        this.width = width;
        this.height = height;
        effectivewidth = width * MazeDimensions.GRIDSEGMENTSIZE;
        effectiveheight = height * MazeDimensions.GRIDSEGMENTSIZE;

        // some tests expect node to exist
        node = new SceneNode();
        node.getTransform().setPosition(new Vector3(effectivewidth / 2 - MazeDimensions.GRIDSEGMENTSIZE / 2, 0, -effectiveheight / 2 + MazeDimensions.GRIDSEGMENTSIZE / 2));

        /*if (MazeTheme.getSettings().debug) {
            //TODO material.setWireframe(true);
        }*/
    }

    /**
     * Was in constructor once.
     */
    private void buildGround() {

        // 6.10.5: Wie hoch die Anzahl Segments ein wird, ist noch unklar.Erstmal pro GridElement
        if (simpleground) {
            // eigentlich nur fuer Tests. Nur eine einzige grosse Plane. Die ist schon in der xz Ebene.
            ShapeGeometry planeGeometry = ShapeGeometry.buildPlane(effectivewidth, effectiveheight, width, height);
            Material material = /*mazeModelFactory.*/getGroundmaterial();
            /*terrain = new SceneNode(*/
            node.setMesh(new Mesh(planeGeometry, material/*, false, true*/));
        } else {
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    SceneNode tile = /*mazeModelFactory.*/buildGroundElement();
                    //Position relativ zum terrain
                    addGridElement(tile, x, y, 0);
                    Point point = new Point(x, y);
                    tiles.put(point, tile);
                }
            }
        }

    }

    /**
     * Put all walls und pillars into the terrain node. But no boxes.
     * <p>
     * Every element puts pillar only top/right for avoiding duplicates.
     * <p>
     */
    public void visualizeGrid() {

        buildGround();
        int w = layout.getMaxWidth();
        int h = layout.getHeight();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                Point p = new Point(x, y);

                if (x == 6 && y == 2) {
                    int hh = 9;
                }

                if (layout.destinations.contains(p)) {
                    addDecoratedField(x, y, Texture.buildBundleTexture("data", "textures/SokobanTarget.png"));
                }
                if (layout.isStartField(p) && layout.getNumberOfTeams() > 1) {
                    // mark home position, but only when multiple teams are playing
                    // TODO use GridTeam and extract for unit test?
                    int teamId = layout.getTeamByHome(p);
                    String textureFile;
                    if (layout.getTeamByIndex(teamId).isMonsterTeam) {
                        textureFile = "textures/MazeHome-monster.png";
                    } else {
                        // Monster(teams) have their own color. So don't just use teamid as index for user color.
                        textureFile = "textures/MazeHome-" + TeamColor.teamColors[layout.getNonMonsterTeamByHome(p)].getColor() + ".png";
                    }
                    addDecoratedField(x, y, Texture.buildBundleTexture("data", textureFile));
                }
                if (layout.isWall(p)) {
                    handleWall(p);
                }

            }
        }

        buildCeiling();

        finalizeGrid();
        logger.debug("grid visualized");
    }

    protected void buildCeiling() {
    }

    protected void finalizeGrid() {

    }

    /**
     * Returns the top node containing everything.
     *
     * @return
     */
    public SceneNode getNode() {
        return node;
    }

    public Map<Point, SceneNode> getTiles() {
        return tiles;
    }

    public Map<Point, SceneNode> getWalls() {
        return walls;
    }

    private void addDecoratedField(int x, int y, Texture texture) {
        float destinationsize = 1.1f;
        // a bit larger for better visibility with box in front of it.
        destinationsize = 1.3f;
        ShapeGeometry cubegeometry = ShapeGeometry.buildPlane(destinationsize, destinationsize, 1, 1);
        Material mat = Material.buildLambertMaterial(texture, 0.5, true);
        SceneNode dest = new SceneNode(new Mesh(cubegeometry, mat));
        // raise a bit to avoid z-Fighting
        addGridElement(dest, x, y, 0.01f);
    }

    /**
     * used in test
     */
    public void addGridElement(SceneNode element, int x, int y, float zfightingyoffset) {
        Vector3 p = getTerrainElementCoordinates(x, y);
        p = p.add(new Vector3(0, zfightingyoffset, 0));
        element.getTransform().setPosition(p);
        node.attach(element);
    }

    /**
     * Liefert die OpenGL Koordinaten in der y0 Ebene des Zentrums des xy GridFeldes in Relation zum xy Grid.
     *
     * @param x
     * @param y
     * @return
     */
    protected Vector3 getTerrainElementCoordinates(int x, int y) {
        float gsz = MazeDimensions.GRIDSEGMENTSIZE;
        float gsz2 = gsz / 2;
        return new Vector3(-effectivewidth / 2 + x * gsz + gsz2, 0, effectiveheight / 2 - y * gsz - gsz2);
    }

    protected Vector3 getTerrainElementCoordinates(Point p) {
        return getTerrainElementCoordinates(p.getX(), p.getY());
    }

    public static int isVWALL(MazeLayout layout, Point p) {

        if (!layout.isWall(p)) {
            return STRAIGHTWALLMODE_NONE;
        }
        if (layout.isWall(p.addX(-1)) || layout.isWall(p.addX(1))) {
            return STRAIGHTWALLMODE_NONE;
        }
        boolean high = layout.isWall(p.addY(1));
        boolean low = layout.isWall(p.addY(-1));
        if (high && low) {
            return STRAIGHTWALLMODE_FULL;
        }
        if (high) {
            return STRAIGHTWALLMODE_HIGH_PART;
        }
        if (low) {
            return STRAIGHTWALLMODE_LOW_PART;
        }

        return STRAIGHTWALLMODE_NONE;
    }

    public static int isHWALL(MazeLayout layout, Point p) {

        // selber Block und links oder rechts aber nicht drüber oder drunter
        if (!layout.isWall(p)) {
            return STRAIGHTWALLMODE_NONE;
        }
        if (layout.isWall(p.addY(-1)) || layout.isWall(p.addY(1))) {
            return STRAIGHTWALLMODE_NONE;
        }
        boolean high = layout.isWall(p.addX(1));
        boolean low = layout.isWall(p.addX(-1));
        if (high && low) {
            return STRAIGHTWALLMODE_FULL;
        }
        if (high) {
            return STRAIGHTWALLMODE_HIGH_PART;
        }
        if (low) {
            return STRAIGHTWALLMODE_LOW_PART;
        }

        return STRAIGHTWALLMODE_NONE;
    }

    /**
     * Does the wall continue at top?
     * <p>
     * 31.5.21: Also when it is a wall and beyond, but not to the left or right?
     * 1.6.21: No, then it is a center
     */
    public static boolean hasTopWall/*Pillar*/(MazeLayout layout, Point p) {
        boolean isblock = false;

        if (layout.isWall(p)) {
            isblock = true;
        }

        // Wenn es selber Wall ist und darüber auch
        if (isblock && layout.isWall(p.addY(1))) {
            return true;
        }
        return false;
    }

    /**
     * Does the wall continue to the right?
     * <p>
     * 31.5.21: Also when it is the end of a wall, ie wall to the left but not above and beyond?
     * 3.6.21: No, then it is a center
     */
    public static boolean hasRightWall/*Pillar*/(MazeLayout layout, Point p) {

        boolean isblock = false;

        if (layout.isWall(p)) {
            isblock = true;
        }
        // Wenn es selber BLOCK ist und rechts auch
        if (isblock && layout.isWall(p.addX(1))) {
            return true;
        }
        return false;
    }

    /**
     * Alle nicht durchgehenden Walls (also endende) haben einen center pillar. Ausser alleinstehende.
     *
     * @return
     */
    public static boolean hasCenterWall/*Pillar*/(MazeLayout layout, Point p) {

        int surroundingwalls = 0;

        if (!layout.isWall(p)) {
            return false;
        }
        if (layout.isWall(p.addX(1))) {
            surroundingwalls++;
        }
        if (layout.isWall(p.addX(-1))) {
            surroundingwalls++;
        }
        if (layout.isWall(p.addY(1))) {
            surroundingwalls++;
        }
        if (layout.isWall(p.addY(-1))) {
            surroundingwalls++;
        }
        return surroundingwalls == 1;
    }

    abstract Material getGroundmaterial();

    abstract SceneNode buildGroundElement();

    abstract void handleWall(Point p);

}
