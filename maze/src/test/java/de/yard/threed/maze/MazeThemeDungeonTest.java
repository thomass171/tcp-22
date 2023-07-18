package de.yard.threed.maze;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.engine.testutil.PlatformFactoryHeadless;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


/**
 * Created by thomass on 15.07.23.
 */
public class MazeThemeDungeonTest {
    static Platform platform = EngineTestFactory.initPlatformForTest(new String[]{"engine", "maze"}, new PlatformFactoryHeadless());
    MazeTheme mazeTheme;

    //no good idea to define statics here because it restarts platform init

    @BeforeEach
    public void setup() {
        mazeTheme = MazeTheme.init(MazeTheme.THEME_DUNGEON);
    }

    /**
     * __####
     * ###  ####
     * #     $ #
     * # #  #$ #
     * # . .#@ #
     * #########
     *
     */
    @Test
    public void testTerrainWikipedia() throws Exception {

        Grid grid = GridTest.loadGridAndTerrain("skbn/SokobanWikipedia.txt", 1);
        MazeLayout layout = grid.getMazeLayout();
        MazeTraditionalTerrain terrain = (MazeTraditionalTerrain) mazeTheme.buildTerrain(layout);
        terrain.visualizeGrid();
        Assertions.assertEquals(layout.getMaxWidth() * layout.getHeight(), terrain.getTiles().values().size(), "tiles");
    }


}
