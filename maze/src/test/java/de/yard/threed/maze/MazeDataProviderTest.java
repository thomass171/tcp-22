package de.yard.threed.maze;

import com.github.tomakehurst.wiremock.WireMockServer;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.engine.testutil.SceneRunnerForTesting;
import de.yard.threed.javacommon.SimpleHeadlessPlatformFactory;
import de.yard.threed.maze.testutils.MazeTestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class MazeDataProviderTest {
    static Platform platform = EngineTestFactory.initPlatformForTest(new String[]{"engine", "maze"}, new SimpleHeadlessPlatformFactory());

    private WireMockServer wireMockServer;
    private SceneRunnerForTesting sceneRunner;

    @BeforeEach
    void setup() {
        sceneRunner = ((SceneRunnerForTesting) AbstractSceneRunner.getInstance());
        wireMockServer = new WireMockServer(wireMockConfig().port(8089));
        wireMockServer.start();
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    public void testMazeDataProvider() {
        SystemManager.reset();
        MazeDataProvider.reset();
        MazeDataProvider.init();
        Grid grid = (Grid) MazeDataProvider.getInstance().getData(new Object[]{"grid"});
        Assertions.assertNotNull(grid);

        String gridName = (String) MazeDataProvider.getInstance().getData(new Object[]{"gridname"});
        Assertions.assertNotNull(gridName);
        // the default name without explicit initial maze
        assertEquals("skbn/SokobanWikipedia.txt", gridName);
    }

    @Test
    public void testGridTestData() throws Exception {
        // The grid should not contain the sequence '\''n' as separate character.
        assertFalse(MazeTestUtils.responseBody.contains("\\"));
    }

    @Test
    public void testRemoteGrid() throws Exception {
        SystemManager.reset();
        MazeDataProvider.reset();

        MazeTestUtils.mockHttpGetSokobanWikipedia(wireMockServer);

        String url = "http://localhost:" + wireMockServer.port() + "/mazes/1";

        MazeDataProvider.init(url, null);

        TestUtils.waitUntil(() -> sceneRunner.futures.size() > 0 && sceneRunner.futures.get(0).getFirst().isDone(), 2000);

        sceneRunner.runLimitedFrames(1, 0);

        Grid grid = (Grid) MazeDataProvider.getInstance().getData(new Object[]{"grid"});
        Assertions.assertNotNull(grid);
        assertEquals(6, grid.getMazeLayout().height);
        assertEquals(1, grid.getMazeLayout().getNumberOfTeams());
        String gridName = (String) MazeDataProvider.getInstance().getData(new Object[]{"gridname"});
        Assertions.assertNotNull(gridName);
        assertEquals("Sokoban Wikipedia", gridName);
    }
}
