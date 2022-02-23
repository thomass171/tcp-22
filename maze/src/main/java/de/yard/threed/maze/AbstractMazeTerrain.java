package de.yard.threed.maze;

import de.yard.threed.core.Point;
import de.yard.threed.engine.SceneNode;

import java.util.Map;

/**
 * 3D visualization/representation of a maze.
 * Additionally there is a logical representation (GridState oder MazeLayout).
 * <p>
 * Contains everything static, ground, walls and pillars. Everything in a submodel,so delete is just a remove of the terrain node.
 * <p>
 * No Player, No Boxen.
 * <p>
 * Created by thomass on 22.2.22.
 */
public interface AbstractMazeTerrain {

    /**
     * Put all walls und pillars into the terrain node. But no boxes.
     * <p>
     */
    void visualizeGrid(Grid grid);

    /**
     * used in test
     */
    void addGridElement(SceneNode element, int x, int y, float zfightingyoffset);

    SceneNode getNode();

    Map<Point, SceneNode> getTiles();

    Map<Point, SceneNode> getWalls();
}
