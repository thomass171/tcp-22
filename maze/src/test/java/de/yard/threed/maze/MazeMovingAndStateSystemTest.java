package de.yard.threed.maze;

import com.github.tomakehurst.wiremock.WireMockServer;
import de.yard.threed.core.Event;
import de.yard.threed.core.Point;
import de.yard.threed.core.configuration.ConfigurationByProperties;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.engine.BaseEventRegistry;
import de.yard.threed.engine.ModelBuilderRegistry;
import de.yard.threed.engine.Observer;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.EcsTestHelper;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.ecs.SystemState;
import de.yard.threed.engine.ecs.UserComponent;
import de.yard.threed.engine.ecs.UserSystem;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;
import de.yard.threed.engine.testutil.SceneRunnerForTesting;
import de.yard.threed.engine.vr.VrInstance;
import de.yard.threed.maze.testutils.MazeTestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static de.yard.threed.maze.MazeTheme.THEME_TRADITIONAL;
import static org.junit.jupiter.api.Assertions.*;


/**
 * <p>
 * Created by thomass on 28.01.22.
 */
public class MazeMovingAndStateSystemTest {

    private WireMockServer wireMockServer;
    private SceneRunnerForTesting sceneRunner;
    private MazeMovingAndStateSystem mazeMovingAndStateSystem;

    /**
     *
     */
    @BeforeEach
    public void setup() {

        SystemState.state = 0;
        MazeDataProvider.reset();
        // should be done in setup SystemManager.reset();

        wireMockServer = new WireMockServer(wireMockConfig().port(8089));
        wireMockServer.start();

        EcsTestHelper.setup(() -> {
            MazeTheme st = MazeTheme.buildFromIdentifier(THEME_TRADITIONAL);
            SystemManager.addSystem(new MazeMovingAndStateSystem(st));

        }, "engine", "maze");

        sceneRunner = (SceneRunnerForTesting) AbstractSceneRunner.instance;
        mazeMovingAndStateSystem = (MazeMovingAndStateSystem) SystemManager.findSystem(MazeMovingAndStateSystem.TAG);

    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    public void testSimpleNonVR() throws Exception {

        Observer.buildForDefaultCamera();
        assertNotNull(Observer.getInstance(), "observer");

        startSimpleTest("skbn/SokobanWikipedia.txt");
    }

    @Test
    public void testSimpleVR() throws Exception {

        Map<String, String> properties = new HashMap<>();
        properties.put("enableVR", "true");
        Platform.getInstance().getConfiguration().addConfiguration(new ConfigurationByProperties(properties), true);

        VrInstance.buildFromArguments();
        assertNotNull(VrInstance.getInstance(), "VrInstance");

        Observer.buildForDefaultCamera();
        assertNotNull(Observer.getInstance(), "observer");

        startSimpleTest("skbn/SokobanWikipedia.txt");

    }

    @Test
    public void testRemoteGridNotFound() throws Exception {

        MazeTestUtils.mockHttpGetSokobanWikipedia(wireMockServer);

        // shows wiremock log message 'url does not match'
        String url = "http://localhost:" + wireMockServer.port() + "/mazes/99";

        MazeDataProvider.init(url,null);

        sceneRunner.runLimitedFrames(10, 0.1, 100);
    }

    @Test
    public void testSuccessfulJoinViaRequestSokobanWikipedia() throws Exception {

        Observer.buildForDefaultCamera();
        assertNotNull(Observer.getInstance(), "observer");

        EcsEntity userEntity = startSimpleTest("skbn/SokobanWikipedia.txt");

        SystemManager.putRequest(UserSystem.buildJoinRequest(userEntity.getId()/*,false*/));
        EcsTestHelper.processRequests();

        List<Event> joinEvents = EcsTestHelper.getEventsFromHistory(BaseEventRegistry.USER_EVENT_JOINED);
        assertEquals(1, joinEvents.size(), "JOIN events");
        Event joinEvent = joinEvents.get(0);

        MoverComponent mc = MoverComponent.getMoverComponent(userEntity);
        assertNotNull(mc);
        // should be on start position
        TestUtils.assertPoint(new Point(6,1), mc.getLocation());
        // but has no scenenode yet (not yet assembled)
        assertNull(userEntity.getSceneNode());
        assertNull(mc.getMovable());

        // mock assemble without AvatarSystem.
        userEntity.buildSceneNodeByModelFactory("", new ModelBuilderRegistry[]{});
        SystemManager.sendEvent(BaseEventRegistry.buildUserAssembledEvent(userEntity));
        EcsTestHelper.processSeconds(2);
        // has empty scenenode now
        assertNotNull(userEntity.getSceneNode());
        assertNotNull(mc.getMovable());

        // second player should be rejected
        EcsEntity secondPlayer = joinSecondPlayer();
        List<Event> failEvents = EcsTestHelper.getEventsFromHistory(BaseEventRegistry.USER_EVENT_JOINFAILED);
        assertEquals(1, failEvents.size(), "FAIL events");
        Event failEvent = joinEvents.get(0);

    }

    @Test
    public void testSuccessfulJoinViaRequestP_Simple() throws Exception {

        Observer.buildForDefaultCamera();
        assertNotNull(Observer.getInstance(), "observer");

        EcsEntity userEntity = startSimpleTest("maze/Maze-P-Simple.txt");

        SystemManager.putRequest(UserSystem.buildJoinRequest(userEntity.getId()/*,false*/));
        EcsTestHelper.processRequests();

        List<Event> joinEvents = EcsTestHelper.getEventsFromHistory(BaseEventRegistry.USER_EVENT_JOINED);
        assertEquals(1, joinEvents.size(), "JOIN events");
        Event joinEvent = joinEvents.get(0);

        // second player should be rejected
        EcsEntity secondPlayer = joinSecondPlayer();
        List<Event> failEvents = EcsTestHelper.getEventsFromHistory(BaseEventRegistry.USER_EVENT_JOINFAILED);
        assertEquals(1, failEvents.size(), "FAIL events");
        Event failEvent = joinEvents.get(0);
    }

    /**
     * Prepare everything like after LOGIN event.
     */
    private EcsEntity startSimpleTest(String gridName) {

        // what is the grid expected here?
        MazeDataProvider.init();
        EcsTestHelper.processSeconds(2);
        assertNotNull(Grid.getInstance());

        String testUserName = "testUserName";
        EcsEntity user = new EcsEntity(new UserComponent("user account"));
        user.setName(testUserName);
        //t.b.c.?? Could be per test
        return user;
    }

    /**
     * Prepare everything like after LOGIN event.
     */
    private EcsEntity joinSecondPlayer() {

        String testUserName = "secondUserName";
        EcsEntity user = new EcsEntity(new UserComponent("second player"));
        user.setName(testUserName);

        SystemManager.putRequest(UserSystem.buildJoinRequest(user.getId()));
        EcsTestHelper.processRequests();

        return user;
    }
}
