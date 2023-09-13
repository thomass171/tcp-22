package de.yard.threed.maze;

import de.yard.threed.core.*;
import de.yard.threed.engine.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 3D visualization of a maze. This is a traditional grid where
 * - A pillar is always located in the mid on the boundary of two grid fields.
 * - There is no pillar at corners (where walls intersect) or on 'T's.
 * <p>
 * Also see GridState and MazeLayout.
 * <p>
 * <p/>
 * Created by thomass on 07.05.15.
 */
public class MazeTraditionalTerrain extends AbstractMazeTerrain {

    public Map<Point, SceneNode> topPillars = new HashMap<Point, SceneNode>();
    public Map<Point, SceneNode> rightPillars = new HashMap<Point, SceneNode>();
    public Map<Point, SceneNode> centerPillars = new HashMap<Point, SceneNode>();
    MazeTraditionalModelFactory mazeModelFactory;

    public MazeTraditionalTerrain(MazeLayout layout/*int width, int height*/, MazeTraditionalModelFactory mazeModelFactory) {
        super(layout);
        this.mazeModelFactory = mazeModelFactory;
    }

    @Override
    Material getGroundmaterial() {
        return mazeModelFactory.getGroundmaterial();
    }

    @Override
    SceneNode buildGroundElement() {
        return mazeModelFactory.buildGroundElement();
    }

    @Override
    void handleWall(Point p) {

        int x = p.getX();
        int y = p.getY();

        if (hasTopWall(layout, p)) {
            addTopPillar(mazeModelFactory.buildPillar(), p);
            //addTopPillar(mf.buildPillar(), p);
        }
        if (hasRightWall(layout, p)) {
            addRightPillar(mazeModelFactory.buildPillar(), p);
            //addRightPillar(mf.buildPillar(), p);
        }
        if (hasCenterWall(layout, p)) {
            addCenterPillar(mazeModelFactory.buildPillar(), p);
            //addCenterPillar(mf.buildPillar(), p);
        }
        int wallmode;
        if ((wallmode = isHWALL(layout, p)) > 0) {
            SceneNode wall = mazeModelFactory.buildWall(MazeDimensions.GRIDSEGMENTSIZE - MazeModelFactory.PILLARWIDTH, wallmode);
            addGridElement(wall, x, y, 0);
            walls.put(p, wall);
        } else {
            if ((wallmode = isVWALL(layout, p)) > 0) {
                SceneNode wall = mazeModelFactory.buildWall(MazeDimensions.GRIDSEGMENTSIZE - MazeModelFactory.PILLARWIDTH, wallmode);
                wall.getTransform().rotateY(new Degree(90));
                addGridElement(wall, x, y, 0);
                walls.put(p, wall);
            } else {
                // some kind of corner, 'T' or single block?
                if (layout.isWall(p)) {

                    // Erstmal feststellen, in welche Richtungen ein Wand gehen muss.

                    String directions = "";
                    if (hasTopWall(layout, p)) {
                        directions += "T";
                    }
                    if (hasRightWall(layout, p)) {
                        directions += "R";
                    }
                    if (hasTopWall(layout, new Point(x, y - 1))) {
                        directions += "B";
                    }
                    if (hasRightWall(layout, new Point(x - 1, y))) {
                        directions += "L";
                    }

                    SceneNode wallElement = null;
                    Degree angle = new Degree(0);
                    switch (StringUtils.length(directions)) {
                        case 4:
                            // Kreuz
                            wallElement = mazeModelFactory.buildCross(MazeDimensions.GRIDSEGMENTSIZE - MazeModelFactory.PILLARWIDTH/*, xypos*/);
                            break;
                        case 3:
                            // T
                            wallElement = mazeModelFactory.buildT(MazeDimensions.GRIDSEGMENTSIZE - MazeModelFactory.PILLARWIDTH/*, xypos*/);
                            angle = getTAngle(directions);
                            break;
                        case 2:
                            // Ecke
                            wallElement = mazeModelFactory.buildCorner(MazeDimensions.GRIDSEGMENTSIZE - MazeModelFactory.PILLARWIDTH/*, xypos*/, STRAIGHTWALLMODE_NONE);
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
                            wallElement = mazeModelFactory.buildStandaloneWallElement(MazeDimensions.GRIDSEGMENTSIZE - MazeModelFactory.PILLARWIDTH);
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
     * @return top/right/center array
     */
    public SceneNode[] getPillar(Point p) {
        return new SceneNode[]{topPillars.get(p), rightPillars.get(p), centerPillars.get(p)};
    }


}
