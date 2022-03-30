package de.yard.threed.maze;

import de.yard.threed.core.*;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.*;
import de.yard.threed.engine.geometry.ShapeGeometry;
import de.yard.threed.core.platform.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * 3D visualization of a maze. This is a traditional grid where
 * - A pillar is always located in the mid on the boundary of two grid fields.
 * - There is no pillar at corners (where walls intersect) or on 'T's.
 * <p>
 * Dazu gibt es noch die logische (GridState oder MazeLayout).
 * Das ist zunächst mal speziell auf Maze zugeschnitten.
 * Es enthält alles statische, den Ground, alle Wände und Pillars. Ist alles in einem SubModel, sodass ein Löschen einfach mit remove des Terrain geht.
 * <p>
 * Aber keine Player, keine Boxen.
 * <p>
 * <p/>
 * Created by thomass on 07.05.15.
 */
public class MazeTerrain implements AbstractMazeTerrain {
    Log logger = Platform.getInstance().getLog(MazeTerrain.class);
    int width;
    int height;
    float effectivewidth, effectiveheight;
    //public SceneNode terrain;
    boolean simpleground = false;
    private Map<Point, SceneNode> tiles = new HashMap<Point, SceneNode>();
    // walls are only pure H and V walls. But the nodes are the center of two single planes, not the real wall!
    private Map<Point, SceneNode> walls = new HashMap<Point, SceneNode>();
    public Map<Point, SceneNode> topPillars = new HashMap<Point, SceneNode>();
    public Map<Point, SceneNode> rightPillars = new HashMap<Point, SceneNode>();
    public Map<Point, SceneNode> centerPillars = new HashMap<Point, SceneNode>();
    private SceneNode node;

    public MazeTerrain(int width, int height) {
        node = new SceneNode();
        this.width = width;
        this.height = height;
        effectivewidth = width * MazeDimensions.GRIDSEGMENTSIZE;
        effectiveheight = height * MazeDimensions.GRIDSEGMENTSIZE;
        // 6.10.5: Wie hoch die Anzahl Segments ein wird, ist noch unklar.Erstmal pro GridElement
        if (simpleground) {
            // eigentlich nur fuer Tests. Nur eine einzige grosse Plane. Die ist schon in der xz Ebene.
            ShapeGeometry planeGeometry = ShapeGeometry.buildPlane(effectivewidth, effectiveheight, width, height);
            Material material = MazeSettings.getSettings().groundmaterial;
            /*terrain = new SceneNode(*/
            node.setMesh(new Mesh(planeGeometry, material/*, false, true*/));
        } else {
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    SceneNode tile = MazeModelFactory.buildGroundElement();
                    //Position relativ zum terrain
                    addGridElement(tile, x, y, 0);
                    Point point = new Point(x, y);
                    tiles.put(point, tile);
                }
            }
        }
        node.getTransform().setPosition(new Vector3(effectivewidth / 2 - MazeDimensions.GRIDSEGMENTSIZE / 2, 0, -effectiveheight / 2 + MazeDimensions.GRIDSEGMENTSIZE / 2));


        if (MazeSettings.getSettings().debug) {
            //TODO material.setWireframe(true);
        }
    }

    /**
     * Alle Walls und Pillars kommen ins Terrain. Nur die Boxen nicht.
     * <p>
     * Jedes Element baut seine Pillar nur oben und rechts, damit die nicht doppelt
     * gebaut werdebn.
     * <p>
     */
    @Override
    public void visualizeGrid(Grid grid) {
        MazeModelFactory mf = new MazeModelFactory();

        int w = grid.getMaxWidth();
        int h = grid.getHeight();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                Point p = new Point(x, y);

                if (x == 6 && y == 2) {
                    int hh = 9;
                }
                if (grid.hasTopPillar(p)) {
                    addTopPillar(MazeModelFactory.buildPillar(), p);
                }
                if (grid.hasRightPillar(p)) {
                    addRightPillar(MazeModelFactory.buildPillar(), p);
                }
                if (grid.hasCenterPillar(p)) {
                    addCenterPillar(MazeModelFactory.buildPillar(), p);
                }
                if (grid.getMazeLayout().destinations.contains(p)) {
                    addDecoratedField(x, y, Texture.buildBundleTexture("data", "textures/SokobanTarget.png"));
                }
                if (grid.getMazeLayout().isStartField(p) && grid.getMazeLayout().getNumberOfTeams() > 1) {
                    // mark home position, but only when multiple teams are playing
                    int teamId = grid.getMazeLayout().getTeamByHome(p);
                    String textureFile;
                    if (grid.getMazeLayout().getStartPositionsOfTeam(teamId).get(0).isMonster) {
                        textureFile = "textures/MazeHome-monster.png";
                    } else {
                        textureFile = "textures/MazeHome-" + MazeSettings.teamColors[teamId] + ".png";
                    }
                    addDecoratedField(x, y, Texture.buildBundleTexture("data", textureFile));
                }
                int wallmode;
                if ((wallmode = grid.isHWALL(p)) > 0) {
                    SceneNode wall = mf.buildWall(MazeDimensions.GRIDSEGMENTSIZE - MazeModelFactory.PILLARWIDTH, wallmode);
                    addGridElement(wall, x, y, 0);
                    walls.put(p, wall);
                } else {
                    if ((wallmode = grid.isVWALL(p)) > 0) {
                        SceneNode wall = mf.buildWall(MazeDimensions.GRIDSEGMENTSIZE - MazeModelFactory.PILLARWIDTH, wallmode);
                        wall.getTransform().rotateY(new Degree(90));
                        addGridElement(wall, x, y, 0);
                        walls.put(p, wall);
                    } else {
                        // some kind of corner, 'T' or single block?
                        if (grid.isWall(p)) {
                            // Erstmal feststellen, in welche Richtungen ein Wand gehen muss.

                            String directions = "";
                            if (grid.hasTopPillar(p)) {
                                directions += "T";
                            }
                            if (grid.hasRightPillar(p)) {
                                directions += "R";
                            }
                            if (grid.hasTopPillar(new Point(x, y - 1))) {
                                directions += "B";
                            }
                            if (grid.hasRightPillar(new Point(x - 1, y))) {
                                directions += "L";
                            }

                            SceneNode wallElement = null;
                            Degree angle = new Degree(0);
                            switch (StringUtils.length(directions)) {
                                case 4:
                                    // Kreuz
                                    wallElement = mf.buildCross(MazeDimensions.GRIDSEGMENTSIZE - MazeModelFactory.PILLARWIDTH/*, xypos*/);
                                    break;
                                case 3:
                                    // T
                                    wallElement = mf.buildT(MazeDimensions.GRIDSEGMENTSIZE - MazeModelFactory.PILLARWIDTH/*, xypos*/);
                                    angle = getTAngle(directions);
                                    break;
                                case 2:
                                    // Ecke
                                    wallElement = mf.buildCorner(MazeDimensions.GRIDSEGMENTSIZE - MazeModelFactory.PILLARWIDTH/*, xypos*/, Grid.STRAIGHTWALLMODE_NONE);
                                    // Die Drehung ist abhängig davon, wo die Ecke steht
                                    if (StringUtils.contains(directions, "T")) {
                                        if (StringUtils.contains(directions, "R")) {
                                            angle = new Degree(180);
                                        } else if (StringUtils.contains(directions, "L")) {
                                            angle = new Degree(-90);
                                        }
                                    } else {
                                        if (StringUtils.contains(directions, "R")) {
                                            angle = new Degree(90);
                                        }
                                        // ansonsten keine Drehung
                                    }
                                    break;
                                case 0:
                                    // Ein einzelnes Block Element
                                    wallElement = mf.buildStandaloneWallElement(MazeDimensions.GRIDSEGMENTSIZE - MazeModelFactory.PILLARWIDTH);
                                    break;
                                default:
                                    logger.warn("invalid wall element at " + x + " " + y);
                                    // should not happen
                                    break;
                            }
                            if (wallElement != null) {
                                wallElement.getTransform().rotateY(angle);
                                addGridElement(wallElement, x, y, 0);
                            }
                        }
                    }
                }
            }
        }
        logger.debug("grid visualized");
    }

    private void addDecoratedField(int x, int y, Texture texture) {
        float destinationsize = 1.1f;
        // a bit larger for better visibility with box in front of it.
        destinationsize = 1.3f;
        ShapeGeometry cubegeometry = ShapeGeometry.buildPlane(destinationsize, destinationsize, 1, 1);
        Material mat = Material.buildLambertMaterial(texture, true, true);
        SceneNode dest = new SceneNode(new Mesh(cubegeometry, mat));
        // raise a bit to avoid z-Fighting
        addGridElement(dest, x, y, 0.01f);
    }

    @Override
    public SceneNode getNode() {
        return node;
    }

    @Override
    public Map<Point, SceneNode> getTiles() {
        return tiles;
    }

    @Override
    public Map<Point, SceneNode> getWalls() {
        return walls;
    }

    private Degree getTAngle(String directions) {
        if (!StringUtils.contains(directions, "R")) {
            return new Degree(-90);
        }
        if (!StringUtils.contains(directions, "L")) {
            return new Degree(90);
        }
        if (!StringUtils.contains(directions, "B")) {
            return new Degree(180);
        }
        // ansonsten keine Drehung
        return new Degree(0);
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

    private void addTopPillar(SceneNode pillar, Point p) {
        topPillars.put(p, pillar);
        pillar.getTransform().setPosition(getTerrainElementCoordinates(p).add(MazeDimensions.getTopOffset()));
        node.attach(pillar);
    }

    private void addRightPillar(SceneNode pillar, Point p) {
        rightPillars.put(p, pillar);
        pillar.getTransform().setPosition(getTerrainElementCoordinates(p).add(MazeDimensions.getRightOffset()));
        node.attach(pillar);
    }

    private void addCenterPillar(SceneNode pillar, Point p) {
        centerPillars.put(p, pillar);
        pillar.getTransform().setPosition(getTerrainElementCoordinates(p));
        node.attach(pillar);
    }

    /**
     * Liefert die OpenGL Koordinaten in der y0 Ebene des Zentrums des xy GridFeldes in Relation zum xy Grid.
     *
     * @param x
     * @param y
     * @return
     */
    private Vector3 getTerrainElementCoordinates(int x, int y) {
        float gsz = MazeDimensions.GRIDSEGMENTSIZE;
        float gsz2 = gsz / 2;
        return new Vector3(-effectivewidth / 2 + x * gsz + gsz2, 0, effectiveheight / 2 - y * gsz - gsz2);
    }

    private Vector3 getTerrainElementCoordinates(Point p) {
        return getTerrainElementCoordinates(p.getX(), p.getY());
    }

    /**
     * @return top/right/center array
     */
    public SceneNode[] getPillar(Point p) {
        return new SceneNode[]{topPillars.get(p), rightPillars.get(p), centerPillars.get(p)};
    }
}
