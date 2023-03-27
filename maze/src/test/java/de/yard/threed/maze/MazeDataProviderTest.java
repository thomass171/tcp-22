package de.yard.threed.maze;

import com.github.tomakehurst.wiremock.WireMockServer;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.engine.testutil.SceneRunnerForTesting;
import de.yard.threed.javacommon.SimpleHeadlessPlatformFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
    public void testRemoteGrid() throws Exception {
        SystemManager.reset();
        MazeDataProvider.reset();

        String responseBody = "{\n" +
                "  \"name\" : \"Sokoban Wikipedia\",\n" +
                "  \"grid\" : \"  ####\n###  ####\n#     $ #\n# #  #$ #\n# . .#@ #\n#########\",\n" +
                "  \"secret\" : null,\n" +
                "  \"description\" : \"The example from Wikipedia\",\n" +
                "  \"type\" : \"S\",\n" +
                "  \"createdAt\" : \"2023-03-18T17:16:58.609591+01:00\",\n" +
                "  \"createdBy\" : \"admin\",\n" +
                "  \"modifiedAt\" : \"2023-03-18T17:16:58.609591+01:00\",\n" +
                "  \"modifiedBy\" : \"admin\",\n" +
                "  \"_links\" : {\n" +
                "    \"self\" : {\n" +
                "      \"href\" : \"http://ubuntu-server.udehlavj1efjeuqv.myfritz.net/mazes/1\"\n" +
                "    },\n" +
                "    \"maze\" : {\n" +
                "      \"href\" : \"http://ubuntu-server.udehlavj1efjeuqv.myfritz.net/mazes/1\"\n" +
                "    }\n" +
                "  }\n" +
                "}";

        String url = "http://localhost:" + wireMockServer.port() + "/mazes/1";

        wireMockServer.stubFor(get(urlEqualTo("/mazes/1"))
                //.withHeader("Accept", matching("text/.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/hal+json")
                        .withBody(responseBody)));

        MazeDataProvider.init(url);

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
