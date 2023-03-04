package de.yard.threed.maze;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.testutil.PlatformFactoryHeadless;
import de.yard.threed.engine.testutil.EngineTestFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 *
 */
public class MazeDataProviderTest {
    static Platform platform = EngineTestFactory.initPlatformForTest( new String[] {"engine","maze"}, new PlatformFactoryHeadless());

    @Test
    public void testMazeDataProvider() {
        MazeDataProvider.reset();
        MazeDataProvider.init();
        Grid grid = (Grid) MazeDataProvider.getInstance().getData(new Object[]{"grid"});
        Assertions.assertNotNull(grid);

        String gridName = (String) MazeDataProvider.getInstance().getData(new Object[]{"gridname"});
        Assertions.assertNotNull(gridName);
        // the default name without explicit initial maze
        assertEquals("skbn/SokobanWikipedia.txt",gridName);
    }
}
