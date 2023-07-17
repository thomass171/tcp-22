package de.yard.threed.maze;

import de.yard.threed.core.Point;
import de.yard.threed.engine.SceneNode;

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
public interface AbstractMazeTerrain {

    /**
     * Put all walls und pillars into the terrain node. But no boxes.
     * <p>
     */
    void visualizeGrid();

    /**
     * Returns the top node containing everything.
     * @return
     */
    SceneNode getNode();

    Map<Point, SceneNode> getTiles();

    Map<Point, SceneNode> getWalls();
}
