package de.yard.threed.sceneserver;

import de.yard.threed.core.Packet;
import de.yard.threed.core.Pair;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.EntityFilter;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.ecs.SystemState;
import de.yard.threed.engine.ecs.UserSystem;
import de.yard.threed.sceneserver.testutils.TestClient;
import de.yard.threed.sceneserver.testutils.TestUtils;
import de.yard.threed.traffic.apps.BasicTravelScene;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import static de.yard.threed.engine.BaseEventRegistry.BASE_EVENT_ENTITY_CHANGE;
import static de.yard.threed.sceneserver.testutils.TestUtils.waitForClientConnected;
import static de.yard.threed.sceneserver.testutils.TestUtils.waitForClientPacket;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test for traffic "Demo.xml" scene (traffic scene definition "traffic:tiles/Demo.xml").
 */
@Slf4j
public class DemoSceneTest {

    static final int INITIAL_FRAMES = 10;

    SceneServer sceneServer;

    @BeforeEach
    public void setup() throws Exception {
        HashMap<String, String> properties = new HashMap<String, String>();
        properties.put("argv.basename", "traffic:tiles/Demo.xml");
        // just to be sure to have automove. automove is already enabled in Demo.xml
        properties.put("argv.enableAutomove", "true");
        System.setProperty("scene", "de.yard.threed.traffic.apps.BasicTravelScene");
        sceneServer = TestUtils.setupServerForScene("de.yard.threed.traffic.apps.BasicTravelScene", INITIAL_FRAMES, properties);
    }

    @Test
    public void testLaunch() throws IOException {
        log.debug("testLaunch");
        //?assertRunningThreads(); läuft docvh nur der clientlistener?
        assertEquals(INITIAL_FRAMES, sceneServer.getSceneRunner().getFrameCount());
        // no user/avatar and graph yet.
        assertEquals(0, SystemManager.findEntities((EntityFilter) null).size(), "number of entities");

        SystemState.state = SystemState.STATE_READY_TO_JOIN;

        TestClient testClient = new TestClient();
        testClient.connectAndLogin();
        waitForClientConnected();
        waitForClientPacket();

        TestUtils.runAdditionalFrames(sceneServer.getSceneRunner(), 5);
        assertEquals(INITIAL_FRAMES + 5, sceneServer.getSceneRunner().getFrameCount());

        // Check login succeeded.
        // possible race condition with movements arriving before login/joined event
        List<Packet> packets = testClient.getAllPackets();
        assertTrue(packets.size() > 0);
        TestUtils.assertEventPacket(UserSystem.USER_EVENT_LOGGEDIN, null, packets);

        // join happened implicitly, so Avatar should exist.
        TestUtils.assertEventPacket(UserSystem.USER_EVENT_JOINED, null, packets);

        // wait for terrain available to load vehicle
        TestUtils.runAdditionalFrames(sceneServer.getSceneRunner(), 50);
        List<EcsEntity> entities = SystemManager.findEntities((EntityFilter) null);
        assertEquals(1 + 1, entities.size(), "number of entites (avatar+loc)");
        EcsEntity userEntity = SystemManager.findEntities(e -> TestClient.USER_NAME.equals(e.getName())).get(0);
        assertNotNull(userEntity, "user entity");
        EcsEntity locEntity = SystemManager.findEntities(e -> "loc".equals(e.getName())).get(0);
        assertNotNull(locEntity, "loc entity");

        // Movements also should arrive in client
        TestUtils.assertEventPacket(BASE_EVENT_ENTITY_CHANGE, new Pair[]{
                new Pair("p_position", "*")
        }, packets);


        SceneNode locNode = locEntity.getSceneNode();
        double xpos0 = locNode.getTransform().getPosition().getX();
        TestUtils.runAdditionalFrames(sceneServer.getSceneRunner(), 50);
        double xpos1 = locNode.getTransform().getPosition().getX();
        double xdiff = Math.abs(xpos0 - xpos1);
        log.debug("xdiff={}", xdiff);
        assertTrue(xdiff > 3.0);

    }

    /**
     * Scene will have no client connected, thus no player/user.
     */
    @Test
    public void testLongRunning() {
        TestUtils.runAdditionalFrames(sceneServer.getSceneRunner(), 300);

    }
}
