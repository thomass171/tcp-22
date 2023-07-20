package de.yard.threed.maze;

import de.yard.threed.engine.Material;

/**
 *
 */
public class MazeDungeonModelFactory extends MazeModelFactory {

    public MazeDungeonModelFactory(MazeTheme settings) {
        super(settings);
    }

    public Material getGroundmaterial() {
        return groundmaterial;
    }


}
