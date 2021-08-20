package de.yard.threed.maze;


import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;

public class MazeLevel {
    static Log logger = Platform.getInstance().getLog(MazeLevel.class);

    String name;
    String grid;

    MazeLevel(String name, String grid) {
        this.name = name;
        this.grid = grid;
    }


}
